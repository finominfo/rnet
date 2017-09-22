package hu.finominfo.rnet.communication.tcp.events.address;

import hu.finominfo.common.Globals;
import hu.finominfo.rnet.communication.tcp.events.file.FileEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class AddressEventHandler extends SimpleChannelInboundHandler<AddressEvent> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AddressEvent msg) throws Exception {
        boolean found = false;
        for (Map.Entry<String, List<Long>> entry : Globals.get().clientNameAddress.entrySet()) {
            for (Long long1 : entry.getValue()) {
                for (Long long2 : msg.getAddresses()) {
                    if (long1.longValue() == long2.longValue()) {
                        return;
                    }
                }
            }
        }
        if (Globals.get().clientNameAddress.putIfAbsent(String.valueOf(msg.getAddresses().get(0)), msg.getAddresses()) == null) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, List<Long>> entry : Globals.get().clientNameAddress.entrySet()) {
                builder.append(entry.getKey()).append('=');
                builder.append(entry.getValue().stream().map(Object::toString).collect(Collectors.joining("-")));
                builder.append(System.lineSeparator());
            }
            try (PrintStream out = new PrintStream(new FileOutputStream("addresses.txt"))) {
                out.print(builder.toString());
            }
        }
    }
}
