package hu.finominfo.rnet.communication.tcp.server;

import hu.finominfo.common.Globals;
import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventDecoder;
import hu.finominfo.rnet.communication.tcp.events.file.FileEventHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;

/**
 * Created by User on 2017.09.17..
 */
public class Server {
    private final static Logger logger = Logger.getLogger(Server.class);
    private final EventLoopGroup parentGroup;
    private final EventLoopGroup childGroup;
    private final ServerBootstrap bootstrap;

    public Server(int port) {
        parentGroup = new NioEventLoopGroup();
        childGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        bootstrap.group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_SNDBUF, Event.BUFFER_SIZE)
                .childOption(ChannelOption.SO_RCVBUF, Event.BUFFER_SIZE)
                .handler(
                        new ChannelInitializer<ServerSocketChannel>() {
                            @Override
                            protected void initChannel(ServerSocketChannel channel)
                                    throws Exception {
                                ChannelPipeline pipeline = channel.pipeline();
                                pipeline.addLast(new EventDecoder());
                                pipeline.addLast(new FileEventHandler());
                            }
                        }
                )
                .childHandler(new ChannelHandler() {
                    @Override
                    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                        String ipAndPort = ctx.channel().remoteAddress().toString();
                        logger.info(ipAndPort + " connected.");
                        String ip = Globals.get().getIp(ipAndPort);
                        ClientParam clientParam = Globals.get().serverClients.get(ip);
                        if (null == clientParam) {
                            Globals.get().serverClients.put(ip, new ClientParam(ctx));
                        } else {
                            clientParam.setContext(ctx);
                        }
                    }

                    @Override
                    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                        String ipAndPort = ctx.channel().remoteAddress().toString();
                        logger.info(ipAndPort + " disconnected.");
                        String ip = Globals.get().getIp(ipAndPort);
                        Globals.get().serverClients.get(ip).setContext(null);

                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        logger.error(ctx.channel().remoteAddress().toString(), cause);
                    }
                })
                .localAddress(new InetSocketAddress("0.0.0.0", port));
    }


    public ChannelFuture bind() {
        return bootstrap.bind();
    }

    public void stop() {
        parentGroup.shutdownGracefully();
        childGroup.shutdownGracefully();
    }
}