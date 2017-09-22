package hu.finominfo.rnet.communication.tcp.client;

import io.netty.channel.ChannelFuture;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.22.
 */
public class ServerParam {
    private volatile ChannelFuture future;
    private final AtomicBoolean sentAddresses = new AtomicBoolean(false);


    public ServerParam(ChannelFuture future) {
        this.future = future;
    }

    public AtomicBoolean getSentAddresses() {
        return sentAddresses;
    }

    public ChannelFuture getFuture() {
        return future;
    }

    public void setFuture(ChannelFuture future) {
        this.future = future;
    }
}
