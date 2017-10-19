package hu.finominfo.rnet.communication.http;

import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.properties.Props;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import org.apache.log4j.Logger;

/**
 * Created by kalman.kovacs@globessey.local on 2017.10.19.
 */
public class HttpServer {

    private final static Logger logger = Logger.getLogger(HttpServer.class);
    private volatile ChannelFuture channel;
    private final EventLoopGroup masterGroup;
    private final EventLoopGroup slaveGroup;

    public HttpServer() {
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
                                    ch.pipeline().addLast("request", new MyChannelInboundHandlerAdapter() {
                                    });
                                }
                            })
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);
            channel = bootstrap.bind(Props.get().getHttpPort()).sync();
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }

    public void shutdown() {
        slaveGroup.shutdownGracefully();
        masterGroup.shutdownGracefully();
        try {
            channel.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }

    public static void main(String[] args) {
        new HttpServer().start();
    }
}
