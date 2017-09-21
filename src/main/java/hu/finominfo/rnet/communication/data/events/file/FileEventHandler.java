package hu.finominfo.rnet.communication.data.events.file;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.FileOutputStream;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class FileEventHandler extends SimpleChannelInboundHandler<FileEvent> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileEvent msg) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(msg.getName(), true)) {
            fos.write(msg.getData(), 0, msg.getData().length);
            fos.flush();
            fos.close();
            //visszaküldeni egy received eventet
        } catch (Exception ex) {
            //visszaküldeni egy failed eventet
        }
    }
}
