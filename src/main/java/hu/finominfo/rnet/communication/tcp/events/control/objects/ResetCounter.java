package hu.finominfo.rnet.communication.tcp.events.control.objects;

import hu.finominfo.rnet.communication.tcp.events.control.ControlObject;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

import java.io.File;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.12.
 */
public class ResetCounter implements ControlObject{
    private final int minutes;

    public ResetCounter(int minutes) {
        this.minutes = minutes;
    }

    public int getMinutes() {
        return minutes;
    }

    @Override
    public void getData(ByteBuf buf) {
        buf.writeInt(minutes);
    }

    public static ResetCounter create(ByteBuf msg) {
        int minutes = msg.readInt();
        return new ResetCounter(minutes);
    }

}
