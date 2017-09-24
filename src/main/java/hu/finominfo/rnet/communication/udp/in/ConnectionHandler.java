package hu.finominfo.rnet.communication.udp.in;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.TaskToDo;
import hu.finominfo.rnet.communication.udp.Connection;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.17..
 */
public class ConnectionHandler extends SimpleChannelInboundHandler<Connection> {
    private final static Logger logger = Logger.getLogger(ConnectionHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Connection msg) throws Exception {
        boolean found = false;
        for (Connection connection : Globals.get().connections) {
            if (connection.equals(msg)) {
                found = true;
                break;
            }
        }
        if (!found) {
            Globals.get().connections.add(msg);
        }
        logger.info("Connection object arrived: " + msg.getServerIp() + ":" + msg.getServerPort() + " connections size: " + Globals.get().connections.size());
        if (Globals.get().isTasksEmpty()) {
            Globals.get().addToTasksIfNotExists(TaskToDo.START_SERVER);
            Globals.get().addToTasksIfNotExists(TaskToDo.FIND_SERVERS_TO_CONNECT);
            Globals.get().addToTasksIfNotExists(TaskToDo.SEND_MAC_ADRESSES);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
