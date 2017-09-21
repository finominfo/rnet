package hu.finominfo.rnet.communication.data.server;

import hu.finominfo.rnet.communication.data.events.Event;
import hu.finominfo.rnet.communication.data.events.EventDecoder;
import hu.finominfo.rnet.communication.data.events.file.FileEventHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * Created by User on 2017.09.17..
 */
public class Server {
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
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel channel)
                                    throws Exception {
                                ChannelPipeline pipeline = channel.pipeline();
                                //pipeline.addLast(new EventEncoder(InetSocketAddress));
                                pipeline.addLast(new EventDecoder());
                                pipeline.addLast(new FileEventHandler());
                                //pipeline.addLast(new EventHandler());
                            }
                        }
                )
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
