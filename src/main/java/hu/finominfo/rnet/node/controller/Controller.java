package hu.finominfo.rnet.node.controller;

import hu.finominfo.rnet.properties.Props;
import hu.finominfo.rnet.common.*;
import hu.finominfo.rnet.communication.tcp.client.ServerParam;
import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.del.DelFileEvent;
import hu.finominfo.rnet.communication.tcp.events.file.FileEvent;
import hu.finominfo.rnet.communication.tcp.events.message.MessageEvent;
import hu.finominfo.rnet.communication.tcp.events.picture.PictureEvent;
import hu.finominfo.rnet.communication.udp.Broadcaster;
import hu.finominfo.rnet.communication.tcp.client.Client;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import hu.finominfo.rnet.communication.tcp.server.Server;
import hu.finominfo.rnet.taskqueue.Task;
import hu.finominfo.rnet.taskqueue.TaskToDo;
import hu.finominfo.rnet.taskqueue.Worker;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.Map;

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

    public Controller() {
        broadcastPort = Props.get().getPort();
        clientPort = broadcastPort + 1;
        serverPort = broadcastPort + 2;
        Globals.get().addToTasksIfNotExists(TaskToDo.START_SERVER);
        Globals.get().addToTasksIfNotExists(TaskToDo.SEND_BROADCAST);
        //Globals.get().addToTasksIfNotExists(TaskToDo.FIND_SERVERS_TO_CONNECT);
        //Globals.get().addToTasksIfNotExists(TaskToDo.SEND_FILE, "netty-all-4.1.15.Final.jar", FileType.AUDIO, "192.168.0.111");
    }

    @Override
    public void runCurrentTask() {
        switch (currentTask.getTaskToDo()) {
            case START_SERVER:
                if (shouldHandleAgain(5000) && currentTask.getTaskSendingFinished().compareAndSet(true, false)) {
                    server = new Server(serverPort);
                    server.bind().addListener(this);
                }
                break;
            case SEND_BROADCAST:
                if (broadcaster == null && shouldHandleAgain(1000) && currentTask.getTaskSendingFinished().compareAndSet(true, false)) {
                    broadcaster = new Broadcaster(broadcastPort);
                    broadcaster.start(1, 400, this);
                }
                break;
            case FIND_SERVERS_TO_CONNECT:
                if (currentTask.getTaskSendingFinished().compareAndSet(true, false)) {
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
                        currentTaskFinished();
                    }
                }
                break;
            case DEL_FILE:
                try {
                    if (currentTask.getTaskSendingFinished().compareAndSet(true, false)) {
                        Globals.get().connectedServers.get(currentTask.getToSend()).getFuture().channel()
                                .writeAndFlush(new DelFileEvent(currentTask.getPathFromFileType(), currentTask.getName()));
                        logger.info("DEL_FILE sending, delete file name: " + currentTask.getName());
                    }
                } catch (Exception e) {
                    logger.error(e);
                } finally {
                    currentTaskFinished();
                }
                break;
            case SEND_MESSAGE:
                try {
                    if (currentTask.getTaskSendingFinished().compareAndSet(true, false)) {
                        Globals.get().connectedServers.get(currentTask.getToSend()).getFuture().channel()
                                .writeAndFlush(new MessageEvent(currentTask.getName(), currentTask.getTime()));
                        logger.info("MESSAGE sending, text: " + currentTask.getName());
                    }
                } catch (Exception e) {
                    logger.error(e);
                } finally {
                    currentTaskFinished();
                }
                break;
            case SEND_PICTURE:
                try {
                    if (currentTask.getTaskSendingFinished().compareAndSet(true, false)) {
                        Globals.get().connectedServers.get(currentTask.getToSend()).getFuture().channel()
                                .writeAndFlush(new PictureEvent(currentTask.getPathFromFileType(), currentTask.getName(), currentTask.getTime()));
                        logger.info("PICTURE sending, name: " + currentTask.getName());
                    }
                } catch (Exception e) {
                    logger.error(e);
                } finally {
                    currentTaskFinished();
                }
                break;
            case SEND_FILE:
                if (currentTask.getTaskSendingFinished().compareAndSet(true, false)) {
                    if (currentTask.getIsLast().get()) {
                        currentTaskFinished();
                        return;
                    }
                    try {
                        File file = new File(currentTask.getName());
                        long length = file.length();
                        long remaining = length - currentTask.getFilePosition().get();
                        if (remaining == 0) {
                            currentTaskFinished();
                            return;
                        }
                        boolean isLast = remaining <= Event.MAX_BINARY_SIZE;
                        boolean isFirst = currentTask.getFilePosition().get() == 0;
                        currentTask.getIsLast().set(isLast);
                        int smaller = isLast ? (int) remaining : Event.MAX_BINARY_SIZE;
                        byte[] bytes = new byte[smaller];
                        ByteBuffer buffer = ByteBuffer.wrap(bytes);
                        RandomAccessFile raf = new RandomAccessFile(file, "r");
                        FileChannel fc = raf.getChannel();
                        fc.position(currentTask.getFilePosition().get());
                        fc.read(buffer);
                        byte[] data = buffer.array();
                        currentTask.getFilePosition().addAndGet(data.length);
                        currentTask.getCurrentLength().set(data.length);
                        int pos = currentTask.getName().lastIndexOf(File.separatorChar);
                        String shortName = currentTask.getName().substring(pos + 1);
                        logger.info("shortName: " + shortName);
                        FileEvent fileEvent = new FileEvent(currentTask.getFileType(), data, shortName, isFirst, isLast);
                        Globals.get().connectedServers.get(currentTask.getToSend()).getFuture().channel().writeAndFlush(fileEvent).addListener(this);
                    } catch (Exception e) {
                        logger.error(Utils.getStackTrace(e));
                        currentTaskFinished();
                    }
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
                case START_SERVER:
                    logger.info("Server successful created at port: " + serverPort);
                    currentTaskFinished();
                    break;
                case FIND_SERVERS_TO_CONNECT:
                    Globals.get().connectedServers.put(currentClient.getKey(), new ServerParam(future));
                    logger.info("Client successful connected back: " + currentClient.getKey() + " " + clientPort);
                    break;
                case SEND_FILE:
                    int from = currentTask.getFilePosition().get() - currentTask.getCurrentLength().get();
                    logger.info("FILE sending was successful position from: " + from + " to: " + currentTask.getFilePosition());
                    break;
            }
        } else {
            switch (currentTask.getTaskToDo()) {
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
                case SEND_FILE:
                    if (currentTask.getCounter().incrementAndGet() > 50) {
                        currentTaskFinished();
                    } else {
                        int from = currentTask.getFilePosition().get() - currentTask.getCurrentLength().get();
                        logger.info("FILE sending was failed position from: " + from + " to: " + currentTask.getFilePosition());
                        currentTask.getFilePosition().set(from);
                    }
                    break;
            }
        }
    }

    @Override
    public void completed(CompletedEvent result, Integer attachment) {
        currentTask.getTaskSendingFinished().set(true);
        switch (result) {
            case BROADCAST_FINISHED:
                broadcaster.stop();
                broadcaster = null;
                if (!Globals.get().serverClients.isEmpty()) {
                    currentTaskFinished();
                }
                break;
        }
    }

    @Override
    public void failed(Throwable exc, Integer attachment) {
        currentTask.getTaskSendingFinished().set(true);
        logger.error(currentTask, exc);
        broadcaster.stop();
        broadcaster = null;
    }

    @Override
    protected Task getTask() {
        return Globals.get().tasks.poll();
    }
}
