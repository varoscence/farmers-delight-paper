package com.ashblossom.farmersdelight.resourcepack;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Injects a lightweight HTTP handler into Paper's Netty pipeline on the game port.
 * HTTP GET requests (from Minecraft clients downloading the resource pack) are
 * intercepted and answered with the pack ZIP before Minecraft sees them.
 * All normal game connections are unaffected.
 */
public class PackNettyInjector {

    private static final String INIT_HANDLER  = "fd_pack_init";
    private static final String DETECT_HANDLER = "fd_pack_detect";

    public static void inject(FarmersDelightPlugin plugin) {
        // Run on next tick so the server channels are bound before we try to find them
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                doInject(plugin);
            } catch (Exception e) {
                plugin.getLogger().warning(
                    "[FD] Could not inject resource pack handler into Netty: " + e.getMessage());
                plugin.getLogger().warning(
                    "[FD] Resource pack will not be served. If items look wrong, set a valid " +
                    "resource-pack-url in plugins/FarmersDelight/config.yml as fallback.");
            }
        });
    }

    public static void remove() {
        // Channel handlers clean themselves up when the server shuts down.
    }

    // ─────────────────────────────────────────────────────────────────────────

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void doInject(FarmersDelightPlugin plugin) throws Exception {
        // 1. Get MinecraftServer from CraftServer (stable public method)
        Object nmsServer = Bukkit.getServer().getClass()
            .getMethod("getServer").invoke(Bukkit.getServer());

        // 2. Find the ServerConnectionListener (has the listening ChannelFuture list)
        Object serverConn = findServerConnection(nmsServer);
        if (serverConn == null) throw new RuntimeException("ServerConnection not found");

        // 3. Find the List<ChannelFuture> inside ServerConnectionListener
        List<ChannelFuture> channelFutures = findChannelFutures(serverConn);
        if (channelFutures == null || channelFutures.isEmpty())
            throw new RuntimeException("Listening channels not found");

        // 4. For each server channel (usually one), add a handler that fires on each
        //    new incoming child connection and installs the pack detector.
        for (ChannelFuture future : channelFutures) {
            Channel serverChannel = future.channel();
            if (serverChannel.pipeline().get(INIT_HANDLER) != null) continue; // already injected

            serverChannel.pipeline().addFirst(INIT_HANDLER, new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (msg instanceof Channel child) {
                        child.pipeline().addFirst(DETECT_HANDLER, new PackDetectHandler(plugin));
                    }
                    ctx.fireChannelRead(msg);
                }
            });
        }
        plugin.getLogger().info("[FD] Resource pack injected on port " + plugin.getServer().getPort());
    }

    // ─── Reflection helpers ───────────────────────────────────────────────────

    private static Object findServerConnection(Object nmsServer) throws Exception {
        // Try getConnection() — the Mojang-mapped method name, present in Paper 1.17+
        for (Method m : nmsServer.getClass().getMethods()) {
            if (m.getParameterCount() == 0 && m.getName().equals("getConnection")) {
                Object result = m.invoke(nmsServer);
                if (result != null) return result;
            }
        }
        // Fall back: search fields of MinecraftServer for an object that holds
        // a List<ChannelFuture>
        for (Class<?> cls = nmsServer.getClass(); cls != null; cls = cls.getSuperclass()) {
            for (Field f : cls.getDeclaredFields()) {
                f.setAccessible(true);
                Object val;
                try { val = f.get(nmsServer); } catch (Exception e) { continue; }
                if (val == null || val.getClass().getName().startsWith("java.")) continue;
                if (findChannelFutures(val) != null) return val;
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<ChannelFuture> findChannelFutures(Object obj) throws Exception {
        for (Class<?> cls = obj.getClass(); cls != null; cls = cls.getSuperclass()) {
            for (Field f : cls.getDeclaredFields()) {
                if (!List.class.isAssignableFrom(f.getType())) continue;
                f.setAccessible(true);
                Object val;
                try { val = f.get(obj); } catch (Exception e) { continue; }
                if (!(val instanceof List list)) continue;
                try {
                    if (!list.isEmpty() && list.get(0) instanceof ChannelFuture) {
                        return (List<ChannelFuture>) list;
                    }
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    // ─── Inner handler ────────────────────────────────────────────────────────

    private static final class PackDetectHandler extends ChannelInboundHandlerAdapter {
        private final FarmersDelightPlugin plugin;

        PackDetectHandler(FarmersDelightPlugin plugin) { this.plugin = plugin; }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (!(msg instanceof ByteBuf buf)) { ctx.fireChannelRead(msg); return; }

            boolean isHttp = buf.readableBytes() >= 4
                && buf.getByte(buf.readerIndex())   == 'G'
                && buf.getByte(buf.readerIndex()+1) == 'E'
                && buf.getByte(buf.readerIndex()+2) == 'T'
                && buf.getByte(buf.readerIndex()+3) == ' ';

            ctx.pipeline().remove(this);

            if (isHttp) {
                buf.release();
                servePackAndClose(ctx);
            } else {
                ctx.fireChannelRead(buf);
            }
        }

        private void servePackAndClose(ChannelHandlerContext ctx) {
            byte[] data = plugin.getResourcePackServer().getPackBytes();
            String header = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: application/octet-stream\r\n"
                + "Content-Length: " + data.length + "\r\n"
                + "Connection: close\r\n\r\n";
            byte[] hdr = header.getBytes(StandardCharsets.US_ASCII);
            ByteBuf resp = ctx.alloc().buffer(hdr.length + data.length);
            resp.writeBytes(hdr);
            resp.writeBytes(data);
            ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }
}
