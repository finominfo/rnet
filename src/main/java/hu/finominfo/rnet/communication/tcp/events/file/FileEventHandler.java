package hu.finominfo.rnet.communication.tcp.events.file;

import hu.finominfo.rnet.common.Globals;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class FileEventHandler extends SimpleChannelInboundHandler<FileEvent> {
    private final static Logger logger = Logger.getLogger(FileEventHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileEvent msg) throws Exception {
        String nameWithPath = null;
        switch (msg.getFileType()) {
            case VIDEO:
                nameWithPath = Globals.get().videoFolder + File.pathSeparator + msg.getName();
                break;
            case AUDIO:
                nameWithPath = Globals.get().audioFolder + File.pathSeparator + msg.getName();
                break;
            case PICTURE:
                nameWithPath = Globals.get().pictureFolder + File.pathSeparator + msg.getName();
                break;
        }
        try (FileOutputStream fos = new FileOutputStream(nameWithPath, true)) {
            fos.write(msg.getData());
            fos.flush();
            fos.close();
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
}
