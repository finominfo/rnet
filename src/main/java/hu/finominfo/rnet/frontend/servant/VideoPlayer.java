package hu.finominfo.rnet.frontend.servant;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.events.control.objects.PlayVideo;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.07.
 */
public class VideoPlayer {

    private final static Logger logger = Logger.getLogger(VideoPlayer.class);

    private final PlayVideo playVideo;
    private volatile Process proc = null;
    private volatile boolean shouldFinish = false;

    public VideoPlayer(PlayVideo playVideo) {
        this.playVideo = playVideo;
    }

    public void play() {
        try {
            //Globals.get().executor.schedule(() -> destroy(), playVideo.getSeconds(), TimeUnit.SECONDS);
            //String[] s = {"/bin/bash", "-c", "/usr/bin/omxplayer " + playVideo.getPathAndName()};
            String s = "omxplayer " + playVideo.getPathAndName();
            Globals.get().status.setVideo("Playing: " + playVideo.getPathAndName());
            proc = Runtime.getRuntime().exec(s);
            Globals.get().executor.submit(new Runnable() {
                @Override
                public void run() {
                    if (proc.isAlive()) {
                        Globals.get().executor.schedule(this, 1, TimeUnit.SECONDS);
                    } else {
                        Globals.get().status.setVideo(null);
                    }
                }
            });
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }

    private class Outer implements Runnable {
        @Override
        public void run() {
            try {
                final String s = "omxplayer " + playVideo.getPathAndName();
                Globals.get().status.setVideo("Playing: " + playVideo.getPathAndName());
                proc = Runtime.getRuntime().exec(s);
                Globals.get().executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        if (proc.isAlive()) {
                            Globals.get().executor.schedule(this, 1, TimeUnit.SECONDS);
                        } else {
                            Globals.get().status.setVideo(null);
                            if (!shouldFinish) {
                                Globals.get().executor.schedule(Outer.this, 1, TimeUnit.SECONDS);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                logger.error(Utils.getStackTrace(e));
            }
        }
    }

    public void continuousPlay() {
        shouldFinish = false;
        try {
            Globals.get().executor.submit(new Outer());
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }

    public void stop() {
        shouldFinish = true;
    }

    /*
    public void destroy() {
        try {
            if (null != proc && proc.isAlive()) {
//                Robot r = new Robot();
//                r.keyPress(KeyEvent.VK_Q);
//                r.keyRelease(KeyEvent.VK_Q);
                //proc.getOutputStream().write((byte)0x71);
                if (proc != null && proc.isAlive()) {
                    String s = "kill -9 " + getPidOfProcess(proc);
                    Runtime.getRuntime().exec(s);
                    //proc.destroyForcibly();
                }
                Globals.get().status.setVideo(null);
            }
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }
    */

    public static synchronized long getPidOfProcess(Process p) {
        long pid = -1;

        try {
            if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
                Field f = p.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = f.getLong(p);
                f.setAccessible(false);
            }
        } catch (Exception e) {
            pid = -1;
        }
        return pid;
    }
}
