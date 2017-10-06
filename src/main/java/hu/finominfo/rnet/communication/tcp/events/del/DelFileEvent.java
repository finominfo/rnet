package hu.finominfo.rnet.communication.tcp.events.del;

import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventType;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

import java.io.File;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.29.
 */
public class DelFileEvent extends Event {

    private final String contentType;
    private final String shortName;

    public DelFileEvent(String contentType, String shortName) {
        super(EventType.DEL_FILE);
        this.contentType = contentType;
        this.shortName = shortName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getShortName() {
        return shortName;
    }

    public String getPathAndName() {
        return  contentType + File.separator + shortName;
    }

    @Override
    public void getRemainingData(ByteBuf buf) {
        byte[] bytes1 = getContentType().getBytes(CharsetUtil.UTF_8);
        byte[] bytes2 = getShortName().getBytes(CharsetUtil.UTF_8);
        buf.writeInt(bytes1.length);
        buf.writeInt(bytes2.length);
        buf.writeBytes(bytes1);
        buf.writeBytes(bytes2);
    }

    public static DelFileEvent create(ByteBuf msg) {
        int size1 = msg.readInt();
        int size2 = msg.readInt();
        byte[] bytes1 = new byte[size1];
        byte[] bytes2 = new byte[size2];
        msg.readBytes(bytes1);
        msg.readBytes(bytes2);
        String contentType = new String(bytes1, CharsetUtil.UTF_8);
        String shortName = new String(bytes2, CharsetUtil.UTF_8);
        return new DelFileEvent(contentType, shortName);
    }
}
