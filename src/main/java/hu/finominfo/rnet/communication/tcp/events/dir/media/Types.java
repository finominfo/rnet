package hu.finominfo.rnet.communication.tcp.events.dir.media;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.database.H2KeyValue;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by kks on 2018.04.13..
 */
public class Types {
    //TODO: Az AUDIO DURING-ot az omxplayer játsza le.
    private final static Logger logger = Logger.getLogger(Types.class);
    public static List<TimeOrder> AUDIO = Arrays.asList(TimeOrder.BEFORE, TimeOrder.DURING, TimeOrder.SUCCESS, TimeOrder.FAILED);
    public static List<TimeOrder> VIDEO = Arrays.asList(TimeOrder.BEFORE, TimeOrder.SUCCESS, TimeOrder.FAILED);
    public static List<TimeOrder> PICTURE = Arrays.asList(TimeOrder.BEFORE, TimeOrder.SUCCESS, TimeOrder.FAILED);

    private final Map<TimeOrder, String> audioTypes;
    private final Map<TimeOrder, String> videoTypes;
    private final Map<TimeOrder, String> pictureTypes;

    private Types(Map<TimeOrder, String> audioTypes, Map<TimeOrder, String> videoTypes, Map<TimeOrder, String> pictureTypes) {
        this.audioTypes = audioTypes;
        this.videoTypes = videoTypes;
        this.pictureTypes = pictureTypes;
    }

    public Map<TimeOrder, String> getAudioTypes() {
        return audioTypes;
    }

    public Map<TimeOrder, String> getVideoTypes() {
        return videoTypes;
    }

    public Map<TimeOrder, String> getPictureTypes() {
        return pictureTypes;
    }

    public void getRemainingData(ByteBuf buf) {
        byte[] audio = getData(audioTypes);
        byte[] video = getData(videoTypes);
        byte[] picture = getData(pictureTypes);
        buf.writeInt(audio.length);
        buf.writeInt(video.length);
        buf.writeInt(picture.length);
        buf.writeBytes(audio);
        buf.writeBytes(video);
        buf.writeBytes(picture);
    }

    private byte[] getData(Map<TimeOrder, String> types) {
        StringBuilder builder = new StringBuilder();
        types.entrySet().stream().forEach(entry -> {
            builder.append(entry.getKey().ordinal()).append(":").append(entry.getValue());
            builder.append('\n');
        });
        return builder.toString().getBytes(CharsetUtil.UTF_8);
    }

    public static Types create(ByteBuf msg) {
        int audioSize = msg.readInt();
        int videoSize = msg.readInt();
        int pictureSize = msg.readInt();
        byte[] audio = new byte[audioSize];
        byte[] video = new byte[videoSize];
        byte[] picture = new byte[pictureSize];
        msg.readBytes(audio);
        msg.readBytes(video);
        msg.readBytes(picture);
        return new Types(setData(audio), setData(video), setData(picture));
    }

    private static Map<TimeOrder, String> setData(byte[] bytes) {
        final Map<TimeOrder, String> type = new HashMap<>();
        String input = new String(bytes, CharsetUtil.UTF_8);
        Arrays.asList(input.split("\n")).stream().forEach(str -> {
            List<String> strings = new ArrayList(Arrays.asList(str.split(":")));
            if (strings.size() == 2) {
                String key = strings.get(0);
                String value = strings.get(1);
                type.put(TimeOrder.values()[Integer.valueOf(key)], value);
            }
        });
        return type;
    }

    public static Types getSaved() {
        return new Types(getSavedType("AUDIO"), getSavedType("VIDEO"), getSavedType("PICTURE"));
    }

    public static Map<TimeOrder, String> getSavedType(String type) {
        Map<TimeOrder, String> savedType = new HashMap<>();
        AUDIO.stream().forEach(timeOrder ->
                savedType.put(timeOrder, H2KeyValue.getValue(type + timeOrder.ordinal())));
        return savedType;
    }

    public void save() {
        getAudioTypes().forEach((key, value) -> H2KeyValue.set("AUDIO" + key.ordinal(), value == null ? "" : value));
        getVideoTypes().forEach((key, value) -> H2KeyValue.set("VIDEO" + key.ordinal(), value == null ? "" : value));
        getPictureTypes().forEach((key, value) -> {
            H2KeyValue.set("PICTURE" + key.ordinal(), value == null ? "" : value);
            logger.info("PICTURE" + key.ordinal() + " - " + (value == null ? "" : value));
        });
    }

    private static void saveType(Map<TimeOrder, String> types, String type) {
        types.forEach((key, value) -> H2KeyValue.set(type + key.ordinal(), value == null ? "" : value));
    }

    public static void setNext(Map<TimeOrder, String> types, String current) {
        logger.info("current: " + current);
        Map<TimeOrder, String> emptyPlaces = new HashMap<>();
        types.entrySet().stream()
                .filter(entry -> entry.getValue() == null || entry.getValue().isEmpty())
                .forEach(x -> emptyPlaces.put(x.getKey(), x.getValue()));
        Optional<Map.Entry<TimeOrder, String>> currentType = types.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().equals(current)).findAny();
        if (currentType.isPresent()) {
            currentType.get().setValue("");
        }
        if (emptyPlaces.isEmpty()) {
            return;
        }
        if (emptyPlaces.size() == 1) {
            Map.Entry<TimeOrder, String> other = emptyPlaces.entrySet().iterator().next();
            other.setValue(current);
            return;
        }
        Map<TimeOrder, String> sortedTypes = new TreeMap<TimeOrder, String>(types);
        Iterator<Map.Entry<TimeOrder, String>> iterator = sortedTypes.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<TimeOrder, String> entry = iterator.next();
            if (entry.getValue() != null && entry.getValue().equals(current)) {
                break;
            }
        }
        if (iterator.hasNext()) {
            Map.Entry<TimeOrder, String> entry = iterator.next();
            entry.setValue(current);
        } else {
            sortedTypes.entrySet().iterator().next().setValue(current);
        }
    }

    @Override
    public String toString() {
        return "Types{" +
                "audioTypes=" + audioTypes +
                ", videoTypes=" + videoTypes +
                ", pictureTypes=" + pictureTypes +
                '}';
    }
}
