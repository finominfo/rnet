package hu.finominfo.rnet.common;

import hu.finominfo.rnet.communication.tcp.events.file.FileType;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.23.
 */
public class Task {
    private final TaskToDo taskToDo;
    private final String name;
    private final String toSend;
    private final FileType fileType;
    private final AtomicInteger filePosition = new AtomicInteger(0);
    private final AtomicInteger currentLength = new AtomicInteger(0);
    private final AtomicInteger counter = new AtomicInteger(0);
    private final AtomicBoolean isLast = new AtomicBoolean(false);


    public Task(TaskToDo taskToDo) {
        this.taskToDo = taskToDo;
        this.name = null;
        this.fileType = null;
        this.toSend = null;
    }

    public Task(TaskToDo taskToDo, String name) {
        this.taskToDo = taskToDo;
        this.name = name;
        this.fileType = null;
        this.toSend = null;
    }

    public Task(TaskToDo taskToDo, String name, FileType fileType) {
        this.taskToDo = taskToDo;
        this.name = name;
        this.fileType = fileType;
        this.toSend = null;
    }

    public Task(TaskToDo taskToDo, String name, FileType fileType, String toSend) {
        this.taskToDo = taskToDo;
        this.name = name;
        this.fileType = fileType;
        this.toSend = toSend;
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
}
