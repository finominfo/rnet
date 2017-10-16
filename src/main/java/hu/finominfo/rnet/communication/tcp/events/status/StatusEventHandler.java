package hu.finominfo.rnet.communication.tcp.events.status;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.events.file.FileType;
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
public class StatusEventHandler extends SimpleChannelInboundHandler<StatusEvent> {
    private final static Logger logger = Logger.getLogger(StatusEventHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StatusEvent msg) throws Exception {
        logger.info("StatusEvent arrived: " + msg.getAddresses().get(0));
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
        if (Globals.VERSION > msg.getVersion()) {
            Globals.get().addToTasksIfNotExists(TaskToDo.SEND_FILE, Globals.PROP_NAME, FileType.MAIN, ip);
            Globals.get().addToTasksIfNotExists(TaskToDo.SEND_FILE, Globals.JAR_NAME, FileType.MAIN, ip);
        }
    }
}
