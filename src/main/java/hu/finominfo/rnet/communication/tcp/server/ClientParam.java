package hu.finominfo.rnet.communication.tcp.server;

import hu.finominfo.rnet.communication.tcp.client.Client;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.22.
 */
public class ClientParam {
    private volatile ChannelHandlerContext context;
    private final AtomicBoolean connectedBack = new AtomicBoolean(false);
    private volatile Client client = null;
    private volatile long lastTrying = 0;
    private volatile String name = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClientParam(ChannelHandlerContext context) {
        this.context = context;
    }

    public ChannelHandlerContext getContext() {
        return context;
    }

    public void setContext(ChannelHandlerContext context) {
        this.context = context;
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

    public void setLastTrying() {
        lastTrying = System.currentTimeMillis();
    }

    public boolean possibleToTry() {
        return lastTrying + 60_000 < System.currentTimeMillis();
    }
}
