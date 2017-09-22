package hu.finominfo.rnet.communication.tcp.events.file;

import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.EventType;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.19..
 */

public class FileEvent extends Event {
    private final FileType fileType;
    private final byte[] data; //A length-et is el kell küldeni!!!
    private final String name; //A length-et is el kell küldeni!!!
    private final boolean lastPart;

    public FileEvent(FileType fileType, byte[] data, String name, boolean lastPart) {
        super(EventType.FILE);
        this.fileType = fileType;
        this.data = data;
        this.name = name;
        this.lastPart = lastPart;
    }

    public FileType getFileType() {
        return fileType;
    }

    public byte[] getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public boolean isLastPart() {
        return lastPart;
    }

    public static FileEvent create(ByteBuf msg) {
        FileType fileType = FileType.get(msg.readByte());
        int size = msg.readInt();
        byte[] data = new byte[size];
        msg.readBytes(data);
        size = msg.readInt();
        byte[] strData = new byte[size];
        msg.readBytes(strData);
        String name = new String(strData, CharsetUtil.UTF_8);
        boolean lastPart = msg.readBoolean();
        return new FileEvent(fileType, data, name, lastPart);
    }

    @Override
    public void getRemainingData(ByteBuf buf) {
        buf.writeByte(getFileType().getNumber());
        buf.writeInt(getData().length);
        buf.writeBytes(getData());
        byte[] strData = getName().getBytes(CharsetUtil.UTF_8);
        buf.writeInt(strData.length);
        buf.writeBytes(strData);
        buf.writeBoolean(isLastPart());
    }
}

