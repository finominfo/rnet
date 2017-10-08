package hu.finominfo.rnet.communication.tcp.events.del;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import hu.finominfo.rnet.taskqueue.TaskToDo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.file.Files;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.29.
 */
public class DelFileEventHandler extends SimpleChannelInboundHandler<DelFileEvent> {
    private final static Logger logger = Logger.getLogger(DelFileEventHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DelFileEvent msg) throws Exception {
        String ipAndPort = ctx.channel().remoteAddress().toString();
        String ip = Globals.get().getIp(ipAndPort);
        String fullFileName = System.getProperty("user.dir") + File.separator + msg.getPathAndName();
        logger.info("PictureEvent arrived from: " + ip + " filename: " + fullFileName);
        try {
            File file = new File(fullFileName);
            Files.deleteIfExists(file.toPath());
            Globals.get().addToTasksIfNotExists(TaskToDo.SEND_DIR);
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
