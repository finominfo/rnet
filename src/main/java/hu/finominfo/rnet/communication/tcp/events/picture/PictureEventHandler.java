package hu.finominfo.rnet.communication.tcp.events.picture;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.frontend.servant.MessageDisplay;
import hu.finominfo.rnet.frontend.servant.PictureDisplay;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.29.
 */
public class PictureEventHandler extends SimpleChannelInboundHandler<PictureEvent> {
    private final static Logger logger = Logger.getLogger(PictureEventHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PictureEvent msg) throws Exception {
        String ipAndPort = ctx.channel().remoteAddress().toString();
        String ip = Globals.get().getIp(ipAndPort);
        logger.info("PictureEvent arrived from: " + ip);
        new PictureDisplay(msg.getPathAndName(), msg.getSeconds()).display();
    }
}
