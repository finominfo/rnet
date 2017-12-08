package hu.finominfo.rnet.communication.http;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.statistics.Stat;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.apache.log4j.Logger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.19.
 */
public class MyChannelInboundHandlerAdapter extends ChannelInboundHandlerAdapter {

    private final static Logger logger = Logger.getLogger(MyChannelInboundHandlerAdapter.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            String ipAndPort = ctx.channel().remoteAddress().toString();
            String ip = Globals.get().getIp(ipAndPort);
            final FullHttpRequest request = (FullHttpRequest) msg;
            String uri = request.uri();
            switch (Command.get(uri)) {
                case start:
                    logger.info("START_COUNTER arrived: " + ip);
                    Utils.startCounterVideo();
                    sendResponse(ctx, request, "Counter started.");
                    break;
                case stop:
                    Globals.get().counter.makeStop();
                    sendResponse(ctx, request, "Counter stopped.");
                    break;
                case rnetstat:
                    sendResponse(ctx, request, Stat.get());
                    break;
                case unknown:
                    sendResponse(ctx, request, "Unknown command, Try /start, /stop.");
                    break;
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }


    private void sendResponse(ChannelHandlerContext ctx, FullHttpRequest request, final String responseMessage) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(responseMessage.getBytes())
        );
        if (HttpHeaders.isKeepAlive(request)) {
            response.headers().set(
                    HttpHeaders.Names.CONNECTION,
                    HttpHeaders.Values.KEEP_ALIVE
            );
        }
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, responseMessage.length());
        ctx.writeAndFlush(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.writeAndFlush(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.wrappedBuffer(cause.getMessage().getBytes())
        ));
    }
}
