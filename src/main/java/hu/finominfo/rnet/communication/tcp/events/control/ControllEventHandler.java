package hu.finominfo.rnet.communication.tcp.events.control;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.communication.tcp.events.control.objects.ShowPicture;
import hu.finominfo.rnet.frontend.servant.PictureDisplay;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.29.
 */
public class ControllEventHandler extends SimpleChannelInboundHandler<ControlEvent> {
    private final static Logger logger = Logger.getLogger(ControllEventHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ControlEvent msg) throws Exception {
        String ipAndPort = ctx.channel().remoteAddress().toString();
        String ip = Globals.get().getIp(ipAndPort);
        logger.info("ControlEvent arrived from: " + ip);
        switch (msg.getControlType()) {
            case SHOW_PICTURE:
                ShowPicture showPicture = (ShowPicture)msg.getControlObject();
                new PictureDisplay(showPicture.getPathAndName(), showPicture.getSeconds()).display();
                break;
            case PLAY_VIDEO:
                break;
            case PLAY_AUDIO:
                break;
            case PLAY_AUDIO_CONTINUOUS:
                break;
            case STOP_AUDIO:
                break;
            case RESET_COUNTER:
                break;
            case START_COUNTER:
                break;
            case STOP_COUNTER:
                break;
        }
    }
}
