package hu.finominfo.rnet.database;

import hu.finominfo.rnet.common.Globals;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kalman.kovacs@gmail.com on 2017.11.08.
 */
public class H2KeyValue {

    public static String COUNTER = "counter";
    public static String DEF_AUDIO = "defAudio";
    public static String DEF_VIDEO = "defVideo";

    private static H2KeyValue ourInstance = new H2KeyValue();

    public static String getValue(String key) {
        return ourInstance.getMapValue(key);
    }

    public static void set(String key, String value) {
        ourInstance.setMap(key, value);
    }

    private Map<String, String> keyValue = new HashMap<>();
    private final AtomicBoolean updateStarted = new AtomicBoolean(false);

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
                Globals.get().executor.schedule(() -> {
                    MVStore mvStore = MVStore.open("database");
                    MVMap<String, String> dataMap = mvStore.openMap("strings");
                    keyValue.entrySet().stream().forEach(e -> dataMap.put(e.getKey(), e.getValue()));
                    compactFile(5000, mvStore);
                    mvStore.commit();
                    mvStore.close();
                }, 10, TimeUnit.SECONDS);
            }, 3, TimeUnit.MINUTES);
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
    }

    private void compactFile(long maxCompactTime, MVStore store) {
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
    }
}
