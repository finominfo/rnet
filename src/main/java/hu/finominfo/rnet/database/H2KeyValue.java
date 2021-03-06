package hu.finominfo.rnet.database;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.communication.tcp.events.dir.media.Media;
import hu.finominfo.rnet.communication.tcp.events.dir.media.TimeOrder;
import hu.finominfo.rnet.communication.tcp.events.dir.media.Types;
import org.apache.log4j.Logger;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kalman.kovacs@gmail.com on 2017.11.08.
 */
public class H2KeyValue {

    public static String LAST_SENDING = "lastsending";
    public static String COUNTER = "counter";
    public static String COUNTER_FINISHED = "stop";
    public static String COUNTER_CURRENT_STATE = "counterCurrentState";

    private static H2KeyValue ourInstance = new H2KeyValue();
    private final static Logger logger = Logger.getLogger(H2KeyValue.class);

    public static String getValue(String key) {
        return ourInstance.getMapValue(key);
    }

    public static void set(String key, String value) {
        ourInstance.setMap(key, value);
    }

    private Map<String, String> keyValue = new HashMap<>();
    private final AtomicBoolean updateStarted = new AtomicBoolean(false);
    private final AtomicLong lastCompact = new AtomicLong(0);

    private String getMapValue(String key) {
        return keyValue.get(key);
    }

    private void setMap(String key, String value) {
        keyValue.put(key, value);
        updateDatabase();
    }

    private void updateDatabase() {
        if (updateStarted.compareAndSet(false, true)) {
            Globals.get().executor.schedule(() -> {
                updateStarted.set(false);
                MVStore mvStore = MVStore.open("database");
                MVMap<String, String> dataMap = mvStore.openMap("strings");
                logger.info("Writing data to database.");
                keyValue.entrySet().stream().forEach(e -> dataMap.put(e.getKey(), e.getValue()));
                compactFile(30_000, mvStore);
                logger.info("Comitting data to database.");
                mvStore.commit();
                logger.info("Closing database.");
                mvStore.close();
            }, 1, TimeUnit.MINUTES);
        }
    }

    private H2KeyValue() {
        MVStore mvStore = MVStore.open("database");
        MVMap<String, String> dataMap = mvStore.openMap("strings");
        checkDefaults(dataMap);
        dataMap.entrySet().stream().forEach(e -> keyValue.put(e.getKey(), e.getValue()));
        mvStore.commit();
        mvStore.close();
    }

    private void checkDefaults(MVMap<String, String> dataMap) {
        if (dataMap.get(COUNTER) == null) {
            dataMap.put(COUNTER, "60");
        }
        if (dataMap.get(COUNTER_CURRENT_STATE) == null
                || dataMap.get(COUNTER_CURRENT_STATE).equals("0")
                ||  dataMap.get(COUNTER_CURRENT_STATE).equals(dataMap.get(COUNTER))) {
            dataMap.put(COUNTER_CURRENT_STATE, COUNTER_FINISHED);
        }
        if (dataMap.get(LAST_SENDING) == null) {
            dataMap.put(LAST_SENDING, "0");
        }
        Arrays.asList(Media.values()).forEach(media -> media.timeOrder.forEach(timeOrder -> {
            String key = media.value + timeOrder.ordinal();
            if (dataMap.get(key) == null) {
                dataMap.put(key, "");
            }
        }));
    }

    private void compactFile(long maxCompactTime, MVStore store) {
        long last = lastCompact.get();
        long now = System.currentTimeMillis();
        if (now - last > 86_400_000L && lastCompact.compareAndSet(last, now)) {
            logger.info("Start compacting database.");
            store.setRetentionTime(0);
            long start = System.currentTimeMillis();
            while (store.compact(95, 16 * 1024 * 1024)) {
                store.sync();
                store.compactMoveChunks(95, 16 * 1024 * 1024);
                long time = System.currentTimeMillis() - start;
                if (time > maxCompactTime) {
                    break;
                }
            }
            logger.info("Finished compacting database.");
        }
    }
}
