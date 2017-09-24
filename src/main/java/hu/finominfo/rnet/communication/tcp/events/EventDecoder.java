package hu.finominfo.rnet.communication.tcp.events;

import hu.finominfo.rnet.communication.tcp.events.address.AddressEvent;
import hu.finominfo.rnet.communication.tcp.events.file.FileEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class EventDecoder extends ByteToMessageDecoder {
    private final static Logger logger = Logger.getLogger(EventDecoder.class);
    private final ByteBuf fileBuffer = Unpooled.buffer();
    private final ByteBuf input = Unpooled.buffer();
    private final AtomicInteger fileSize = new AtomicInteger(0);
    private volatile boolean waitingFoContinue = false;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        EventType eventType;
        input.writeBytes(in);
        if (!waitingFoContinue) {
            eventType = checkBeginning(input);
            if (eventType == null) return;
            logger.info(eventType.name() + " event arrived");
        } else {
            eventType = EventType.FILE;
        }
        switch (eventType) {
            case ADDRESS:
                out.add(AddressEvent.create(input));
                break;
            case FILE:
                waitingFoContinue = true;
                if (fileSize.get() == 0) {
                    if (input.readableBytes() > 9) {
                        fileSize.set(input.getInt(6) + input.getInt(10) + 10);
                    } else {
                        return;
                    }
                }
                int requiredMore = fileSize.get() - fileBuffer.readableBytes();
                int currentSize = input.readableBytes();
                if (currentSize <= requiredMore) {
                    fileBuffer.writeBytes(input);
                } else {
                    byte[] required = new byte[requiredMore];
                    input.readBytes(required);
                    fileBuffer.writeBytes(required);
                }
                //logger.info(eventType.name() + " event required bytes: " + (requiredMore - currentSize));
                if (fileBuffer.readableBytes() == fileSize.get()) {
                    fileSize.set(0);
                    waitingFoContinue = false;
                    logger.info(eventType.name() + " event part finished");
                    out.add(FileEvent.create(fileBuffer));
                }
                break;
            case RECEIVED:
                //Értesítés receivedről!!!
                break;
            case NOT_RECEIVED:
                //Értesítés not receivedről!!!
                break;
            case START:
                break;
            case STOP:
                break;
        }

        int readableBytes = input.readableBytes();
        if (readableBytes > 0) {
            logger.error("readeable bytes: " + readableBytes);
            input.discardReadBytes();
        }
    }

    private EventType checkBeginning(ByteBuf in) {
        EventType eventType;
        if (in.readInt() != Event.CODE) {
            logger.error("NO EVENT CODE");
            in.discardReadBytes();
            return null;
        }
        eventType = EventType.get(in.readByte());
        if (null == eventType) {
            logger.error("NO EVENT TYPE");
            in.discardReadBytes();
            return null;
        }
        return eventType;
    }
}
