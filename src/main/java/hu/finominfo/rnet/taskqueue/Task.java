package hu.finominfo.rnet.taskqueue;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.events.file.FileType;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.23.
 */
public class Task {
    private final TaskToDo taskToDo;
    private final FrontEndTaskToDo frontEndTaskToDo;
    private final String name;
    private final String toSend;
    private final FileType fileType;
    private final AtomicInteger filePosition = new AtomicInteger(0);
    private final AtomicInteger currentLength = new AtomicInteger(0);
    private final AtomicInteger counter = new AtomicInteger(0);
    private final AtomicInteger parallelSending = new AtomicInteger(0);
    private final AtomicInteger time = new AtomicInteger(0);
    private final AtomicBoolean isLast = new AtomicBoolean(false);
    private final AtomicBoolean taskSendingFinished = new AtomicBoolean(true);
    private volatile Event event = null;



    public Task(TaskToDo taskToDo, Event event) {
        this.taskToDo = taskToDo;
        this.frontEndTaskToDo = null;
        this.name = null;
        this.fileType = null;
        this.toSend = null;
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public Task(TaskToDo taskToDo, String name) {
        this.taskToDo = taskToDo;
        this.frontEndTaskToDo = null;
        this.name = name;
        this.fileType = null;
        this.toSend = null;
    }

    public Task(TaskToDo taskToDo) {
        this.taskToDo = taskToDo;
        this.frontEndTaskToDo = null;
        this.name = null;
        this.fileType = null;
        this.toSend = null;
    }

    public Task(FrontEndTaskToDo taskToDo) {
        this.taskToDo = null;
        this.frontEndTaskToDo = taskToDo;
        this.name = null;
        this.fileType = null;
        this.toSend = null;
    }


    public Task(TaskToDo taskToDo, String name, FileType fileType) {
        this.taskToDo = taskToDo;
        this.frontEndTaskToDo = null;
        this.name = name;
        this.fileType = fileType;
        this.toSend = null;
    }

    public Task(TaskToDo taskToDo, String name, FileType fileType, String toSend) {
        this.taskToDo = taskToDo;
        this.frontEndTaskToDo = null;
        this.name = name;
        this.fileType = fileType;
        this.toSend = toSend;
    }

    public Task(TaskToDo taskToDo, String name, FileType fileType, String toSend, int time) {
        this.taskToDo = taskToDo;
        this.frontEndTaskToDo = null;
        this.name = name;
        this.fileType = fileType;
        this.toSend = toSend;
        this.time.set(time);
    }

    public String getPathFromFileType() {
        String path = "";
        switch (getFileType()) {
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
        return path;
    }

    public int getTime() {
        return this.time.get();
    }

    public AtomicInteger getParallelSending() {
        return parallelSending;
    }

    public FrontEndTaskToDo getFrontEndTaskToDo() {
        return frontEndTaskToDo;
    }

    public TaskToDo getTaskToDo() {
        return taskToDo;
    }

    public String getName() {
        return name;
    }

    public String getToSend() {
        return toSend;
    }

    public FileType getFileType() {
        return fileType;
    }

    public AtomicInteger getFilePosition() {
        return filePosition;
    }

    public AtomicInteger getCurrentLength() {
        return currentLength;
    }

    public AtomicBoolean getIsLast() {
        return isLast;
    }

    public AtomicInteger getCounter() {
        return counter;
    }

    public AtomicBoolean getTaskSendingFinished() {
        return taskSendingFinished;
    }
}
