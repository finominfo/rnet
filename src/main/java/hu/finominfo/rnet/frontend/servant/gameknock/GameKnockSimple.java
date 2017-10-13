package hu.finominfo.rnet.frontend.servant.gameknock;

import hu.finominfo.rnet.audio.CompletedEvent;
import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.frontend.servant.gameknock.io.HandlingIO;
import hu.finominfo.rnet.frontend.servant.gameknock.io.IOActionType;
import hu.finominfo.rnet.audio.AudioPlayer;
import hu.finominfo.rnet.audio.AudioPlayerWrapper;
import hu.finominfo.rnet.properties.KnockProps;

import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author kalman.kovacs@gmail.com
 */
public final class GameKnockSimple implements CompletionHandler<CompletedEvent, Object>, Runnable {

    private static final int SHORT = 0;
//    private static final int LONG = 1;

    private final List<Integer> rhythms = new ArrayList<>();

    static volatile long lastKnock = 0;

    public final ScheduledExecutorService executor;
    private final KnockProps propertiesReader;
    private final HandlingIO handlingIO;
    private final AudioPlayerWrapper knockPlayer;
    private final AudioPlayer successPlayer;
    private final AudioPlayer failedPlayer;
    private final AtomicInteger state;
    private final List<Integer> rhythmsTemplate;

    public GameKnockSimple() {
        executor = Globals.get().executor;
        propertiesReader = new KnockProps();
        knockPlayer = new AudioPlayerWrapper(executor, propertiesReader.getKnockVoice());
        successPlayer = new AudioPlayer(executor, propertiesReader.getSuccess());
        failedPlayer = new AudioPlayer(executor, propertiesReader.getFailed());
        state = new AtomicInteger(State.UserRepeating.ordinal());
        rhythmsTemplate = propertiesReader.getRythms();
        handlingIO = new HandlingIO(executor);
        //knockPlayer.play(null);
    }

    @Override
    public void run() {
        try {
            long now = System.currentTimeMillis();
            if ((now - lastKnock > 5000) && lastKnock != 0 && state.compareAndSet(State.UserRepeating.ordinal(), State.UserRepeatingCheck.ordinal())) {
                userRepeatingCheck();
            }
            final IOActionType lastAction = handlingIO.getLastAction();
            if (lastAction != null) {
                switch (lastAction) {
                    case KnockPressed:
//                        System.out.println("now - lastKnock : " + (now - lastKnock));
//                        System.out.println("state: " + state);
                        if ((now - lastKnock > 50) && state.get() == State.UserRepeating.ordinal()) {
                            System.out.println("Call knockPlayer");
                            knockPlayer.play(null);
                            if (lastKnock != 0) {
                                rhythms.add((int) (now - lastKnock));
                                if (rhythmsAreOk()) {
                                    state.set(State.PlayGoodResult.ordinal());
                                    successPlayer.play(this);
                                }
                            }
                            lastKnock = System.currentTimeMillis();
                        }
                        break;
                }
            }
        } catch (Throwable t) {
            if (t.getMessage() != null) {
                System.out.println(t.getMessage());
            }
        } finally {
            executor.schedule(this, 10, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void completed(CompletedEvent result, Object attachment) {
        if (result.equals(CompletedEvent.AudioPlayFinished)) {
            if (state.compareAndSet(State.PlayWrongResult.ordinal(), State.UserRepeating.ordinal())) {
                rhythms.clear();
                lastKnock = 0;
            }
            if (state.compareAndSet(State.PlayGoodResult.ordinal(), State.OpenDoor.ordinal())) {
                System.out.println("OPENING DOOR");
                handlingIO.openDoor(this);
            }
        }
        if (result.equals(CompletedEvent.DoorOpened)) {
            if (state.compareAndSet(State.OpenDoor.ordinal(), State.UserRepeating.ordinal())) {
                rhythms.clear();
                lastKnock = 0;
            }
        }
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void userRepeatingCheck() {
        if (rhythmsAreOk()) {
            state.set(State.PlayGoodResult.ordinal());
            successPlayer.play(this);
        } else {
            state.set(State.PlayWrongResult.ordinal());
            failedPlayer.play(this);
        }
    }

    private boolean rhythmsAreOk() {
        if (rhythms.size() != rhythmsTemplate.size()) {
            return false;
        }
        TreeMap<Integer, Integer> orderedRhythms = new TreeMap<>();
        for (int i = 0; i < rhythms.size(); i++) {
            int key = rhythms.get(i);
            while (orderedRhythms.containsKey(key)) {
                key++; // Itt egy picit muszály vagyunk csalni
            }
            orderedRhythms.put(key, i);
        }
        int numOfShorts = 0;
        numOfShorts = rhythmsTemplate.stream().filter((rythm) -> (rythm == SHORT)).map((Integer _item) -> 1).reduce(numOfShorts, Integer::sum);
        for (int i = 0; i < numOfShorts; i++) {
            if (rhythmsTemplate.get(orderedRhythms.pollFirstEntry().getValue()) != SHORT) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        final GameKnockSimple g = new GameKnockSimple();
        g.executor.submit(g);
    }

}
