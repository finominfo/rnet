package hu.finominfo.rnet.communication.tcp.server;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.MyExceptionHandler;
import hu.finominfo.rnet.taskqueue.TaskToDo;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.23.
 */
public class MyChannelHandler implements ChannelHandler {
    private final static Logger logger = Logger.getLogger(MyChannelHandler.class);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        String ipAndPort = ctx.channel().remoteAddress().toString();
        logger.info(ipAndPort + " connected.");
        String ip = Globals.get().getIp(ipAndPort);
        Globals.get().serverClients.put(ip, new ClientParam(ctx));
        Globals.get().addToTasksIfNotExists(TaskToDo.FIND_SERVERS_TO_CONNECT);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String ipAndPort = ctx.channel().remoteAddress().toString();
        logger.info(ipAndPort + " disconnected.");
        try {
            ctx.channel().close();
            ctx.close();
            String ip = Globals.get().getIp(ipAndPort);
            Globals.get().serverClients.remove(ip);
            Globals.get().connectedServers.remove(ip);
        } catch (Exception e) {
            logger.equals(Utils.getStackTrace(e));
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        MyExceptionHandler.handle(ctx, cause);
    }

}
