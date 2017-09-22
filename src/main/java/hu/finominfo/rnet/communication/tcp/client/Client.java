package hu.finominfo.rnet.communication.tcp.client;

import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.17..
 */
public class Client {
    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    public Client(String address, int port) {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_SNDBUF, Event.BUFFER_SIZE)
                .option(ChannelOption.SO_RCVBUF, Event.BUFFER_SIZE)
                .handler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel channel)
                                    throws Exception {
                                ChannelPipeline pipeline = channel.pipeline();
                                pipeline.addLast(new EventEncoder(new InetSocketAddress(address, port)));
                            }
                        }
                )
                .localAddress(new InetSocketAddress(address, port));
    }
    public ChannelFuture bind() {
        return bootstrap.bind();
    }
    public void stop() {
        group.shutdownGracefully();
    }
}
