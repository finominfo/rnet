package hu.finominfo.rnet.communication.tcp.events.file;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.taskqueue.TaskToDo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class FileEventHandler extends SimpleChannelInboundHandler<FileEvent> implements Runnable {
    private final static Logger logger = Logger.getLogger(FileEventHandler.class);
    private final ConcurrentMap<ChannelHandlerContext, Queue<FileEvent>> fileEvents = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileEvent msg) throws Exception {
        if (isClean(ctx, msg)) {
            getFileEvents(ctx).add(msg);
            startSaving();
        } else {
            logger.error("FILE PART WAS DROPPED BECAUSE OF PARALLEL SENDING: " + msg.getName());
        }
    }

    private boolean isClean(ChannelHandlerContext ctx, FileEvent msg) {
        Set<String> names = new HashSet<>();
        lock.lock();
        try {
            fileEvents.entrySet().stream()
                    .filter(ctxEntry -> ctxEntry.getKey() != ctx)
                    .forEach(ctxEntry2 -> ctxEntry2.getValue().stream().forEach(fileEvent -> names.add(fileEvent.getName())));
            return !names.contains(msg.getName());
        } finally {
            lock.unlock();
        }
    }


    private Queue<FileEvent> getFileEvents(ChannelHandlerContext ctx) {
        Queue<FileEvent> events = this.fileEvents.get(ctx);
        if (events == null) {
            Queue<FileEvent> eventsTemp = new ConcurrentLinkedQueue<>();
            events = fileEvents.putIfAbsent(ctx, eventsTemp);
            if (events == null) {
                events = eventsTemp;
            }
        }
        return events;
    }

    private void startSaving() {
        if (running.compareAndSet(false, true)) {
            Globals.get().executor.schedule(this, 100, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void run() {
        try {
            FileEvent fileEvent;
            while ((fileEvent = pollOne()) != null) {
                if (fileEvents.size() > 5) {
                    Globals.get().addToTasksIfNotExists(TaskToDo.SEND_WAIT);
                }
                saveFile(fileEvent);
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            running.set(false);
        }
        if (peekOne() != null) {
            startSaving();
        }
    }

    private FileEvent pollOne() {
        for (Queue<FileEvent> events : fileEvents.values()) {
            if (!events.isEmpty())
                return events.poll();
        }
        return null;
    }

    private FileEvent peekOne() {
        for (Queue<FileEvent> events : fileEvents.values()) {
            if (!events.isEmpty())
                return events.peek();
        }
        return null;
    }

    private void saveFile(FileEvent msg) throws Exception {
        String path = null;
        switch (msg.getFileType()) {
            case MAIN:
                path = ".";
                break;
            case VIDEO:
                path = Globals.videoFolder;
                break;
            case AUDIO:
                path = Globals.audioFolder;
                break;
            case PICTURE:
                path = Globals.pictureFolder;
                break;
        }
        String fullName = path + File.separator + msg.getName();
        if (msg.isFirstPart()) {
            try {
                boolean result = Files.deleteIfExists(new File(fullName).toPath());
            } catch (Exception e) {
                logger.error("Could not delete file: " + fullName);
            }
        }
        try (FileOutputStream fos = new FileOutputStream(fullName, true)) {
            fos.write(msg.getData());
            fos.flush();
            fos.close();
            if (msg.isLastPart()) {
                logger.info("FILE HAS BEEN WRITTEN SUCCESSFULLY: " + fullName);
                Globals.get().addToTasksIfNotExists(TaskToDo.SEND_DIR);
                if (msg.getFileType() == FileType.MAIN && msg.getName().endsWith(Globals.JAR_NAME)) {
                    Utils.restartApplication();
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

}
