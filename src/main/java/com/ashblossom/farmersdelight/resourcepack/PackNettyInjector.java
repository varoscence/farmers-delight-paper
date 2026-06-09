package com.ashblossom.farmersdelight.resourcepack;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import net.kyori.adventure.key.Key;

import java.nio.charset.StandardCharsets;

public class PackNettyInjector {

    private static final Key KEY = Key.key("farmersdelight", "pack_server");

    public static void inject(FarmersDelightPlugin plugin) {
        io.papermc.paper.network.ChannelInitializeListenerHolder.addListener(KEY, channel ->
            channel.pipeline().addFirst("fd_pack_detector", new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (!(msg instanceof ByteBuf buf)) {
                        ctx.fireChannelRead(msg);
                        return;
                    }

                    boolean isHttp = buf.readableBytes() >= 4
                        && buf.getByte(buf.readerIndex())   == 'G'
                        && buf.getByte(buf.readerIndex()+1) == 'E'
                        && buf.getByte(buf.readerIndex()+2) == 'T'
                        && buf.getByte(buf.readerIndex()+3) == ' ';

                    ctx.pipeline().remove(this);

                    if (isHttp) {
                        buf.release();
                        serve(ctx, plugin);
                    } else {
                        ctx.fireChannelRead(buf);
                    }
                }

                private void serve(ChannelHandlerContext ctx, FarmersDelightPlugin plugin) {
                    byte[] data = plugin.getResourcePackServer().getPackBytes();
                    String header = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: application/octet-stream\r\n"
                        + "Content-Length: " + data.length + "\r\n"
                        + "Connection: close\r\n\r\n";
                    byte[] headerBytes = header.getBytes(StandardCharsets.US_ASCII);
                    ByteBuf resp = ctx.alloc().buffer(headerBytes.length + data.length);
                    resp.writeBytes(headerBytes);
                    resp.writeBytes(data);
                    ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
                }
            })
        );
        plugin.getLogger().info("Resource pack injected on game port " + plugin.getServer().getPort());
    }

    public static void remove() {
        io.papermc.paper.network.ChannelInitializeListenerHolder.removeListener(KEY);
    }
}
