package hu.finominfo.rnet.communication.tcp.events;

import hu.finominfo.rnet.communication.tcp.events.address.AddressEvent;
import hu.finominfo.rnet.communication.tcp.events.file.FileEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class EventDecoder extends ByteToMessageDecoder {
    private final static Logger logger = Logger.getLogger(EventDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        logger.info("SOMETHING ARRIVED");
        if (in.readInt() != Event.CODE) {
            logger.error("NO EVENT CODE");
            in.discardReadBytes();
            return;
        }
        EventType eventType = EventType.get(in.readByte());
        if (null == eventType) {
            logger.error("NO EVENT TYPE");
            in.discardReadBytes();
            return;
        }
        logger.info(eventType.name() + " event arrived");
        switch (eventType) {
            case ADDRESS:
                out.add(AddressEvent.create(in));
                break;
            case FILE:
                out.add(FileEvent.create(in));
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
        int readableBytes = in.readableBytes();
        if (readableBytes > 0) {
            logger.error("readeable bytes: " + readableBytes);
            in.discardReadBytes();
        }
    }
}
