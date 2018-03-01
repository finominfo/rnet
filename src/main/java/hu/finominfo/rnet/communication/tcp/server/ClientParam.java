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
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.22.
 */
public class ClientParam {
    private volatile ChannelHandlerContext context = null;
    private volatile Client client = null;
    private volatile AtomicLong lastTrying = new AtomicLong(0);
    private volatile String name = "";
    private volatile String status = null;
    private volatile String defAudio = null;
    private volatile String defVideo = null;
    private final Map<String, List<String>> dirs = new HashMap();

    public String getDefAudio() {
        return defAudio;
    }

    public void setDefAudio(String defAudio) {
        this.defAudio = defAudio;
    }

    public String getDefVideo() {
        return defVideo;
    }

    public void setDefVideo(String defVideo) {
        this.defVideo = defVideo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public boolean possibleToTry() {
        long last = lastTrying.get();
        long now = System.currentTimeMillis();
        return last + 10_000 < now && lastTrying.compareAndSet(last, now);
    }

}
