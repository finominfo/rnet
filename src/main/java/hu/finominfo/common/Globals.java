package hu.finominfo.common;

import hu.finominfo.rnet.communication.data.server.ClientParam;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

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
    public final ConcurrentMap<String, ClientParam> clients = new ConcurrentHashMap<>(); //ip and port - context

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
