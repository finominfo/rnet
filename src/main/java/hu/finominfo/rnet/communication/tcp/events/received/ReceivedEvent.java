package hu.finominfo.rnet.communication.tcp.events.received;

import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventType;
import io.netty.buffer.ByteBuf;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class ReceivedEvent extends Event {

    public ReceivedEvent() {
        super(EventType.RECEIVED);
    }

    @Override
    public void getRemainingData(ByteBuf buf) {
    }
}
