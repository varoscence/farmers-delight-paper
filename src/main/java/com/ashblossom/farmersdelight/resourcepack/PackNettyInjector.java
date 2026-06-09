package com.ashblossom.farmersdelight.resourcepack;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.ServerChannel;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PackNettyInjector {

    private static final String INIT_KEY   = "fd_pack_init";
    private static final String DETECT_KEY = "fd_pack_detect";

    public static void inject(FarmersDelightPlugin plugin) {
        // Retry a few times — channels list may be empty on the very first tick
        scheduleAttempt(plugin, 1);
    }

    private static void scheduleAttempt(FarmersDelightPlugin plugin, int attempt) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                int injected = doInject(plugin);
                if (injected > 0) {
                    plugin.getLogger().info("[FD] Resource pack Netty handler active on game port "
                        + plugin.getServer().getPort() + " (" + injected + " channel(s))");
                } else if (attempt < 5) {
                    plugin.getLogger().info("[FD] No server channels found yet, retrying (" + attempt + "/5)...");
                    scheduleAttempt(plugin, attempt + 1);
                } else {
                    plugin.getLogger().warning("[FD] Could not find server channels after 5 attempts.");
                    plugin.getLogger().warning("[FD] Resource pack is still served via the configured URL if set.");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[FD] Netty injection error (attempt " + attempt + "): " + e.getMessage());
                if (attempt < 3) scheduleAttempt(plugin, attempt + 1);
            }
        }, attempt * 5L); // 5, 10, 15, 20, 25 ticks
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int doInject(FarmersDelightPlugin plugin) throws Exception {
        // -- Step 1: Get MinecraftServer --
        Object nms = Bukkit.getServer().getClass()
            .getMethod("getServer").invoke(Bukkit.getServer());

        // -- Step 2: Get ServerConnectionListener via three strategies --
        Object conn = null;

        // Strategy A: getConnection() — Mojang-mapped public method (Paper 1.17+)
        for (Method m : nms.getClass().getMethods()) {
            if (m.getName().equals("getConnection") && m.getParameterCount() == 0) {
                Object r = m.invoke(nms);
                if (r != null) { conn = r; break; }
            }
        }

        // Strategy B: field named "connection" (Mojang-mapped field name)
        if (conn == null) {
            for (Class<?> c = nms.getClass(); c != null; c = c.getSuperclass()) {
                try {
                    Field f = c.getDeclaredField("connection");
                    f.setAccessible(true);
                    conn = f.get(nms);
                    if (conn != null) break;
                } catch (NoSuchFieldException ignored) {}
            }
        }

        // Strategy C: scan all fields of MinecraftServer looking for an object
        //             that itself has a List field containing ChannelFuture items
        if (conn == null) {
            outer:
            for (Class<?> c = nms.getClass(); c != null; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    if (f.getType().isPrimitive() || f.getType() == String.class) continue;
                    f.setAccessible(true);
                    Object candidate;
                    try { candidate = f.get(nms); } catch (Exception e) { continue; }
                    if (candidate == null) continue;
                    if (hasChannelFutureList(candidate)) { conn = candidate; break outer; }
                }
            }
        }

        if (conn == null) {
            throw new RuntimeException("ServerConnectionListener not found via getConnection(), "
                + "field 'connection', or field scan on " + nms.getClass().getSimpleName());
        }

        // -- Step 3: Find List<ChannelFuture> in the connection object --
        List<ChannelFuture> futures = null;

        // Try field named "channels" (Mojang-mapped name)
        for (Class<?> c = conn.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField("channels");
                f.setAccessible(true);
                Object val = f.get(conn);
                if (val instanceof List list && isChannelFutureList(list)) {
                    futures = (List<ChannelFuture>) list;
                    break;
                }
            } catch (NoSuchFieldException ignored) {}
        }

        // Generic scan
        if (futures == null) {
            for (Class<?> c = conn.getClass(); c != null; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    if (!List.class.isAssignableFrom(f.getType())) continue;
                    f.setAccessible(true);
                    Object val;
                    try { val = f.get(conn); } catch (Exception e) { continue; }
                    if (val instanceof List list && isChannelFutureList(list)) {
                        futures = (List<ChannelFuture>) list;
                        break;
                    }
                }
                if (futures != null) break;
            }
        }

        if (futures == null || futures.isEmpty()) return 0;

        // -- Step 4: Inject into each listening (server socket) channel --
        int count = 0;
        for (ChannelFuture cf : futures) {
            Channel serverCh = cf.channel();
            // Only inject into server-side listening channels, not active client connections
            if (!(serverCh instanceof ServerChannel)) continue;
            if (serverCh.pipeline().get(INIT_KEY) != null) { count++; continue; } // already done
            serverCh.pipeline().addFirst(INIT_KEY, new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (msg instanceof Channel child) {
                        child.pipeline().addFirst(DETECT_KEY, new PackDetectHandler(plugin));
                    }
                    ctx.fireChannelRead(msg);
                }
            });
            count++;
        }
        return count;
    }

    @SuppressWarnings("rawtypes")
    private static boolean hasChannelFutureList(Object obj) {
        for (Class<?> c = obj.getClass(); c != null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (!List.class.isAssignableFrom(f.getType())) continue;
                f.setAccessible(true);
                try {
                    Object val = f.get(obj);
                    if (val instanceof List list && isChannelFutureList(list)) return true;
                } catch (Exception ignored) {}
            }
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private static boolean isChannelFutureList(List list) {
        try {
            if (list.isEmpty()) return false;
            return list.get(0) instanceof ChannelFuture;
        } catch (Exception e) { return false; }
    }

    public static void remove() {} // handlers clean up on server shutdown

    // ── Per-connection HTTP detector ─────────────────────────────────────────

    private static final class PackDetectHandler extends ChannelInboundHandlerAdapter {
        private final FarmersDelightPlugin plugin;
        PackDetectHandler(FarmersDelightPlugin p) { this.plugin = p; }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (!(msg instanceof ByteBuf buf)) { ctx.fireChannelRead(msg); return; }

            // Wait until we have at least 4 bytes
            if (buf.readableBytes() < 4) { ctx.fireChannelRead(msg); return; }

            boolean isHttp = buf.getByte(buf.readerIndex())   == 'G'
                          && buf.getByte(buf.readerIndex()+1) == 'E'
                          && buf.getByte(buf.readerIndex()+2) == 'T'
                          && buf.getByte(buf.readerIndex()+3) == ' ';

            ctx.pipeline().remove(this);

            if (isHttp) {
                buf.release();
                byte[] data = plugin.getResourcePackServer().getPackBytes();
                plugin.getLogger().info("[FD] Serving resource pack via HTTP: " + data.length + " bytes");
                if (data.length == 0) {
                    plugin.getLogger().warning("[FD] Pack bytes are empty — rebuild may have failed. Check startup logs.");
                    ctx.close();
                    return;
                }
                String header = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: application/octet-stream\r\n"
                    + "Content-Length: " + data.length + "\r\n"
                    + "Connection: close\r\n\r\n";
                byte[] hdr = header.getBytes(StandardCharsets.US_ASCII);
                ByteBuf resp = ctx.alloc().buffer(hdr.length + data.length);
                resp.writeBytes(hdr);
                resp.writeBytes(data);
                ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
            } else {
                ctx.fireChannelRead(buf);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { ctx.close(); }
    }
}
