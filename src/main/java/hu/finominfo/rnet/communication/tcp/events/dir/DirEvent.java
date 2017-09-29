package hu.finominfo.rnet.communication.tcp.events.dir;

import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventType;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

import java.util.*;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.29.
 */
public class DirEvent extends Event {
    private final Map<String, List<String>> dirs;

    public DirEvent() {
        super(EventType.DIR);
        this.dirs = new HashMap<>();
    }

    public Map<String, List<String>> getDirs() {
        return dirs;
    }

    @Override
    public void getRemainingData(ByteBuf buf) {
        StringBuilder builder = new StringBuilder();
        dirs.entrySet().stream().forEach(entry -> {
            builder.append(entry.getKey());
            entry.getValue().stream().forEach(name -> builder.append(":").append(name));
            builder.append('\n');
        });
        byte[] bytes = builder.toString().getBytes(CharsetUtil.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public static DirEvent create(ByteBuf msg) {
        int size = msg.readInt();
        byte[] bytes = new byte[size];
        msg.readBytes(bytes);
        String input = new String(bytes, CharsetUtil.UTF_8);
        DirEvent dirEvent = new DirEvent();
        Arrays.asList(input.split("\n")).stream().forEach(str -> {
            List<String> strings = new ArrayList(Arrays.asList(str.split(":")));
            String key = strings.remove(0);
            List<String> values = new ArrayList<>();
            dirEvent.getDirs().put(key, values);
            strings.stream().forEach(s -> values.add(s));
        });
        return dirEvent;
    }
}
