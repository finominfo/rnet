package hu.finominfo.rnet.communication.tcp.events.dir.media;

import hu.finominfo.rnet.database.H2KeyValue;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by kks on 2018.04.13..
 */
public class Types {
    //TODO: Az AUDIO DURING-ot az omxplayer játsza le.
    private final static Logger logger = Logger.getLogger(Types.class);

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

    public static Types load() {
        return new Types(Media.AUDIO.load(), Media.VIDEO.load(), Media.PICTURE.load());
    }

    public static Types empty() {
        return new Types(Media.AUDIO.empty(), Media.VIDEO.empty(), Media.PICTURE.empty());
    }


    public void save() {
        getAudioTypes().forEach((key, value) -> H2KeyValue.set(Media.AUDIO.value + key.ordinal(), checkValue(value, key)));
        getVideoTypes().forEach((key, value) -> H2KeyValue.set(Media.VIDEO.value + key.ordinal(), checkValue(value, key)));
        getPictureTypes().forEach((key, value) -> H2KeyValue.set(Media.PICTURE.value + key.ordinal(), checkValue(value, key)));
    }

    private String checkValue(String value, TimeOrder key) {
        return value == null ? "" : (value.startsWith(key.getSign()) ? value.substring(key.getSign().length()) : value);
    }

    private static void saveType(Map<TimeOrder, String> types, String type) {
        types.forEach((key, value) -> H2KeyValue.set(type + key.ordinal(), value == null ? "" : value));
    }

    public Types setNext(Map<TimeOrder, String> oneOfTypes, String current) {
        Map<TimeOrder, String> emptyPlaces = new HashMap<>();
        oneOfTypes.entrySet().stream()
                .filter(entry -> entry.getValue() == null || entry.getValue().isEmpty())
                .forEach(x -> emptyPlaces.put(x.getKey(), x.getValue()));
        Optional<Map.Entry<TimeOrder, String>> currentType = oneOfTypes.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().equals(current)).findAny();
        Map<TimeOrder, String> sortedTypes = new TreeMap<TimeOrder, String>(oneOfTypes);
        if (currentType.isPresent()) {
            Iterator<Map.Entry<TimeOrder, String>> iterator = sortedTypes.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<TimeOrder, String> entry = iterator.next();
                if (entry.getValue().equals(current)) {
                    break;
                }
            }
            while (iterator.hasNext()) {
                Map.Entry<TimeOrder, String> entry = iterator.next();
                if (entry.getValue() == null || entry.getValue().isEmpty()) {
                    entry.setValue(current);
                    break;
                }
            }
            oneOfTypes.putAll(sortedTypes);
            currentType.get().setValue("");
        } else {
            Iterator<Map.Entry<TimeOrder, String>> iterator = sortedTypes.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<TimeOrder, String> entry = iterator.next();
                if (entry.getValue() == null || entry.getValue().isEmpty()) {
                    entry.setValue(current);
                    break;
                }
            }
            oneOfTypes.putAll(sortedTypes);
        }
        return this;
    }

    @Override
    public String toString() {
        return "Types{" +
                "audioTypes=" + audioTypes +
                ", videoTypes=" + videoTypes +
                ", pictureTypes=" + pictureTypes +
                '}';
    }

    public static void main(String[] args) {
        Map<TimeOrder, String> picTypes = new HashMap<>();
        Media.PICTURE.timeOrder.forEach(to -> picTypes.put(to, ""));
//        setNext(picTypes, "tesztoldold");
//        System.out.println("result: " + picTypes);
//        setNext(picTypes, "tesztold");
//        System.out.println("result: " + picTypes);
//        setNext(picTypes, "teszt");
//        System.out.println("result: " + picTypes);
//        setNext(picTypes, "teszt");
//        System.out.println("result: " + picTypes);
//        setNext(picTypes, "teszt");
//        System.out.println("result: " + picTypes);
//        setNext(picTypes, "teszt");
//        System.out.println("result: " + picTypes);
//        setNext(picTypes, "teszt");
//        System.out.println("result: " + picTypes);
    }
}
