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
    private final String defAudio;
    private final String defVideo;

    public DirEvent(String status, String defAudio, String defVideo) {
        super(EventType.DIR);
        this.dirs = new HashMap<>();
        this.status = status;
        this.defAudio = defAudio;
        this.defVideo = defVideo;
    }

    public Map<String, List<String>> getDirs() {
        return dirs;
    }

    public String getStatus() {
        return status;
    }

    public String getDefAudio() {
        return defAudio;
    }

    public String getDefVideo() {
        return defVideo;
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
        byte[] audBytes = defAudio.getBytes(CharsetUtil.UTF_8);
        byte[] vidBytes = defVideo.getBytes(CharsetUtil.UTF_8);
        buf.writeBytes(audBytes);
        buf.writeBytes(vidBytes);
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
        int sizeAud = msg.readInt();
        int sizeVid = msg.readInt();
        byte[] audBytes = new byte[sizeAud];
        byte[] vidBytes = new byte[sizeVid];
        msg.readBytes(audBytes);
        msg.readBytes(vidBytes);
        String defAudio = new String(audBytes, CharsetUtil.UTF_8);
        String defVideo = new String(vidBytes, CharsetUtil.UTF_8);
        DirEvent dirEvent = new DirEvent(status, defAudio, defVideo);
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
