package hu.finominfo.rnet.communication.udp.in;

import hu.finominfo.common.Globals;
import hu.finominfo.rnet.communication.udp.Connection;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.17..
 */
public class ConnectionHandler extends SimpleChannelInboundHandler<Connection> {
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
        System.out.println(msg.getServerIp() + ":" + msg.getServerPort() + " connections size: " + Globals.get().connections.size());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}