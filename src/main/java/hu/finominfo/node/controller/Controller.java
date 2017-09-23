package hu.finominfo.node.controller;

import hu.finominfo.common.Globals;
import hu.finominfo.common.Props;
import hu.finominfo.common.TaskToDo;
import hu.finominfo.common.Worker;
import hu.finominfo.rnet.communication.udp.Broadcaster;
import hu.finominfo.node.CompletedEvent;
import hu.finominfo.rnet.communication.tcp.client.Client;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import hu.finominfo.rnet.communication.tcp.server.Server;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;

import java.nio.channels.CompletionHandler;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21.
 */
public class Controller extends Worker implements CompletionHandler<CompletedEvent, Integer>, ChannelFutureListener {

    private final static Logger logger = Logger.getLogger(Controller.class);
    private volatile Broadcaster broadcaster = null;
    private volatile Server server;
    private final int broadcastPort;
    private final int clientPort;
    private final int serverPort;
    private volatile Map.Entry<String, ClientParam> currentClient = null;
    private volatile long lastBroadcast = 0;

    public Controller() {
        broadcastPort = Props.get().getPort();
        clientPort = broadcastPort + 1;
        serverPort = broadcastPort + 2;
        Globals.get().tasksToDo.add(TaskToDo.START_SERVER);
        Globals.get().tasksToDo.add(TaskToDo.SEND_BROADCAST);
        Globals.get().tasksToDo.add(TaskToDo.FIND_SERVERS_TO_CONNECT);
    }

    @Override
    public void runCurrentTask() {
        switch (currentTask) {
            case START_SERVER:
                server = new Server(serverPort);
                server.bind().addListener(this);
                break;
            case SEND_BROADCAST:
                if (System.currentTimeMillis() - lastBroadcast > 10_000 && broadcaster == null) {
                    lastBroadcast = System.currentTimeMillis();
                    broadcaster = new Broadcaster(broadcastPort);
                    broadcaster.start(7, 1000, this);
                }
                break;
            case FIND_SERVERS_TO_CONNECT:
                boolean foundNewClient = false;
                Iterator<Map.Entry<String, ClientParam>> iterator = Globals.get().serverClients.entrySet().iterator();
                while (iterator.hasNext()) {
                    currentClient = iterator.next();
                    ClientParam clientParam = currentClient.getValue();
                    if (clientParam.possibleToTry() && null != clientParam.getContext() && clientParam.getConnectedBack().compareAndSet(false, true)) {
                        Client client = new Client(currentClient.getKey(), clientPort);
                        clientParam.setClient(client);
                        client.bind().addListener(this);
                        foundNewClient = true;
                        break;
                    }
                }
                if (!foundNewClient && currentTaskRunning() > 5000) {
                    currentTaskFinished();
                }
                break;
        }
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            switch (currentTask) {
                case START_SERVER:
                    logger.info("Server successful created at port: " + serverPort);
                    currentTaskFinished();
                    break;
                case FIND_SERVERS_TO_CONNECT:
                    logger.info("Client successful connected back: " + currentClient.getKey() + " " + clientPort);
                    break;
            }
        } else {
            switch (currentTask) {
                case START_SERVER:
                    logger.error("Server could not started at port: " + serverPort);
                    server.stop();
                    break;
                case FIND_SERVERS_TO_CONNECT:
                    currentClient.getValue().getClient().stop();
                    currentClient.getValue().getConnectedBack().set(false);
                    currentClient.getValue().setLastTrying();
                    logger.error("Client could not connected back: " + currentClient.getKey() + " " + clientPort);
                    break;
            }
        }
    }

    @Override
    public void completed(CompletedEvent result, Integer attachment) {
        switch (result) {
            case BROADCAST_FINISHED:
                broadcaster.stop();
                broadcaster = null;
                if (currentTaskRunning() > 5000 && !Globals.get().serverClients.isEmpty() ) {
                //if (currentTaskRunning() > 5000) {
                    currentTaskFinished();
                }
                break;
        }
    }

    @Override
    public void failed(Throwable exc, Integer attachment) {
        logger.error(currentTask, exc);
        currentTaskFinished();
    }

}
