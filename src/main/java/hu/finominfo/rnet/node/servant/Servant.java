package hu.finominfo.rnet.node.servant;

import hu.finominfo.properties.Props;
import hu.finominfo.rnet.common.*;
import hu.finominfo.rnet.common.Interface;
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

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21.
 */
public class Servant extends SynchronousWorker implements ChannelFutureListener {

    private final static Logger logger = Logger.getLogger(Servant.class);
    private volatile ConnectionMonitor monitor = null;
    private volatile Server server;
    private final int broadcastMonitorPort;
    private final int clientPort;
    private final int serverPort;
    private volatile String currentConnectToServer;
    private volatile Client currentClient;
    private volatile Map.Entry<String, ServerParam> currentServerParam;


    public Servant() {
        super();
        broadcastMonitorPort = Props.get().getPort();
        serverPort = broadcastMonitorPort + 1;
        clientPort = broadcastMonitorPort + 2;
        Globals.get().addToTasksIfNotExists(TaskToDo.MONITOR_BROADCAST);
        Globals.get().addToTasksIfNotExists(TaskToDo.START_SERVER);
        //Globals.get().addToTasksIfNotExists(TaskToDo.FIND_SERVERS_TO_CONNECT);
        //Globals.get().addToTasksIfNotExists(TaskToDo.SEND_MAC_ADRESSES);

    }


    @Override
    public void runCurrentAsynchronousTask() {
        switch (currentTask.getTaskToDo()) {
            case MONITOR_BROADCAST:
                if (shouldHandleAgain(5000)) {
                    if (monitor != null) {
                        monitor.stop();
                    }
                    monitor = new ConnectionMonitor(broadcastMonitorPort);
                    monitor.bind().addListener(this);
                }
                break;
            case START_SERVER:
                if (shouldHandleAgain(5000)) {
                    if (server != null) {
                        server.stop();
                    }
                    server = new Server(serverPort);
                    server.bind().addListener(this);
                }
                break;
            case FIND_SERVERS_TO_CONNECT:
                if (currentTask.getTaskSendingFinished().compareAndSet(true, false)) {
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
                    if ((!foundNewServer && !Globals.get().connectedServers.isEmpty()) || (currentTask.getCounter().incrementAndGet() > 50)) {
                        Globals.get().addToTasksIfNotExists(TaskToDo.SEND_MAC_ADRESSES);
                        currentTaskFinished();
                    }
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
                if ((!shouldSend && currentTaskRunning(2000)) || (currentTask.getCounter().incrementAndGet() > 100)) {
                    currentTaskFinished();
                }
                break;
            default:
                logger.error("Not implemented task: " + currentTask.getTaskToDo().toString());
                currentTaskFinished();
                break;
        }
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        currentTask.getTaskSendingFinished().set(true);
        if (future.isSuccess()) {
            switch (currentTask.getTaskToDo()) {
                case MONITOR_BROADCAST:
                    currentTaskFinished();
                    logger.error("Broadcast monitor successfully created at port: " + broadcastMonitorPort);
                    break;
                case START_SERVER:
                    logger.info("Server successfully created at port: " + serverPort);
                    currentTaskFinished();
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
            switch (currentTask.getTaskToDo()) {
                case MONITOR_BROADCAST:
                    logger.error("Broadcast monitor could not started");
                    monitor.stop();
                    monitor = null;
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
    }
}