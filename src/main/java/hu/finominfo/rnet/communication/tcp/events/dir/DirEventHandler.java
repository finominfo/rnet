package hu.finominfo.rnet.communication.tcp.events.dir;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.29.
 */
public class DirEventHandler extends SimpleChannelInboundHandler<DirEvent> {
    private final static Logger logger = Logger.getLogger(DirEventHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DirEvent msg) throws Exception {
        String ipAndPort = ctx.channel().remoteAddress().toString();
        String ip = Globals.get().getIp(ipAndPort);
        logger.info("DirEvent arrived: " + ip);
        ClientParam clientParam = Globals.get().serverClients.get(ip);
        msg.getDirs().entrySet().stream().forEach(entry -> {
            clientParam.getDirs().put(entry.getKey(), entry.getValue());
            //TODO: Ezt majd átrakni synchron taskként.
            switch (entry.getKey()) {
                case Globals.videoFolder :
                    Globals.get().getFrontEnd().videoListModel.clear();
                    entry.getValue().stream().forEach(str -> Globals.get().getFrontEnd().videoListModel.addElement(str));
                break;
                case Globals.audioFolder :
                    Globals.get().getFrontEnd().audioListModel.clear();
                    entry.getValue().stream().forEach(str -> Globals.get().getFrontEnd().audioListModel.addElement(str));
                    break;
                case Globals.pictureFolder :
                    Globals.get().getFrontEnd().pictureListModel.clear();
                    entry.getValue().stream().forEach(str -> Globals.get().getFrontEnd().pictureListModel.addElement(str));
                    break;
            }
        });
    }
}
