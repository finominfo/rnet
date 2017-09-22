package hu.finominfo.rnet.communication.udp.in;

import hu.finominfo.rnet.communication.udp.Connection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.17..
 */
public class ConnectionDecoder extends MessageToMessageDecoder<DatagramPacket> {
    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        ByteBuf content = msg.content();
        if (content.readInt() != Connection.CODE) {return; }
        if (content.readByte() != Connection.SEPARATOR) {return; }
        int port = content.readInt();
        if (content.readByte() != Connection.SEPARATOR) {return; }
        byte[] str = new byte[content.readableBytes()];
        content.readBytes(str);
        String ip = new String(str, CharsetUtil.UTF_8);
        out.add(new Connection(ip, port));
    }
}
