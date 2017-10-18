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
import hu.finominfo.rnet.properties.Props;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

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
                    PictureDisplay.get().display(showPicture.getPathAndName(), showPicture.getSeconds());
                    break;
                case PLAY_VIDEO:
                case PLAY_VIDEO_CONTINUOUS:
                    logger.info(msg.getControlType().name() + " arrived: " + ip);
                    PlayVideo playVideo = (PlayVideo) msg.getControlObject();
                    VideoPlayer.get().play(playVideo);
                    break;
                case PLAY_AUDIO:
                    logger.info("PLAY_AUDIO arrived: " + ip);
                    PlayAudio playAudio = (PlayAudio) msg.getControlObject();
                    closeAudio();
                    Globals.get().audioPlayer = new AudioPlayer(Globals.get().executor, playAudio.getPathAndName());
                    Globals.get().audioPlayer.play(null);
                    Globals.get().executor.schedule(() -> {
                        Globals.get().audioPlayer.close();
                        Globals.get().audioPlayer = null;
                    }, (Globals.get().audioPlayer.getClip().getMicrosecondLength() / 1000) + 200, TimeUnit.MILLISECONDS);
                    break;
                case PLAY_AUDIO_CONTINUOUS:
                    logger.info("PLAY_AUDIO_CONTINUOUS arrived: " + ip);
                    PlayAudio playAudioContinuous = (PlayAudio) msg.getControlObject();
                    closeAudio();
                    Globals.get().audioPlayerContinuous = new AudioPlayerContinuous(Globals.get().executor, playAudioContinuous.getPathAndName());
                    Globals.get().audioPlayerContinuous.play(null);
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
                    int minutes = ((ResetCounter) msg.getControlObject()).getMinutes();
                    Globals.get().counter.makeStart();
                    Globals.get().counter.makeStop();
                    Globals.get().counter.milliseconds = minutes * 60_000L;
                    Globals.get().counter.resetButtonPressed();
                    break;
                case START_COUNTER:
                    logger.info("START_COUNTER arrived: " + ip);
                    String videoPlayAtCounterStart = Props.get().getVideoPlayAtCounterStart();
                    if (videoPlayAtCounterStart != null && !videoPlayAtCounterStart.isEmpty()) {
                        PlayVideo playVideo2 = new PlayVideo(Globals.videoFolder, videoPlayAtCounterStart, 30);
                        VideoPlayer.get().play(playVideo2);
                        Globals.get().executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                if (VideoPlayer.get().isPlaying()) {
                                    Globals.get().executor.schedule(this, 1, TimeUnit.SECONDS);
                                } else {
                                    Globals.get().counter.makeStart();
                                }
                            }
                        });
                    } else {
                        Globals.get().counter.makeStart();
                    }
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

    private void closeAudio() {
        AudioPlayer audioPlayer = Globals.get().audioPlayer;
        if (audioPlayer != null) {
            audioPlayer.close();
            Globals.get().audioPlayer = null;
        }
        AudioPlayerContinuous audioPlayerContinuous = Globals.get().audioPlayerContinuous;
        if (audioPlayerContinuous != null) {
            audioPlayerContinuous.stop();
            audioPlayerContinuous.close();
            Globals.get().audioPlayerContinuous = null;
        }
    }
}
