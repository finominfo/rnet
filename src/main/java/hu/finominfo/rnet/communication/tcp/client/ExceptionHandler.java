package hu.finominfo.rnet.communication.tcp.client;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.MyExceptionHandler;
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
        MyExceptionHandler.handle(ctx, cause);
    }
}