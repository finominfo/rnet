package hu.finominfo.rnet.communication.tcp.events;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.26.
 */
public class FileInputCollector {
    private final ByteBuf byteBuf;
    private volatile boolean waitingForContinue = false;

    public FileInputCollector() {
        this.byteBuf = Unpooled.buffer();
    }

    public ByteBuf getByteBuf() {
        return byteBuf;
    }

    public boolean isWaitingForContinue() {
        return waitingForContinue;
    }

    public void setWaitingForContinue(boolean waitingForContinue) {
        this.waitingForContinue = waitingForContinue;
    }
}
