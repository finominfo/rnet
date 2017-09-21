package hu.finominfo.node.controller;

import hu.finominfo.common.Globals;
import hu.finominfo.common.PropReader;
import hu.finominfo.node.EventToDo;
import hu.finominfo.rnet.communication.connection.Broadcaster;
import hu.finominfo.node.CompletedEvent;
import org.apache.log4j.Logger;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21.
 */
public class Controller implements CompletionHandler<CompletedEvent, Integer>, Runnable {

    private final static Logger logger = Logger.getLogger(Controller.class);
    private volatile EventToDo eventToDo;
    private volatile Broadcaster broadcaster;

    public Controller() {
        eventToDo = EventToDo.BROADCAST;
    }

    @Override
    public void run() {
        switch (eventToDo) {
            case BROADCAST:
                broadcaster = new Broadcaster(Integer.valueOf(PropReader.getSingletonInstance().getProperties().getProperty("port", "10000")));
                broadcaster.start(4, 2, this);
                break;
        }
    }

    @Override
    public void completed(CompletedEvent result, Integer attachment) {
        switch (result) {
            case BROADCAST_FINISHED:
                broadcaster.stop();
                if (attachment < 2) {
                    eventToDo = EventToDo.BROADCAST;
                    Globals.executor.schedule(this, 5, TimeUnit.SECONDS);
                } else {

                }
                break;
        }

    }

    @Override
    public void failed(Throwable exc, Integer attachment) {
        logger.equals(exc);
        Globals.executor.schedule(this, 5, TimeUnit.SECONDS);
    }
}
