package hu.finominfo.rnet.communication.udp.in;

import hu.finominfo.rnet.common.Globals;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.17..
 */
public class ConnectionMonitor {
    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    public ConnectionMonitor(int port) {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(
                        new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(Channel channel)
                                    throws Exception {
                                ChannelPipeline pipeline = channel.pipeline();
                                pipeline.addLast(new ConnectionDecoder());
                                pipeline.addLast(new ConnectionHandler());
                            }
                        }
                )
                .localAddress(new InetSocketAddress("0.0.0.0", port));
        Globals.get().monitor = this;
    }
    public ChannelFuture bind() {
        return bootstrap.bind();
    }
    public void stop() {
        group.shutdownGracefully();
    }
}
