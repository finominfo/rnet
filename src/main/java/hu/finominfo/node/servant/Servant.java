package hu.finominfo.node.servant;

import hu.finominfo.common.Globals;
import hu.finominfo.common.Props;
import hu.finominfo.node.EventToDo;
import hu.finominfo.rnet.communication.Interface;
import hu.finominfo.rnet.communication.tcp.client.ServerParam;
import hu.finominfo.rnet.communication.tcp.events.address.AddressEvent;
import hu.finominfo.rnet.communication.udp.Connection;
import hu.finominfo.rnet.communication.udp.in.ConnectionMonitor;
import hu.finominfo.rnet.communication.tcp.client.Client;
import hu.finominfo.rnet.communication.tcp.server.Server;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21.
 */
public class Servant implements Runnable, ChannelFutureListener {

    private final static Logger logger = Logger.getLogger(Servant.class);
    private final Queue<EventToDo> tasksToDo = new ConcurrentLinkedQueue<>();
    private volatile EventToDo currentToDo;
    private volatile ConnectionMonitor monitor;
    private volatile Server server;
    private final int broadcastMonitorPort;
    private final int clientPort;
    private final int serverPort;
    private volatile String currentConnectToServer;
    private volatile Client currentClient;
    private volatile Map.Entry<String, ServerParam> currentServerParam;
    private final AtomicBoolean shouldNext = new AtomicBoolean(true);

    public Servant() {
        broadcastMonitorPort = Props.get().getPort();
        serverPort = broadcastMonitorPort + 1;
        clientPort = broadcastMonitorPort + 2;
        tasksToDo.add(EventToDo.BROADCAST_MONITOR);
        tasksToDo.add(EventToDo.START_SERVER);
        tasksToDo.add(EventToDo.FIND_SERVERS_TO_CONNECT);
        tasksToDo.add(EventToDo.SEND_MAC_ADRESSES);
    }


    @Override
    public void run() {
        if (shouldNext.compareAndSet(true, false)) {
            currentToDo = tasksToDo.poll();
        }
        if (null == currentToDo) {
            runLater();
        }
        switch (currentToDo) {
            case BROADCAST_MONITOR:
                monitor = new ConnectionMonitor(broadcastMonitorPort);
                monitor.bind().addListener(this);
                break;
            case START_SERVER:
                server = new Server(serverPort);
                server.bind().addListener(this);
                break;
            case FIND_SERVERS_TO_CONNECT:
                boolean foundNewServer = false;
                Iterator<Connection> iterator = Globals.get().connections.iterator();
                while (iterator.hasNext()) {
                    Connection connection = iterator.next();
                    String address = connection.getServerIp();
                    if (!Globals.get().connectedServers.keySet().contains(address)) {
                        currentConnectToServer = address;
                        currentClient = new Client(address, clientPort);
                        currentClient.bind().addListener(this);
                        foundNewServer = true;
                        break;
                    }
                }
                if (!foundNewServer) {
                    if (!Globals.get().connectedServers.isEmpty()) {
                        shouldNext.set(true);
                    }
                    runLater();
                }
                break;
            case SEND_MAC_ADRESSES:
                boolean shouldSend = false;
                Iterator<Map.Entry<String, ServerParam>> serverIterator = Globals.get().connectedServers.entrySet().iterator();
                while (serverIterator.hasNext()) {
                    currentServerParam = serverIterator.next();
                    if (currentServerParam.getValue().getSentAddresses().compareAndSet(false, true)) {
                        currentServerParam.getValue().getFuture().channel().writeAndFlush(new AddressEvent(Interface.adresses)).addListener(this);
                        shouldSend = true;
                        break;
                    }
                }
                if (!shouldSend) {
                    shouldNext.set(true);
                    runLater();
                }
                break;
        }
    }

    private void runLater() {
        Globals.get().executor.schedule(this, 1, TimeUnit.SECONDS);
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            switch (currentToDo) {
                case BROADCAST_MONITOR:
                    shouldNext.set(true);
                    logger.error("Broadcast monitor successfully created at port: " + broadcastMonitorPort);
                    break;
                case START_SERVER:
                    logger.info("Server successfully created at port: " + serverPort);
                    shouldNext.set(true);
                    break;
                case FIND_SERVERS_TO_CONNECT:
                    Globals.get().connectedServers.put(currentConnectToServer, new ServerParam(future));
                    logger.info("Sever successfully connected: " + currentConnectToServer + ":" + clientPort);
                    break;
                case SEND_MAC_ADRESSES:
                    logger.info("Send mac addresses was successful to server. " + currentServerParam.getKey() + ":" + clientPort);
                    break;
            }
        } else {
            switch (currentToDo) {
                case BROADCAST_MONITOR:
                    logger.error("Broadcast monitor could not started");
                    monitor.stop();
                    break;
                case START_SERVER:
                    logger.error("Server could not started at port: " + serverPort);
                    server.stop();
                    break;
                case FIND_SERVERS_TO_CONNECT:
                    logger.error("Server could not connected: " + currentConnectToServer);
                    currentClient.stop();
                    break;
                case SEND_MAC_ADRESSES:
                    logger.info("Send mac addresses was unsuccessful to server. " + currentServerParam.getKey() + ":" + clientPort);
                    currentServerParam.getValue().getSentAddresses().set(false);
                    break;
            }
        }
        runLater();
    }

}
