package hu.finominfo.rnet.communication.tcp.events.dir;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.29.
 */
public class RichDirEventHandler extends SimpleChannelInboundHandler<RichDirEvent> {
    private final static Logger logger = Logger.getLogger(RichDirEventHandler.class);
    private final static AtomicLong dirCounter = new AtomicLong(0);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RichDirEvent msg) throws Exception {
        String ipAndPort = ctx.channel().remoteAddress().toString();
        String ip = Globals.get().getIp(ipAndPort);
        long currentCounterValue = dirCounter.incrementAndGet();
        if ((dirCounter.incrementAndGet() & 0x3f) == 0) {
            logger.info(currentCounterValue + ". DirEvent arrived (last ip: " + ip + ")");
            System.out.println(msg.getTypes());
        }
        try {
            ClientParam clientParam = Globals.get().serverClients.get(ip);
            clientParam.setStatus(msg.getStatus());
            clientParam.setTypes(msg.getTypes());
            msg.getDirs().entrySet().stream().forEach(entry -> clientParam.getDirs().put(entry.getKey(), entry.getValue()));
            Globals.get().getFrontEnd().shouldRefreshAll();
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }
}

