package hu.finominfo.rnet.node.servant;

import hu.finominfo.rnet.database.H2KeyValue;
import hu.finominfo.rnet.properties.Props;
import hu.finominfo.rnet.common.*;
import hu.finominfo.rnet.common.Interface;
import hu.finominfo.rnet.communication.tcp.client.ServerParam;
import hu.finominfo.rnet.communication.tcp.events.status.StatusEvent;
import hu.finominfo.rnet.communication.tcp.events.dir.DirEvent;
import hu.finominfo.rnet.communication.tcp.events.wait.WaitEvent;
import hu.finominfo.rnet.communication.udp.Connection;
import hu.finominfo.rnet.communication.udp.in.ConnectionMonitor;
import hu.finominfo.rnet.communication.tcp.client.Client;
import hu.finominfo.rnet.communication.tcp.server.Server;
import hu.finominfo.rnet.taskqueue.Task;
import hu.finominfo.rnet.taskqueue.TaskToDo;
import hu.finominfo.rnet.taskqueue.Worker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21.
 */
public class Servant extends Worker implements ChannelFutureListener {

    private final static Logger logger = Logger.getLogger(Servant.class);
    private volatile ConnectionMonitor monitor = null;
    private volatile Server server;
    private final int broadcastMonitorPort;
    private final int clientPort;
    private final int serverPort;
    private volatile String currentConnectToServer;
    private volatile Client currentClient;
    private volatile Map.Entry<String, ServerParam> currentServerParam;
    private final AtomicLong longestDirSendingTime = new AtomicLong(0);


    public Servant() {
        super();
        broadcastMonitorPort = Props.get().getPort();
        serverPort = broadcastMonitorPort + 1;
        clientPort = broadcastMonitorPort + 2;
        Globals.get().addToTasksIfNotExists(TaskToDo.MONITOR_BROADCAST);
        Globals.get().addToTasksIfNotExists(TaskToDo.START_SERVER);
        //Globals.get().addToTasksIfNotExists(TaskToDo.FIND_SERVERS_TO_CONNECT);
        //Globals.get().addToTasksIfNotExists(TaskToDo.SEND_MAC_ADDRESSES);

    }


    @Override
    public void runCurrentTask() {
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
                    logger.info("FIND_SERVERS_TO_CONNECT");
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
                        Globals.get().addToTasksIfNotExists(TaskToDo.SEND_MAC_ADDRESSES);
                        currentTaskFinished();
                    }
                }
                break;
            case SEND_MAC_ADDRESSES:
                boolean shouldSend = false;
                Iterator<Map.Entry<String, ServerParam>> serverIterator = Globals.get().connectedServers.entrySet().iterator();
                while (serverIterator.hasNext()) {
                    currentServerParam = serverIterator.next();
                    if (currentServerParam.getValue().getSentAddresses().compareAndSet(false, true)) {
                        currentServerParam.getValue().getFuture().channel().writeAndFlush(new StatusEvent(Interface.addresses, Globals.VERSION)).addListener(this);
                        shouldSend = true;
                        break;
                    }
                }
                if ((!shouldSend && currentTaskRunning(2000)) || (currentTask.getCounter().incrementAndGet() > 100)) {
                    Globals.get().addToTasksIfNotExists(TaskToDo.SEND_DIR);
                    currentTaskFinished();
                }
                break;
            case SEND_WAIT:
                try {
                    Globals.get().connectedServers.values().stream()
                            .forEach(serverParam -> serverParam.getFuture().channel().writeAndFlush(new WaitEvent(5000)));
                } catch (Exception e) {
                    logger.error(e);
                } finally {
                    currentTaskFinished();
                }
            case SEND_DIR:
                try {
                    if (!Globals.get().connectedServers.isEmpty()) {
                        DirEvent dirEvent = new DirEvent(Globals.get().status.getCurrent(),
                                H2KeyValue.getValue(H2KeyValue.DEF_AUDIO),
                                H2KeyValue.getValue(H2KeyValue.DEF_VIDEO));
                        Arrays.asList(Globals.videoFolder, Globals.audioFolder, Globals.pictureFolder).stream()
                                .forEach(folder -> dirEvent.getDirs().put(folder, Utils.getFilesFromFolder(folder)));
                        Globals.get().connectedServers.values().stream()
                                //.filter(ServerParam::dirCanBeSend)
                                .map(serverParam -> serverParam.getFuture().channel())
                                //.filter(Channel::isWritable)
                                .forEach(channel -> channel.writeAndFlush(dirEvent).addListener(FIRE_EXCEPTION_ON_FAILURE));
                    }
                } catch (Exception e) {
                    logger.error(e);
                } finally {
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
        if (currentTask == null) {
            String ip = "";
            if (future.channel() != null && future.channel().remoteAddress() != null) {
                ip = future.channel().remoteAddress().toString();
            }
            logger.error("currentTask is null: " + future.channel().remoteAddress().toString());
            return;
        }
        currentTask.getTaskSendingFinished().set(true);
        if (future.isSuccess()) {
            switch (currentTask.getTaskToDo()) {
                case MONITOR_BROADCAST:
                    currentTaskFinished();
                    logger.error("Broadcast monitor successfully created at port: " + broadcastMonitorPort);
                    break;
                case START_SERVER:
                    logger.info("HttpServer successfully created at port: " + serverPort);
                    currentTaskFinished();
                    break;
                case FIND_SERVERS_TO_CONNECT:
                    Globals.get().connectedServers.put(currentConnectToServer, new ServerParam(future));
                    logger.info("Sever successfully connected: " + currentConnectToServer + ":" + clientPort);
                    break;
                case SEND_MAC_ADDRESSES:
                    logger.info("Send mac addresses was successful to server. " + currentServerParam.getKey() + ":" + clientPort);
                    break;
//                case SEND_DIR:
//                    try {
//                        String ipAndPort = future.channel().remoteAddress().toString();
//                        String ip = Globals.get().getIp(ipAndPort);
//                        ServerParam serverParam = Globals.get().connectedServers.get(ip);
//                        long lastDirSendingTime = serverParam.resetDir();
//                        long longestDirSending = longestDirSendingTime.get();
//                        while (longestDirSending < lastDirSendingTime) {
//                            if (longestDirSendingTime.compareAndSet(longestDirSending, lastDirSendingTime)) {
//                                logger.info("New longest dir sending time: " + lastDirSendingTime + " (" + ip + ").");
//                            }
//                            longestDirSending = longestDirSendingTime.get();
//                        }
//                    } catch (Exception e) {
//                        logger.error(Utils.getStackTrace(e));
//                    }
//                    break;
            }
        } else {
            switch (currentTask.getTaskToDo()) {
                case MONITOR_BROADCAST:
                    logger.error("Broadcast monitor could not started");
                    monitor.stop();
                    monitor = null;
                    break;
                case START_SERVER:
                    logger.error("HttpServer could not started at port: " + serverPort);
                    server.stop();
                    break;
                case FIND_SERVERS_TO_CONNECT:
                    logger.error("HttpServer could not connected: " + currentConnectToServer);
                    currentClient.stop();
                    break;
                case SEND_MAC_ADDRESSES:
                    logger.info("Send mac addresses was unsuccessful to server. " + currentServerParam.getKey() + ":" + clientPort);
                    currentServerParam.getValue().getSentAddresses().set(false);
                    break;
//                case SEND_DIR:
//                    try {
//                        String ipAndPort = future.channel().remoteAddress().toString();
//                        String ip = Globals.get().getIp(ipAndPort);
//                        ServerParam serverParam = Globals.get().connectedServers.get(ip);
//                        Globals.get().connectedServers.remove(ip);
//                        serverParam.getFuture().channel().close();
//                        logger.info("Send DIR EVENT was unsuccessful to server: " + ip + " - trying time was: " + serverParam.resetDir());
//                    } catch (Exception e) {
//                        logger.error(Utils.getStackTrace(e));
//                    }
//                    break;
            }
        }
    }

    @Override
    protected Task getTask() {
        return Globals.get().tasks.poll();
    }
}
