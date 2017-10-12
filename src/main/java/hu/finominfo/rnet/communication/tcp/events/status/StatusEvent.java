package hu.finominfo.rnet.communication.tcp.events.status;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventType;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class StatusEvent extends Event {
    private final int version;
    private final List<Long> addresses;

    public StatusEvent(List<Long> addresses, int version) {
        super(EventType.STATUS);
        this.version = version;
        this.addresses = addresses;
    }

    public List<Long> getAddresses() {
        return addresses;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public void getRemainingData(ByteBuf buf) {
        buf.writeInt(Globals.VERSION);
        buf.writeInt(getAddresses().size());
        getAddresses().stream().forEach(address -> buf.writeLong(address));
    }

    public static StatusEvent create(ByteBuf msg) {
        int version = msg.readInt();
        int size = msg.readInt();
        List<Long> addresses = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            addresses.add(msg.readLong());
        }
        return new StatusEvent(addresses, version);
    }
}
