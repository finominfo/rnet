package hu.finominfo.rnet.communication.data.events;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;


/**
 * Created by kalman.kovacs@gmail.com on 2017.09.16..
 */
public class EventEncoder extends MessageToMessageEncoder<Event> {
    private final static Logger logger = Logger.getLogger(EventEncoder.class);
    private final InetSocketAddress remoteAddress;

    public EventEncoder(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Event msg, List<Object> out) throws Exception {
        ByteBuf buf = ctx.alloc().buffer();
        buf.writeInt(Event.CODE);
        buf.writeByte(msg.getEventType().getNumber());
        msg.getRemainingData(buf);
        out.add(buf);
        logger.info(msg.getEventType().name() + " event sent");
    }
}
