package hu.finominfo.rnet.communication.tcp.client;

import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageAggregator;
import io.netty.handler.codec.memcache.binary.BinaryMemcacheObjectAggregator;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.17..
 */
public class Client {
    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private final String address;
    private final int port;
    public Client(String address, int port) {
        this.address = address;
        this.port = port;
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_SNDBUF, Event.BUFFER_SIZE)
                .option(ChannelOption.SO_RCVBUF, Event.BUFFER_SIZE)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 300_000)
                .handler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel channel)
                                    throws Exception {
                                ChannelPipeline pipeline = channel.pipeline();
                                pipeline.addLast(new EventEncoder());
                                pipeline.addLast(new ExceptionHandler());
                            }
                        }
                );
    }
    public ChannelFuture bind() {
        return bootstrap.connect(new InetSocketAddress(address, port));
    }
    public void stop() {
        group.shutdownGracefully();
    }
}
