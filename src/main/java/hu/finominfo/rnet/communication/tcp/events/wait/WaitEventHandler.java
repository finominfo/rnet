package hu.finominfo.rnet.communication.tcp.events.wait;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import hu.finominfo.rnet.taskqueue.FrontEndTaskToDo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class WaitEventHandler extends SimpleChannelInboundHandler<WaitEvent> {
    private final static Logger logger = Logger.getLogger(WaitEventHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WaitEvent msg) throws Exception {
        logger.info("WaitEvent arrived: " + msg.getTime());
        Globals.get().shouldWait.set(msg.getTime());
    }
}
