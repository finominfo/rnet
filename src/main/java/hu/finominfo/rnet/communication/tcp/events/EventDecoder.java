package hu.finominfo.rnet.communication.tcp.events;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.communication.tcp.events.control.ControlEvent;
import hu.finominfo.rnet.communication.tcp.events.dir.RichDirEvent;
import hu.finominfo.rnet.communication.tcp.events.status.StatusEvent;
import hu.finominfo.rnet.communication.tcp.events.del.DelFileEvent;
import hu.finominfo.rnet.communication.tcp.events.file.FileEvent;
import hu.finominfo.rnet.communication.tcp.events.message.MessageEvent;
import hu.finominfo.rnet.communication.tcp.events.wait.WaitEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class EventDecoder extends ByteToMessageDecoder {
    private final Logger logger = Logger.getLogger(EventDecoder.class);
    private final ConcurrentMap<ChannelHandlerContext, InputCollector> inputs = new ConcurrentHashMap<>();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //logger.info("something arrived, size is : " + in.readableBytes());
        InputCollector inputCollector = getInputCollector(ctx);
        ByteBuf input = inputCollector.getByteBuf();
        input.writeBytes(in);
        EventType eventType = inputCollector.getEventType();
        if (eventType == null) {
            eventType = getEventType(input);
            if (eventType == null) return;
//            logger.info(eventType.name() + " event arrived");
        }
        switch (eventType) {
            case STATUS:
                out.add(StatusEvent.create(input));
                break;
            case CONTROL:
                out.add(ControlEvent.create(input));
                break;
            case DEL_FILE:
                out.add(DelFileEvent.create(input));
                break;
            case MESSAGE:
                out.add(MessageEvent.create(input));
                break;
            case DIR:
                inputCollector.setEventType(EventType.DIR);
                if (input.readableBytes() > 19) {
                    if (input.readableBytes() < input.getInt(5) + input.getInt(9) + input.getInt(13) + input.getInt(17) + input.getInt(21) + 20) {
                        return;
                    }
                } else {
                    return;
                }
                inputCollector.setEventType(null);
                out.add(RichDirEvent.create(input));
                break;
            case WAIT:
                out.add(WaitEvent.create(input));
                break;
            case FILE:
                inputCollector.setEventType(EventType.FILE);
                AtomicInteger fileSize = inputCollector.getFileSize();
                ByteBuf fileBuffer = inputCollector.getFileBuffer();
                if (fileSize.get() == 0) {
                    garbageFinishedChannels();
                    if (input.readableBytes() > 10) {
                        fileSize.set(input.getInt(8) + input.getInt(12) + 11);
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
                    inputCollector.setEventType(null);
                    logger.info(eventType.name() + " event part finished");
                    out.add(FileEvent.create(fileBuffer));
                    input.discardReadBytes();
                    fileBuffer.discardReadBytes();
                }
                return;
        }

        input.discardReadBytes();
    }

    private void garbageFinishedChannels() {
        for (Iterator<ChannelHandlerContext> iterator = inputs.keySet().iterator(); iterator.hasNext(); ) {
            ChannelHandlerContext ctx = iterator.next();
            String ipAndPort = ctx.channel().remoteAddress().toString();
            String ip = Globals.get().getIp(ipAndPort);
            if (Globals.get().serverClients.get(ip) == null || Globals.get().serverClients.get(ip).getContext() == null) {
                iterator.remove();
            }
        }
    }

    private InputCollector getInputCollector(ChannelHandlerContext ctx) {
        InputCollector inputCollector = inputs.get(ctx);
        if (inputCollector == null) {
            InputCollector inputCollectorTemp = new InputCollector();
            inputCollector = inputs.putIfAbsent(ctx, inputCollectorTemp);
            if (inputCollector == null) {
                inputCollector = inputCollectorTemp;
            }
        }
        return inputCollector;
    }

    private EventType getEventType(ByteBuf in) {
        EventType eventType;
        if (in.readInt() != Event.CODE) {
            logger.error("NO EVENT CODE");
            in.clear();
            return null;
        }
        eventType = EventType.get(in.readByte());
        if (null == eventType) {
            logger.error("NO EVENT TYPE");
            in.clear();
            return null;
        }
        return eventType;
    }
}
