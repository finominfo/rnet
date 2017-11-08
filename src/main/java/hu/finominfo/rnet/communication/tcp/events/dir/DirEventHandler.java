package hu.finominfo.rnet.communication.tcp.events.dir;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import hu.finominfo.rnet.frontend.controller.FrontEnd;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.29.
 */
public class DirEventHandler extends SimpleChannelInboundHandler<DirEvent> {
    private final static Logger logger = Logger.getLogger(DirEventHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DirEvent msg) throws Exception {
        String ipAndPort = ctx.channel().remoteAddress().toString();
        String ip = Globals.get().getIp(ipAndPort);
        logger.info("DirEvent arrived: " + ip);
        try {
            ClientParam clientParam = Globals.get().serverClients.get(ip);
            clientParam.setStatus(msg.getStatus());
            boolean shouldRefreshAll = false;
            if ((clientParam.getDefAudio() != null && !clientParam.getDefAudio().equals(msg.getDefAudio())) ||
                    (clientParam.getDefAudio() != null && !clientParam.getDefVideo().equals(msg.getDefVideo()))) {
                shouldRefreshAll = true;
            }
            clientParam.setDefAudio(msg.getDefAudio());
            clientParam.setDefVideo(msg.getDefVideo());
            msg.getDirs().entrySet().stream().forEach(entry -> clientParam.getDirs().put(entry.getKey(), entry.getValue()));
            if (shouldRefreshAll) {
                Globals.get().getFrontEnd().shouldRefreshAll();
            }
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }
}

