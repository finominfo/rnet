package hu.finominfo.rnet.communication.tcp.events.dir.media;

import hu.finominfo.rnet.database.H2KeyValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kks on 2018.04.21..
 */
public enum Media {
    AUDIO("AUDIO", Arrays.asList(TimeOrder.DURING, TimeOrder.SUCCESS, TimeOrder.FAILED)),
    VIDEO("VIDEO", Arrays.asList(TimeOrder.BEFORE, TimeOrder.SUCCESS, TimeOrder.FAILED)),
    PICTURE("PICTURE", Arrays.asList(TimeOrder.BEFORE, TimeOrder.SUCCESS, TimeOrder.FAILED));

    public final String value;
    public final List<TimeOrder> timeOrder;

    Media(String value, List<TimeOrder> timeOrder) {
        this.value = value;
        this.timeOrder = timeOrder;
    }

    public Map<TimeOrder, String> load() {
        Map<TimeOrder, String> saved = new HashMap<>();
        this.timeOrder.stream().forEach(timeOrder ->
                saved.put(timeOrder, H2KeyValue.getValue(this.value + timeOrder.ordinal())));
        return saved;
    }

    public Map<TimeOrder, String> empty() {
        Map<TimeOrder, String> empty = new HashMap<>();
        this.timeOrder.stream().forEach(timeOrder -> empty.put(timeOrder, ""));
        return empty;
    }
}
