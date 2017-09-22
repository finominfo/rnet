package hu.finominfo.rnet.communication.tcp.events.address;

import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventType;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class AddressEvent extends Event {
    private final List<Long> addresses;

    public AddressEvent(List<Long> addresses) {
        super(EventType.ADDRESS);
        this.addresses = addresses;
    }

    public List<Long> getAddresses() {
        return addresses;
    }

    @Override
    public void getRemainingData(ByteBuf buf) {
        buf.writeInt(getAddresses().size());
        getAddresses().stream().forEach(address -> buf.writeLong(address));
    }

    public static AddressEvent create(ByteBuf msg) {
        int size = msg.readInt();
        List<Long> addresses = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            addresses.add(msg.readLong());
        }
        return new AddressEvent(addresses);
    }
}
