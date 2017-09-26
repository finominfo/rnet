package hu.finominfo.rnet.communication.tcp.server;

import hu.finominfo.rnet.common.Globals;
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
        ClientParam clientParam = Globals.get().serverClients.get(ip);
        if (null == clientParam) {
            Globals.get().serverClients.put(ip, new ClientParam(ctx));
        } else {
            clientParam.setContext(ctx);
        }
        Globals.get().addToTasksIfNotExists(TaskToDo.FIND_SERVERS_TO_CONNECT);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String ipAndPort = ctx.channel().remoteAddress().toString();
        logger.info(ipAndPort + " disconnected.");
        String ip = Globals.get().getIp(ipAndPort);
        Globals.get().serverClients.get(ip).setContext(null);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(ctx.channel().remoteAddress().toString(), cause);
    }

}
