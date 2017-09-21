package hu.finominfo.rnet.communication.connection.out;

import hu.finominfo.rnet.communication.Interface;
import hu.finominfo.rnet.communication.connection.Connection;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;

/**
 * Created by User on 2017.09.17..
 */
public class ConnectionBroadcaster {
    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private final Channel ch;

    public ConnectionBroadcaster(int port) {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ConnectionEncoder(new InetSocketAddress("255.255.255.255", port)));
        ch = bootstrap.bind(0).channel();
    }

    public void send(ChannelFutureListener futureListener) {
        int serverPort = 10001;
        for (final String ip : Interface.ips) {
            ChannelFuture channelFuture = ch.writeAndFlush(new Connection(ip, serverPort));
            if (null!= futureListener) {
                channelFuture.addListener(futureListener);
            }
        }
    }

    public void stop () {
        group.shutdownGracefully();
    }

}
