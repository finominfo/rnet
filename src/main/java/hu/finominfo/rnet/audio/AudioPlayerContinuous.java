package hu.finominfo.rnet.audio;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author User
 */
public class AudioPlayerContinuous extends AudioPlayer {

    private volatile boolean running = true;

    public AudioPlayerContinuous(ScheduledExecutorService ses, String audioFile) {
        super(ses, audioFile);
    }

    @Override
    public void play(final CompletionHandler<CompletedEvent, Object> comp) {
        ses.schedule(() -> {
            AudioPlayerContinuous.super.play(new CompletionHandler<CompletedEvent, Object>() {
                @Override
                public void completed(CompletedEvent result, Object attachment) {
                    if (running) {
                        AudioPlayerContinuous.this.play(comp);
                    }
                }
                
                @Override
                public void failed(Throwable exc, Object attachment) {
                    if (running) {
                        AudioPlayerContinuous.this.play(comp);
                    }
                }
            });
        }, 100, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        running = false;
    }

}
