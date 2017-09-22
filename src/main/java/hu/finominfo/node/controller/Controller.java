package hu.finominfo.node.controller;

import hu.finominfo.common.Globals;
import hu.finominfo.common.Props;
import hu.finominfo.node.EventToDo;
import hu.finominfo.rnet.communication.Interface;
import hu.finominfo.rnet.communication.connection.Broadcaster;
import hu.finominfo.node.CompletedEvent;
import hu.finominfo.rnet.communication.data.client.Client;
import hu.finominfo.rnet.communication.data.server.ClientParam;
import hu.finominfo.rnet.communication.data.server.Server;
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
        eventToDo = EventToDo.SERVER;
    }

    @Override
    public void run() {
        switch (eventToDo) {
            case SERVER:
                server = new Server(serverPort);
                server.bind().addListener(this);
                break;
            case BROADCAST:
                broadcaster = new Broadcaster(broadcastPort);
                broadcaster.start(4, 2, this);
                break;
            case CLIENT:
                boolean foundNewClient = false;
                Iterator<Map.Entry<String, ClientParam>> iterator = Globals.get().clients.entrySet().iterator();
                    while (iterator.hasNext()) {
                        currentClient = iterator.next();
                        if (currentClient.getValue().getConnectedBack().compareAndSet(false, true)) {
                            String address = Globals.get().getIp(currentClient.getKey());
                            Client client = new Client(address, clientPort);
                            currentClient.getValue().setClient(client);
                            client.bind().addListener(this);
                            foundNewClient = true;
                            break;
                        }
                    }
                if (!foundNewClient) {
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
                case SERVER:
                    eventToDo = EventToDo.BROADCAST;
                    break;
                case CLIENT:
                    logger.info("Client successful connected back: " + Globals.get().getIp(currentClient.getKey()));
                    break;
            }
        } else {
            switch (eventToDo) {
                case SERVER:
                    logger.error("Server could not started");
                    server.stop();
                    break;
                case CLIENT:
                    logger.error("Client could not connected back: " + Globals.get().getIp(currentClient.getKey()));
                    break;
            }
        }
        nextStart();
    }

    @Override
    public void completed(CompletedEvent result, Integer attachment) {
        switch (result) {
            case BROADCAST_FINISHED:
                broadcaster.stop();
                if (attachment > 1) {
                    eventToDo = EventToDo.CLIENT;
                }
                break;
        }
        nextStart();
    }

    @Override
    public void failed(Throwable exc, Integer attachment) {
        logger.error(exc);
        nextStart();
    }

}
