package hu.finominfo.node.servant;

import hu.finominfo.common.Globals;
import hu.finominfo.common.Props;
import hu.finominfo.node.EventToDo;
import hu.finominfo.rnet.communication.udp.Connection;
import hu.finominfo.rnet.communication.udp.in.ConnectionMonitor;
import hu.finominfo.rnet.communication.tcp.client.Client;
import hu.finominfo.rnet.communication.tcp.server.Server;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21.
 */
public class Servant implements Runnable, ChannelFutureListener {

    private final static Logger logger = Logger.getLogger(Servant.class);
    private volatile EventToDo eventToDo;
    private volatile ConnectionMonitor monitor;
    private volatile Server server;
    private final int broadcastMonitorPort;
    private final int clientPort;
    private final int serverPort;
    private volatile String currentServer;
    private volatile Client currentClient;

    public Servant() {
        broadcastMonitorPort = Props.get().getPort();
        serverPort = broadcastMonitorPort + 1;
        clientPort = broadcastMonitorPort + 2;
        eventToDo = EventToDo.BROADCAST_MONITOR;
    }

    @Override
    public void run() {
        switch (eventToDo) {
            case BROADCAST_MONITOR:
                monitor = new ConnectionMonitor(broadcastMonitorPort);
                monitor.bind().addListener(this);
                break;
            case SERVER:
                server = new Server(serverPort);
                server.bind().addListener(this);
                break;
            case CLIENT:
                boolean foundNewServer = false;
                Iterator<Connection> iterator = Globals.get().connections.iterator();
                while (iterator.hasNext()) {
                    Connection connection = iterator.next();
                    String address = connection.getServerIp();
                    if (!Globals.get().connectedServers.keySet().contains(address)) {
                        logger.info("Try to connect to a new server: " + address + ":" + clientPort);
                        currentServer = address;
                        currentClient = new Client(address, clientPort);
                        currentClient.bind().addListener(this);
                        foundNewServer = true;
                        break;
                    }
                }
                if (!foundNewServer) {
                    logger.info("Not found new server to connect.");
                    nextStart();
                }
                break;
        }
    }

    private void nextStart() {
        Globals.get().executor.schedule(this, 3, TimeUnit.SECONDS);
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            switch (eventToDo) {
                case BROADCAST_MONITOR:
                    eventToDo = EventToDo.SERVER;
                    logger.error("Broadcast monitor successfully created at port: " + broadcastMonitorPort);
                    break;
                case SERVER:
                    logger.info("Server successfully created at port: " + serverPort);
                    eventToDo = EventToDo.CLIENT;
                    break;
                case CLIENT:
                    Globals.get().connectedServers.put(currentServer, future);
                    logger.info("Sever successfully connected: " + currentServer);
                    break;
            }
        } else {
            switch (eventToDo) {
                case BROADCAST_MONITOR:
                    logger.error("Broadcast monitor could not started");
                    monitor.stop();
                    break;
                case SERVER:
                    logger.error("Server could not started at port: " + serverPort);
                    server.stop();
                    break;
                case CLIENT:
                    logger.error("Server could not connected: " + currentServer);
                    currentClient.stop();
                    break;
            }
        }
        nextStart();
    }

}
