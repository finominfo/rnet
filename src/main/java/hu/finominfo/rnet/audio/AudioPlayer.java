package hu.finominfo.rnet.audio;

import hu.finominfo.rnet.common.Globals;

import java.io.File;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * @author User
 */
public class AudioPlayer {

    final ScheduledExecutorService ses;
    private volatile File file;
    private volatile AudioInputStream audioIn;
    private volatile Clip clip;

    public Clip getClip() {
        return clip;
    }

    public AudioPlayer(ScheduledExecutorService ses, String audioFile) {
        this.ses = ses;
        try {
            this.file = new File(audioFile);
            audioIn = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public File getFile() {
        return file;
    }

    public void play(final CompletionHandler<CompletedEvent, Object> comp) {
        try {
            Globals.get().status.setAudio("Playing: " + file.getName());
            clip.start();
            ses.schedule(() -> {
                clip.stop();
                clip.setFramePosition(0);
                Globals.get().status.setAudio(null);
                if (comp != null) {
                    comp.completed(CompletedEvent.AudioPlayFinished, null);
                }
            }, (clip.getMicrosecondLength() / 1000) + 100, TimeUnit.MILLISECONDS);
        } catch (Throwable e1) {
            System.out.println(e1.getMessage());
        }
    }

    public void close() {
        try {
            clip.stop();
            clip.close();
            audioIn.close();
            TimeUnit.SECONDS.sleep(10);
            clip = null;
            audioIn = null;
            file = null;
            System.gc();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AudioPlayer audioPlayer = new AudioPlayer(new ScheduledThreadPoolExecutor(1), "\\ableton\\bir d1.wav");
        audioPlayer.play(null);
    }

}
