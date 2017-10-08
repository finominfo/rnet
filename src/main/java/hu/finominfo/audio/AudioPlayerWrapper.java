package hu.finominfo.audio;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author User
 */
public class AudioPlayerWrapper {

    private static final int NUM_OF_PLAYERS = 2;
    private final AudioPlayer[] audioPlayers;
    private final AtomicInteger currentPlayer;

    public AudioPlayerWrapper(ScheduledExecutorService ses, String audioFile) {
        currentPlayer = new AtomicInteger(-1);
        this.audioPlayers = new AudioPlayer[NUM_OF_PLAYERS];
        for (int i = 0; i < NUM_OF_PLAYERS; i++) {
            audioPlayers[i] = new AudioPlayer(ses, audioFile);
        }
    }

    public void play(final CompletionHandler<CompletedEvent, Object> comp) {
        getNextAudioPLayer().play(comp);
    }

    private AudioPlayer getNextAudioPLayer() {
        while (true) {
            int current = currentPlayer.get();
            int next = current + 1;
            if (next == NUM_OF_PLAYERS) {
                next = 0;
            }
            if (currentPlayer.compareAndSet(current, next)) {
                return audioPlayers[next];
            }
        }
    }
}
