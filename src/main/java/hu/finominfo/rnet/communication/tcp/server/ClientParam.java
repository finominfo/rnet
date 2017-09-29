package hu.finominfo.rnet.communication.tcp.server;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.communication.tcp.client.Client;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
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
    private final Map<String, List<String>> dirs = new HashMap();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClientParam(ChannelHandlerContext context) {
        this.context = context;
        dirs.put(Globals.get().audioFolder, new ArrayList<>());
        dirs.put(Globals.get().videoFolder, new ArrayList<>());
        dirs.put(Globals.get().pictureFolder, new ArrayList<>());
    }

    public Map<String, List<String>> getDirs() {
        return dirs;
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
        return lastTrying + 2_000 < System.currentTimeMillis();
    }
}
