package hu.finominfo.rnet.communication.tcp.events.message;

import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventType;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

import java.io.File;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.29.
 */
public class MessageEvent extends Event {

    private final String text;

    public MessageEvent(String text) {
        super(EventType.MESSAGE);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public void getRemainingData(ByteBuf buf) {
        byte[] bytes1 = getText().getBytes(CharsetUtil.UTF_8);
        buf.writeInt(bytes1.length);
        buf.writeBytes(bytes1);
    }

    public static MessageEvent create(ByteBuf msg) {
        int size1 = msg.readInt();
        byte[] bytes1 = new byte[size1];
        msg.readBytes(bytes1);
        String text = new String(bytes1, CharsetUtil.UTF_8);
        return new MessageEvent(text);
    }
}
