package hu.finominfo.rnet.frontend.servant.common;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.events.control.objects.PlayVideo;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.07.
 */
public class VideoPlayerContinuous {

    private final static Logger logger = Logger.getLogger(VideoPlayerContinuous.class);

    public static VideoPlayerContinuous get() {
        return ourInstance;
    }

    private static VideoPlayerContinuous ourInstance = new VideoPlayerContinuous();

    private final AtomicBoolean playing = new AtomicBoolean(false);
    private volatile Process process = null;
    private volatile PlayVideo lastPlayVideo;

    public void play(final PlayVideo playVideo) {
        File f = new File(playVideo.getPathAndName());
        if (f.exists() && !f.isDirectory() && playing.compareAndSet(false, true)) {
            try {
                lastPlayVideo = playVideo;
                //ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", "omxplayer -o hdmi " + playVideo.getPathAndName());
                ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", "omxplayer " + playVideo.getPathAndName());
                Globals.get().status.setAudio(playVideo.getShortName());
                process = processBuilder.start();
                Globals.get().executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        if (playing.get()) {
                            if (process.isAlive()) {
                                Globals.get().executor.schedule(this, 1, TimeUnit.SECONDS);
                            } else {
                                play(lastPlayVideo);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                logger.error(Utils.getStackTrace(e));
                stop();
            }
        }
    }

    public boolean isPlaying() {
        return playing.get();
    }

    public void stop() {
        if (process != null && process.isAlive()) {
            try {
                process.getOutputStream().write((byte)0x71);
                process.getOutputStream().flush();
                process.getOutputStream().close();
            } catch (Exception e) {
                logger.error(Utils.getStackTrace(e));
                process.destroyForcibly();
            }
        }
        playing.set(false);
        Globals.get().status.setAudio(null);
    }
}
