package hu.finominfo.rnet.communication.tcp.events.wait;

import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventType;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class WaitEvent extends Event {

    private final int time;

    public WaitEvent(int time) {
        super(EventType.WAIT);
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    @Override
    public void getRemainingData(ByteBuf buf) {
        buf.writeInt(getTime());
    }

    public static WaitEvent create(ByteBuf msg) {
        return new WaitEvent(msg.readInt());
    }
}
