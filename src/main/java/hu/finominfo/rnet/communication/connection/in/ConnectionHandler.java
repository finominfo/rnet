package hu.finominfo.rnet.communication.connection.in;

import hu.finominfo.rnet.communication.Interface;
import hu.finominfo.rnet.communication.connection.Connection;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.17..
 */
public class ConnectionHandler extends SimpleChannelInboundHandler<Connection> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Connection msg) throws Exception {
        boolean found = false;
        for (Connection connection : Interface.connections) {
            if (connection.equals(msg)) {
                found = true;
                break;
            }
        }
        if (!found) {
            Interface.connections.add(msg);
        }
        //TODO: Itt kell elindítani a TCP kapcsolatot visszafelé
        System.out.println(msg.getServerIp() + ":" + msg.getServerPort() + " connections size: " + Interface.connections.size());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
