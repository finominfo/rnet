package hu.finominfo.rnet.communication.tcp.events.dir;

import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventType;
import hu.finominfo.rnet.communication.tcp.events.dir.media.TimeOrder;
import hu.finominfo.rnet.communication.tcp.events.dir.media.Types;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.29.
 */
public class RichDirEvent extends Event {
    private final static Logger logger = Logger.getLogger(RichDirEvent.class);
    private final Map<String, List<String>> dirs;
    private final String status;
    private final Types types;

    public RichDirEvent(String status, Types types) {
        super(EventType.DIR);
        this.dirs = new HashMap<>();
        this.status = status;
        this.types = types;
    }

    public RichDirEvent( Map<String, List<String>> dirs, String status, Types types) {
        super(EventType.DIR);
        this.dirs = dirs;
        this.status = status;
        this.types = types;
    }

    public Map<String, List<String>> getDirs() {
        return dirs;
    }

    public String getStatus() {
        return status;
    }

    public Types getTypes() {
        return types;
    }

    @Override
    public void getRemainingData(ByteBuf buf) {
        StringBuilder builder = new StringBuilder();
        dirs.entrySet().stream().forEach(entry -> {
            builder.append(entry.getKey());
            entry.getValue().stream().forEach(name -> builder.append(":").append(name));
            builder.append('\n');
        });
        byte[] bytesOfDirs = builder.toString().getBytes(CharsetUtil.UTF_8);
        byte[] bytesOfStatus = status.getBytes(CharsetUtil.UTF_8);
        buf.writeInt(bytesOfDirs.length);
        buf.writeInt(bytesOfStatus.length);
        types.getRemainingData(buf);
        buf.writeBytes(bytesOfDirs);
        buf.writeBytes(bytesOfStatus);
    }

    public static RichDirEvent create(ByteBuf msg) {
        int sizeOfDirs = msg.readInt();
        int sizeOfStatus = msg.readInt();
        Types types = Types.create(msg);
        byte[] bytesOfDirs = new byte[sizeOfDirs];
        byte[] bytesOfStatus = new byte[sizeOfStatus];
        msg.readBytes(bytesOfDirs);
        msg.readBytes(bytesOfStatus);
        String input = new String(bytesOfDirs, CharsetUtil.UTF_8);
        String status = new String(bytesOfStatus, CharsetUtil.UTF_8);
        RichDirEvent dirEvent = new RichDirEvent(status, types);
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
