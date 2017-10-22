package hu.finominfo.rnet.frontend.servant.common;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.events.control.objects.PlayVideo;
import org.apache.log4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.07.
 */
public class VideoPlayer {

    private final static Logger logger = Logger.getLogger(VideoPlayer.class);

    public static VideoPlayer get() {
        return ourInstance;
    }

    private static VideoPlayer ourInstance = new VideoPlayer();

    private final AtomicBoolean playing = new AtomicBoolean(false);
    private volatile Process proc = null;


    public void play(final PlayVideo playVideo) {
        File f = new File(playVideo.getPathAndName());
        if(f.exists() && !f.isDirectory() && playing.compareAndSet(false, true)) {
            try {
                String s = "omxplayer " + playVideo.getPathAndName();
                Globals.get().status.setVideo("Playing: " + playVideo.getPathAndName());
                proc = Runtime.getRuntime().exec(s);
                Globals.get().executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        if (proc.isAlive()) {
                            Globals.get().executor.schedule(this, 1, TimeUnit.SECONDS);
                        } else {
                            finish();
                        }
                    }
                });
            } catch (Exception e) {
                logger.error(Utils.getStackTrace(e));
                finish();
            }
        }
    }

    public boolean isPlaying() {
        return playing.get();
    }

    private void finish() {
        playing.set(false);
        Globals.get().status.setVideo(null);
    }

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
