package hu.finominfo.rnet.communication.tcp.events.control;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.events.control.objects.PlayVideo;
import hu.finominfo.rnet.communication.tcp.events.control.objects.ShowPicture;
import hu.finominfo.rnet.frontend.servant.PictureDisplay;
import hu.finominfo.rnet.frontend.servant.VideoPlayer;
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
        try {
            String ipAndPort = ctx.channel().remoteAddress().toString();
            String ip = Globals.get().getIp(ipAndPort);
            logger.info("ControlEvent arrived from: " + ip);
            switch (msg.getControlType()) {
                case SHOW_PICTURE:
                    logger.info("SHOW_PICTURE arrived: " + ip);
                    ShowPicture showPicture = (ShowPicture) msg.getControlObject();
                    final PictureDisplay pictureDisplay = new PictureDisplay(showPicture.getPathAndName(), showPicture.getSeconds());
                    Globals.get().executor.submit(() -> pictureDisplay.display());
                    break;
                case PLAY_VIDEO:
                    logger.info("PLAY_VIDEO arrived: " + ip);
                    PlayVideo playVideo = (PlayVideo) msg.getControlObject();
                    final VideoPlayer videoPlayer = new VideoPlayer(playVideo);
                    Globals.get().executor.submit(() -> videoPlayer.play());
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
        }catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }
}
