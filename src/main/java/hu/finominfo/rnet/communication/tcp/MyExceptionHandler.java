package hu.finominfo.rnet.communication.tcp;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

/**
 * Created by kalman.kovacs@globessey.local on 2018.03.06.
 */
public class MyExceptionHandler {
    private final static Logger logger = Logger.getLogger(MyExceptionHandler.class);
    public static void handle(ChannelHandlerContext ctx, Throwable cause) {
        String ipAndPort = ctx.channel().remoteAddress().toString();
        logger.error(ipAndPort + " error.", cause);
        String ip = Globals.get().getIp(ipAndPort);
        try {
            ctx.channel().close();
            ctx.close();
        } catch (Exception e) {
            logger.equals(Utils.getStackTrace(e));
        }
        Globals.get().serverClients.remove(ip);
        Globals.get().connectedServers.remove(ip);
    }
}
