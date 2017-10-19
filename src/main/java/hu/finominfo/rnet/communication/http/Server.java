package hu.finominfo.rnet.communication.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

/**
 * Created by kalman.kovacs@globessey.local on 2017.10.19.
 */
public class Server {

    private volatile ChannelFuture channel;
    private final EventLoopGroup masterGroup;
    private final EventLoopGroup slaveGroup;

    public Server() {
        masterGroup = new NioEventLoopGroup();
        slaveGroup = new NioEventLoopGroup();
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });

        try {
            final ServerBootstrap bootstrap =
                    new ServerBootstrap()
                            .group(masterGroup, slaveGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                public void initChannel(final SocketChannel ch)
                                        throws Exception {
                                    ch.pipeline().addLast("codec", new HttpServerCodec());
                                    ch.pipeline().addLast("aggregator", new HttpObjectAggregator(512 * 1024));
                                    ch.pipeline().addLast("request",
                                            new ChannelInboundHandlerAdapter() {
                                                @Override
                                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                                    if (msg instanceof FullHttpRequest) {
                                                        final FullHttpRequest request = (FullHttpRequest) msg;
                                                        System.out.println(request.uri());
                                                        final String responseMessage = "Hello from Netty!";
                                                        FullHttpResponse response = new DefaultFullHttpResponse(
                                                                HttpVersion.HTTP_1_1,
                                                                HttpResponseStatus.OK,
                                                                Unpooled.wrappedBuffer(responseMessage.getBytes())
                                                        );
                                                        if (HttpHeaders.isKeepAlive(request)) {
                                                            response.headers().set(
                                                                    HttpHeaders.Names.CONNECTION,
                                                                    HttpHeaders.Values.KEEP_ALIVE
                                                            );
                                                        }
                                                        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
                                                        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, responseMessage.length());
                                                        ctx.writeAndFlush(response);
                                                    } else {
                                                        super.channelRead(ctx, msg);
                                                    }
                                                }

                                                @Override
                                                public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                                    ctx.flush();
                                                }

                                                @Override
                                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                                    ctx.writeAndFlush(new DefaultFullHttpResponse(
                                                            HttpVersion.HTTP_1_1,
                                                            HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                            Unpooled.wrappedBuffer(cause.getMessage().getBytes())
                                                    ));
                                                }
                                            });
                                }
                            })
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);
            channel = bootstrap.bind(8080).sync();
        } catch (Exception e) {
        }
    }

    public void shutdown() {
        slaveGroup.shutdownGracefully();
        masterGroup.shutdownGracefully();

        try {
            channel.channel().closeFuture().sync();
        } catch (Exception e) {
        }
    }

    public static void main(String[] args) {
        new Server().start();
    }
}
