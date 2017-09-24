package hu.finominfo.rnet.communication.tcp.client;

import hu.finominfo.rnet.common.Globals;
import io.netty.channel.*;
import org.apache.log4j.Logger;

import java.net.SocketAddress;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.24.
 */
public class ExceptionHandler extends ChannelDuplexHandler {

    private final static Logger logger = Logger.getLogger(ExceptionHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String ipAndPort = ctx.channel().remoteAddress().toString();
        logger.error(ipAndPort + " error.", cause);
        String ip = Globals.get().getIp(ipAndPort);
        Globals.get().serverClients.remove(ip);
        Globals.get().connectedServers.remove(ip);

        // Uncaught exceptions from inbound handlers will propagate up to this handler
    }

//    @Override
//    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
//        ctx.connect(remoteAddress, localAddress, promise.addListener(future -> {
//            if (!future.isSuccess()) {
//                // Handle connect exception here...
//            }
//        }));
//    }

//    @Override
//    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
//        ctx.write(msg, promise.addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) {
//                if (!future.isSuccess()) {
//                    // Handle write exception here...
//                }
//            }
//        }));
//    }

    // ... override more outbound methods to handle their exceptions as well
}