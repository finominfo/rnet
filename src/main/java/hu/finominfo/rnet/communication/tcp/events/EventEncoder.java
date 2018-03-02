package hu.finominfo.rnet.communication.tcp.events;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by kalman.kovacs@gmail.com on 2017.09.16..
 */
public class EventEncoder extends MessageToMessageEncoder<Event> {
    private final static Logger logger = Logger.getLogger(EventEncoder.class);
    private final static ConcurrentMap<String, AtomicLong> LOG_COUNTER = new ConcurrentHashMap<>();

    @Override
    protected void encode(ChannelHandlerContext ctx, Event msg, List<Object> out) throws Exception {
        ByteBuf buf = ctx.alloc().buffer();
        buf.writeInt(Event.CODE);
        buf.writeByte(msg.getEventType().getNumber());
        msg.getRemainingData(buf);
        out.add(buf);
        final String msgName = msg.getEventType().name();
        AtomicLong logCounter = LOG_COUNTER.computeIfAbsent(msgName, name -> new AtomicLong(0));
        long currentCounterValue = logCounter.incrementAndGet();
        if ((currentCounterValue & 0x07) == 0) {
            logger.info(currentCounterValue + ". " + msgName + " event sent.");
        }
    }
}
