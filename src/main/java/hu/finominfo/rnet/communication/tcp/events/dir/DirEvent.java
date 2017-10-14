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
    private final String status;

    public DirEvent(String status) {
        super(EventType.DIR);
        this.dirs = new HashMap<>();
        this.status = status;
    }

    public Map<String, List<String>> getDirs() {
        return dirs;
    }

    public String getStatus() {
        return status;
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
        byte[] bytes2 = status.getBytes(CharsetUtil.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeInt(bytes2.length);
        buf.writeBytes(bytes);
        buf.writeBytes(bytes2);
    }

    public static DirEvent create(ByteBuf msg) {
        int size = msg.readInt();
        int size2 = msg.readInt();
        byte[] bytes = new byte[size];
        msg.readBytes(bytes);
        byte[] bytes2 = new byte[size2];
        msg.readBytes(bytes2);
        String input = new String(bytes, CharsetUtil.UTF_8);
        String status = new String(bytes2, CharsetUtil.UTF_8);
        DirEvent dirEvent = new DirEvent(status);
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
