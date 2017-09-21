package hu.finominfo.audio;

import java.io.File;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 *
 * @author User
 */
public class AudioPlayer {

    final ScheduledExecutorService ses;
    private final File file;
    private final AudioInputStream audioIn;
    private final Clip clip;

    public AudioPlayer(ScheduledExecutorService ses, String audioFile) {
        this.ses = ses;
        this.file = new File(audioFile);
        try {
            audioIn = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void play(final CompletionHandler<CompletedEvent, Object> comp) {
        try {
            clip.start();
            ses.schedule(() -> {
                clip.stop();
                clip.setFramePosition(0);
                if (comp != null) {
                    comp.completed(CompletedEvent.AudioPlayFinished, null);
                }
            }, (clip.getMicrosecondLength() / 1000) + 100, TimeUnit.MILLISECONDS);
        } catch (Throwable e1) {
            System.out.println(e1.getMessage());
        }
    }

    public void close() {
        clip.close();
        try {
            audioIn.close();
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
