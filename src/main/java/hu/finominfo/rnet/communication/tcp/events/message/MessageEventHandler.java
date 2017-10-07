package hu.finominfo.rnet.communication.tcp.events.message;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.frontend.servant.MessageDisplay;
import hu.finominfo.rnet.taskqueue.TaskToDo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.29.
 */
public class MessageEventHandler extends SimpleChannelInboundHandler<MessageEvent> {
    private final static Logger logger = Logger.getLogger(MessageEventHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageEvent msg) throws Exception {
        String ipAndPort = ctx.channel().remoteAddress().toString();
        String ip = Globals.get().getIp(ipAndPort);
        logger.info("MessageEvent arrived from: " + ip + " message: " + msg.getText());
        new MessageDisplay(msg.getText()).show();
    }
}
