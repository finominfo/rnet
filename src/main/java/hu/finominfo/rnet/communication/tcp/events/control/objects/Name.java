package hu.finominfo.rnet.communication.tcp.events.control.objects;

import hu.finominfo.rnet.communication.tcp.events.control.ControlObject;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.12.
 */
public class Name implements ControlObject{
    private final String name;

    public Name(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void getData(ByteBuf buf) {
        byte[] nameBytes = getName().getBytes(CharsetUtil.UTF_8);
        buf.writeInt(nameBytes.length);
        buf.writeBytes(nameBytes);
    }

    public static Name create(ByteBuf msg) {
        int size = msg.readInt();
        byte[] bytes = new byte[size];
        msg.readBytes(bytes);
        String name = new String(bytes, CharsetUtil.UTF_8);
        return new Name(name);
    }

}
