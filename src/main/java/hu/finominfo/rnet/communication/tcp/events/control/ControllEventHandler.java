package hu.finominfo.rnet.communication.tcp.events.control;

import hu.finominfo.rnet.audio.AudioPlayer;
import hu.finominfo.rnet.audio.AudioPlayerContinuous;
import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.events.control.objects.PlayAudio;
import hu.finominfo.rnet.communication.tcp.events.control.objects.PlayVideo;
import hu.finominfo.rnet.communication.tcp.events.control.objects.ResetCounter;
import hu.finominfo.rnet.communication.tcp.events.control.objects.ShowPicture;
import hu.finominfo.rnet.frontend.servant.common.PictureDisplay;
import hu.finominfo.rnet.frontend.servant.common.VideoPlayer;
import hu.finominfo.rnet.frontend.servant.common.VideoPlayerContinuous;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

import static hu.finominfo.rnet.common.Utils.closeAudio;

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
                    Utils.showPicture(showPicture);
                    break;
                case PLAY_VIDEO:
                case PLAY_VIDEO_CONTINUOUS:
                    logger.info(msg.getControlType().name() + " arrived: " + ip);
                    PlayVideo playVideo = (PlayVideo) msg.getControlObject();
                    Utils.playVideo(playVideo);
                    break;
                case PLAY_AUDIO:
                    logger.info("PLAY_AUDIO arrived: " + ip);
                    PlayAudio playAudio = (PlayAudio) msg.getControlObject();
                    Utils.playAudio(playAudio);
                    break;
                case PLAY_AUDIO_CONTINUOUS:
                    logger.info("PLAY_AUDIO_CONTINUOUS arrived: " + ip);
                    PlayAudio playAudioContinuous = (PlayAudio) msg.getControlObject();
                    Utils.playAudioContinuous(playAudioContinuous);
                    break;
                case STOP_AUDIO:
                    logger.info("STOP_AUDIO arrived: " + ip);
                    closeAudio();
                    break;
                case STOP_VIDEO:
                    logger.info("STOP_VIDEO arrived: " + ip);
                    break;
                case RESET_COUNTER:
                    logger.info("RESET_COUNTER arrived: " + ip);
                    closeAudio();
                    int minutes = ((ResetCounter) msg.getControlObject()).getMinutes();
                    Globals.get().counter.makeStart();
                    Globals.get().counter.makeStop();
                    Globals.get().counter.milliseconds = minutes * 60_000L;
                    Globals.get().counter.resetButtonPressed();
                    break;
                case START_COUNTER:
                    logger.info("START_COUNTER arrived: " + ip);
                    Utils.startCounterVideo();
                    break;
                case STOP_COUNTER:
                    logger.info("STOP_COUNTER arrived: " + ip);
                    Globals.get().counter.makeStop();
                    break;
            }
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }



}
