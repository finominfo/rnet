package hu.finominfo.rnet.communication.tcp.server;

import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventDecoder;
import hu.finominfo.rnet.communication.tcp.events.status.StatusEventHandler;
import hu.finominfo.rnet.communication.tcp.events.del.DelFileEventHandler;
import hu.finominfo.rnet.communication.tcp.events.dir.DirEventHandler;
import hu.finominfo.rnet.communication.tcp.events.file.FileEventHandler;
import hu.finominfo.rnet.communication.tcp.events.message.MessageEventHandler;
import hu.finominfo.rnet.communication.tcp.events.control.ControllEventHandler;
import hu.finominfo.rnet.communication.tcp.events.wait.WaitEventHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
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
                .childHandler( // Ez így biztos jó.
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel channel)
                                    throws Exception {
                                ChannelPipeline pipeline = channel.pipeline();
                                pipeline.addLast(new IdleStateHandler(300, 0, 0));
                                pipeline.addLast(new MyChannelHandler());
                                pipeline.addLast(new EventDecoder());
                                pipeline.addLast(new FileEventHandler());
                                pipeline.addLast(new ControllEventHandler());
                                pipeline.addLast(new DirEventHandler());
                                pipeline.addLast(new DelFileEventHandler());
                                pipeline.addLast(new MessageEventHandler());
                                pipeline.addLast(new StatusEventHandler());
                                pipeline.addLast(new WaitEventHandler());
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
