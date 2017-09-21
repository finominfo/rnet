package hu.finominfo.rnet.communication.data.events.received;

import hu.finominfo.rnet.communication.data.events.Event;
import hu.finominfo.rnet.communication.data.events.EventType;
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
