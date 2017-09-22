package hu.finominfo.rnet.communication.data.server;

import hu.finominfo.rnet.communication.data.client.Client;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.22.
 */
public class ClientParam {
    private final ChannelHandlerContext context;
    private final AtomicBoolean connectedBack = new AtomicBoolean(false);
    private volatile Client client = null;

    public ClientParam(ChannelHandlerContext context) {
        this.context = context;
    }

    public ChannelHandlerContext getContext() {
        return context;
    }

    public AtomicBoolean getConnectedBack() {
        return connectedBack;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
