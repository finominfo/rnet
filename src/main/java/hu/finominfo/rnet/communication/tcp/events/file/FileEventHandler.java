package hu.finominfo.rnet.communication.tcp.events.file;

import hu.finominfo.rnet.common.Globals;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class FileEventHandler extends SimpleChannelInboundHandler<FileEvent> {
    private final static Logger logger = Logger.getLogger(FileEventHandler.class);
    private volatile String lastFileName = null;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileEvent msg) throws Exception {
        String path = null;
        switch (msg.getFileType()) {
            case MAIN:
                path = ".";
                break;
            case VIDEO:
                path = Globals.get().videoFolder;
                break;
            case AUDIO:
            path = Globals.get().audioFolder;
                break;
            case PICTURE:
                path = Globals.get().pictureFolder;
                break;
        }
        String fullName = path + File.separator + msg.getName();
        if (null == lastFileName) {
            lastFileName = fullName;
            try {
                boolean result = Files.deleteIfExists(new File(fullName).toPath());
            } catch (Exception e) {
                logger.error("Could not delete file: " + fullName);
            }
        }
        try (FileOutputStream fos = new FileOutputStream(fullName, true)) {
            fos.write(msg.getData());
            fos.flush();
            fos.close();
        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            if (msg.isLastPart()) {
                lastFileName = null;
            }
        }
    }
}
