package hu.finominfo.rnet.communication.tcp.events.control;

import io.netty.buffer.ByteBuf;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.12.
 */
public interface ControlObject {
    void getData(ByteBuf buf);
}
