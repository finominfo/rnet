package hu.finominfo.rnet.communication.tcp.events.address;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.taskqueue.FrontEndTaskToDo;
import hu.finominfo.rnet.taskqueue.TaskToDo;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class AddressEventHandler extends SimpleChannelInboundHandler<AddressEvent> {
    private final static Logger logger = Logger.getLogger(AddressEventHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AddressEvent msg) throws Exception {
        logger.info("AddressEvent arrived: " + msg.getAddresses().get(0));
        String ipAndPort = ctx.channel().remoteAddress().toString();
        String ip = Globals.get().getIp(ipAndPort);
        ClientParam clientParam = Globals.get().serverClients.get(ip);
        for (Map.Entry<String, List<Long>> entry : Globals.get().clientNameAddress.entrySet()) {
            if (Utils.isAddressEquals(msg.getAddresses(), entry.getValue())) {
                clientParam.setName(entry.getKey());
                return;
            }
        }
        clientParam.setName(ip);
        if (Globals.get().clientNameAddress.putIfAbsent(ip, msg.getAddresses()) == null) {
            Globals.get().addToFrontEndTasksIfNotExists(FrontEndTaskToDo.SAVE_NAME_ADDRESS);
        }
    }
}
