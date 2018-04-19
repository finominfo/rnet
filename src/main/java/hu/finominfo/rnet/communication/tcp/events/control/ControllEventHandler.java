package hu.finominfo.rnet.communication.tcp.events.control;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.events.control.objects.*;
import hu.finominfo.rnet.communication.tcp.events.dir.media.TimeOrder;
import hu.finominfo.rnet.communication.tcp.events.dir.media.Types;
import hu.finominfo.rnet.database.H2KeyValue;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static hu.finominfo.rnet.common.Utils.closeAudio;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.29.
 */
public class ControllEventHandler extends SimpleChannelInboundHandler<ControlEvent> {
    private final static Logger logger = Logger.getLogger(ControllEventHandler.class);
    private final static ConcurrentMap<String, Long> defAudioHits = new ConcurrentHashMap<>();
    private final static ConcurrentMap<String, Long> defVideoHits = new ConcurrentHashMap<>();
    private final static ConcurrentMap<String, Long> defPictureHits = new ConcurrentHashMap<>();


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
                    if (showPicture.getSeconds() == 0) {
                        Map<TimeOrder, String> picTypes = Types.getSavedType("PICTURE");
                        Globals.get().types.setNext(picTypes, showPicture.getShortName());
                        Types.setNext(picTypes, showPicture.getShortName());
                        Globals.get().types.save();
                    } else {
                        Utils.showPicture(showPicture);
                    }
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
                    String audName = ((Name) msg.getControlObject()).getName();
                    if (audName != null && !audName.isEmpty()) {
                        checkHits(defAudioHits, audName, Types.getSaved().getAudioTypes().get(TimeOrder.DURING));
                    } else {
                        logger.warn("No audName in STOP_AUDIO");
                    }
                    closeAudio();
                    break;
                case STOP_VIDEO:
                    logger.info("STOP_VIDEO arrived: " + ip);
                    String vidName = ((Name) msg.getControlObject()).getName();
                    if (vidName != null && !vidName.isEmpty()) {
                        checkHits(defVideoHits, vidName, Types.getSaved().getAudioTypes().get(TimeOrder.BEFORE));
                    } else {
                        logger.warn("No vidName in STOP_VIDEO");
                    }
                    break;
                case RESET_COUNTER:
                    logger.info("RESET_COUNTER arrived: " + ip);
                    closeAudio();
                    int minutes = ((ResetCounter) msg.getControlObject()).getMinutes();
                    if (minutes == 0 || minutes > 30) {
                        //Globals.get().counter.makeStart();
                        Globals.get().counter.makeStop();
                        if (minutes == 0) {
                            Globals.get().counter.milliseconds = Integer.valueOf(H2KeyValue.getValue(H2KeyValue.COUNTER)) * 60_000L;
                        } else {
                            Globals.get().counter.milliseconds = minutes * 60_000L;
                            H2KeyValue.set(H2KeyValue.COUNTER, String.valueOf(minutes));
                        }
                        Globals.get().counter.resetButtonPressed();
                    } else {
                        Globals.get().counter.addMinutesIfPossible(minutes);
                    }
                    break;
                case START_COUNTER:
                    logger.info("START_COUNTER arrived: " + ip);
                    Utils.playMediaBeforeStartCounter();
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

    private void checkHits(ConcurrentMap<String, Long> defHits, String name, String key) {
        Long value = defHits.get(name);
        Long now = System.currentTimeMillis();
        if (value == null) {
            final Long newValue = new Long(now);
            value = defHits.putIfAbsent(name, newValue);
            if (value == null) {
                value = newValue;
            }
        }
        long hits = (value >> 60) + 1;
        long time = value & 0xfffffffffffffffL;
        if (now - time < 15_000L) {
            if (hits > 2) {
                defHits.remove(name);
                H2KeyValue.set(key, name);
                logger.info("checkHits name set: " + name);
            } else {
                long newValue = (hits << 60) | time;
                defHits.put(name, newValue);
            }
        } else {
            long newValue = (1 << 60) | now;
            defHits.put(name, newValue);
        }
    }


}
