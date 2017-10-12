package hu.finominfo.rnet.audio;

import hu.finominfo.rnet.properties.KnockProps;
import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RythmPlayer {

    private final ScheduledExecutorService ses;
    private final KnockProps propertiesReader;
    private final AudioPlayerWrapper audioPlayer;

    public RythmPlayer(ScheduledExecutorService ses, KnockProps propertiesReader, AudioPlayerWrapper audioPlayer) {
        this.ses = ses;
        this.propertiesReader = propertiesReader;
        this.audioPlayer = audioPlayer;
    }

    public void play(final CompletionHandler<CompletedEvent, Object> comp) {
        final List<Integer> rythms = propertiesReader.getRythms();
        final int shortWaiting = propertiesReader.getShortWaiting();
        final int longWaiting = propertiesReader.getLongWaiting();
        final int sizes[] = new int[]{shortWaiting, longWaiting};
        final AtomicInteger counter = new AtomicInteger(0);

        new Runnable() {

            @Override
            public void run() {
                audioPlayer.play(null);
                if (counter.get() < rythms.size()) {
                    int i = rythms.get(counter.getAndIncrement());
//                    System.out.println(sizes[i]);
                    ses.schedule(this, sizes[i], TimeUnit.MILLISECONDS);
                } else {
                    if (comp != null) {
                        comp.completed(CompletedEvent.RythmPlayFinished, null);
                    }
                }
            }

        }.run();
    }

    public static void main(String[] args) {
        String cpuInfo = null;
        try {
            cpuInfo = new String(Files.readAllBytes(Paths.get("/proc/cpuinfo")));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println(cpuInfo);
        ScheduledThreadPoolExecutor ses = new ScheduledThreadPoolExecutor(4);
        KnockProps propertiesReader1 = new KnockProps();
        RythmPlayer mp = new RythmPlayer(ses, propertiesReader1, new AudioPlayerWrapper(ses, propertiesReader1.getKnockVoice()));
        mp.play(null);
        ses.schedule(new Runnable() {
            @Override
            public void run() {
                mp.play(null);
//                ses.schedule(new Runnable() {
//                    @Override
//                    public void run() {
//                        mp.play(null);
//                    }
//                }, 10, TimeUnit.MILLISECONDS);
                ses.schedule(this, 12, TimeUnit.SECONDS);
            }
        }, 5, TimeUnit.SECONDS);
    }

}
