package hu.finominfo.rnet.communication.tcp.events.message;

import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventType;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.29.
 */
public class MessageEvent extends Event {

    private final String text;
    private final int seconds;

    public MessageEvent(String text, int seconds) {
        super(EventType.MESSAGE);
        this.text = text;
        this.seconds = seconds;
    }

    public String getText() {
        return text;
    }

    public int getSeconds() {
        return seconds;
    }

    @Override
    public void getRemainingData(ByteBuf buf) {
        buf.writeInt(seconds);
        byte[] bytes1 = getText().getBytes(CharsetUtil.UTF_8);
        buf.writeInt(bytes1.length);
        buf.writeBytes(bytes1);
    }

    public static MessageEvent create(ByteBuf msg) {
        int seconds = msg.readInt();
        int size1 = msg.readInt();
        byte[] bytes1 = new byte[size1];
        msg.readBytes(bytes1);
        String text = new String(bytes1, CharsetUtil.UTF_8);
        return new MessageEvent(text, seconds);
    }
}
