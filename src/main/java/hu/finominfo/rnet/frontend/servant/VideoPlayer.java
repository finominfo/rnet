package hu.finominfo.rnet.frontend.servant;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.events.control.objects.PlayVideo;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.07.
 */
public class VideoPlayer {

    private final static Logger logger = Logger.getLogger(VideoPlayer.class);

    private final PlayVideo playVideo;
    private volatile Process proc = null;

    public VideoPlayer(PlayVideo playVideo) {
        this.playVideo = playVideo;
    }

    public void play() {
        try {
            Globals.get().executor.schedule(() -> destroy(), playVideo.getSeconds(), TimeUnit.SECONDS);
            //String[] s = {"/bin/bash", "-c", "/usr/bin/omxplayer " + playVideo.getPathAndName()};
            String s = "omxplayer " + playVideo.getPathAndName();
            proc = Runtime.getRuntime().exec(s);
            proc.waitFor();
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }

    private void destroy() {
        try {
            if (null != proc && proc.isAlive()) {
                proc.destroy();
            }
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }
}
