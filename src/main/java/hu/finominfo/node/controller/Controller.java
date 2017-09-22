package hu.finominfo.node.controller;

import hu.finominfo.common.Globals;
import hu.finominfo.common.Props;
import hu.finominfo.node.EventToDo;
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
public class Controller implements CompletionHandler<CompletedEvent, Integer>, Runnable, ChannelFutureListener {

    private final static Logger logger = Logger.getLogger(Controller.class);
    private volatile EventToDo eventToDo;
    private volatile Broadcaster broadcaster;
    private volatile Server server;
    private final int broadcastPort;
    private final int clientPort;
    private final int serverPort;
    private volatile Map.Entry<String, ClientParam> currentClient = null;

    public Controller() {
        broadcastPort = Props.get().getPort();
        clientPort = broadcastPort + 1;
        serverPort = broadcastPort + 2;
        eventToDo = EventToDo.START_SERVER;
    }

    @Override
    public void run() {
        switch (eventToDo) {
            case START_SERVER:
                server = new Server(serverPort);
                server.bind().addListener(this);
                break;
            case BROADCAST:
                broadcaster = new Broadcaster(broadcastPort);
                broadcaster.start(4, 2, this);
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
                if (!foundNewClient) {
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
            switch (eventToDo) {
                case START_SERVER:
                    logger.info("Server successful created at port: " + serverPort);
                    eventToDo = EventToDo.BROADCAST;
                    break;
                case FIND_SERVERS_TO_CONNECT:
                    logger.info("Client successful connected back: " + currentClient.getKey() + " " + clientPort);
                    break;
            }
        } else {
            switch (eventToDo) {
                case START_SERVER:
                    logger.error("Server could not started at port: " + serverPort);
                    server.stop();
                    break;
                case FIND_SERVERS_TO_CONNECT:
                    currentClient.getValue().getConnectedBack().set(false);
                    currentClient.getValue().setLastTrying();
                    logger.error("Client could not connected back: " + currentClient.getKey() + " " + clientPort);
                    break;
            }
        }
        runLater();
    }

    @Override
    public void completed(CompletedEvent result, Integer attachment) {
        switch (result) {
            case BROADCAST_FINISHED:
                broadcaster.stop();
                if (attachment > 1 && !Globals.get().serverClients.isEmpty()) {
                    eventToDo = EventToDo.FIND_SERVERS_TO_CONNECT;
                }
                break;
        }
        runLater();
    }

    @Override
    public void failed(Throwable exc, Integer attachment) {
        logger.error(exc);
        runLater();
    }

}
