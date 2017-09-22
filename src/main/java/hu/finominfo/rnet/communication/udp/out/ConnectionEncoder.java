package hu.finominfo.rnet.communication.udp.out;

import hu.finominfo.rnet.communication.udp.Connection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.List;


/**
 * Created by User on 2017.09.16..
 */
public class ConnectionEncoder extends MessageToMessageEncoder<Connection> {
    private final InetSocketAddress remoteAddress;
    public ConnectionEncoder(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Connection connection, List<Object> out) throws Exception {
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeInt(Connection.CODE);
        buf.writeByte(Connection.SEPARATOR);
        buf.writeInt(connection.getServerPort());
        buf.writeByte(Connection.SEPARATOR);
        buf.writeBytes(connection.getServerIp().getBytes(CharsetUtil.UTF_8));
        out.add(new DatagramPacket(buf, remoteAddress));
    }
}
