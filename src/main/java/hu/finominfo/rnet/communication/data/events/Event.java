package hu.finominfo.rnet.communication.data.events;

import io.netty.buffer.ByteBuf;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.19..
 */
public abstract class Event {
    public static final int CODE = 0x11224433;
    public static final int MAX_BINARY_SIZE = 1_000_000;
    public static final int BUFFER_SIZE = MAX_BINARY_SIZE + 100_000;
    private final EventType eventType; //A getNumbert kell elküldeni

    public Event(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    public abstract void getRemainingData(ByteBuf buf);
}
