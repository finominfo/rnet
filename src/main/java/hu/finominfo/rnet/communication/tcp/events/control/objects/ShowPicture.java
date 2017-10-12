package hu.finominfo.rnet.communication.tcp.events.control.objects;

import hu.finominfo.rnet.communication.tcp.events.control.ControlObject;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

import java.io.File;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.12.
 */
public class ShowPicture implements ControlObject{
    private final String contentType;
    private final String shortName;
    private final int seconds;

    public ShowPicture(String contentType, String shortName, int seconds) {
        this.contentType = contentType;
        this.shortName = shortName;
        this.seconds = seconds;
    }

    public String getContentType() {
        return contentType;
    }

    public String getShortName() {
        return shortName;
    }

    public String getPathAndName() {
        return  contentType + File.separator + shortName;
    }

    public int getSeconds() {
        return seconds;
    }

    @Override
    public void getData(ByteBuf buf) {
        buf.writeInt(seconds);
        byte[] bytes1 = getContentType().getBytes(CharsetUtil.UTF_8);
        byte[] bytes2 = getShortName().getBytes(CharsetUtil.UTF_8);
        buf.writeInt(bytes1.length);
        buf.writeInt(bytes2.length);
        buf.writeBytes(bytes1);
        buf.writeBytes(bytes2);
    }

    public static ShowPicture create(ByteBuf msg) {
        int seconds = msg.readInt();
        int size1 = msg.readInt();
        int size2 = msg.readInt();
        byte[] bytes1 = new byte[size1];
        byte[] bytes2 = new byte[size2];
        msg.readBytes(bytes1);
        msg.readBytes(bytes2);
        String contentType = new String(bytes1, CharsetUtil.UTF_8);
        String shortName = new String(bytes2, CharsetUtil.UTF_8);
        return new ShowPicture(contentType, shortName, seconds);
    }
}
