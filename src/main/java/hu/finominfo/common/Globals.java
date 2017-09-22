package hu.finominfo.common;

import hu.finominfo.rnet.communication.udp.Connection;
import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import io.netty.channel.ChannelFuture;

import java.util.Queue;
import java.util.concurrent.*;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class Globals {

    private static Globals ourInstance = new Globals();

    public static Globals get() {
        return ourInstance;
    }

    public final String videoFolder = "./video";
    public final String audioFolder = "./audio";
    public final String pictureFolder = "./picture";
    public final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(4);
    public final Queue<Event> events = new ConcurrentLinkedQueue<>();
    public final ConcurrentHashMap.KeySetView<Connection, Boolean> connections = ConcurrentHashMap.newKeySet();
    public final ConcurrentMap<String, ClientParam> serverClients = new ConcurrentHashMap<>(); //ip and port - context
    public final ConcurrentMap<String, ChannelFuture> connectedServers = new ConcurrentHashMap<>();


    public final String getIp(String ipAndPort) {
        int pos = ipAndPort.lastIndexOf(':');
        if (ipAndPort.startsWith("/")) {
            return ipAndPort.substring(1, pos);
        }
        else {
            return ipAndPort.substring(0, pos);
        }
    }
}
