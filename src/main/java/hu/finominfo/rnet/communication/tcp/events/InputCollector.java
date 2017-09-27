package hu.finominfo.rnet.communication.tcp.events;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.26.
 */
public class InputCollector {
    private final ByteBuf byteBuf;
    private volatile EventType eventType = null;
    private final ByteBuf fileBuffer = Unpooled.buffer();
    private final AtomicInteger fileSize = new AtomicInteger(0);


    public InputCollector() {
        this.byteBuf = Unpooled.buffer();
    }

    public ByteBuf getByteBuf() {
        return byteBuf;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public ByteBuf getFileBuffer() {
        return fileBuffer;
    }

    public AtomicInteger getFileSize() {
        return fileSize;
    }
}
