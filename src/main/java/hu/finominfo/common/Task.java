package hu.finominfo.common;

import hu.finominfo.rnet.communication.tcp.events.file.FileType;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.23.
 */
public class Task {
    private final TaskToDo taskToDo;
    private final String name;
    private final FileType fileType;

    public Task(TaskToDo taskToDo) {
        this.taskToDo = taskToDo;
        this.name = null;
        this.fileType = null;
    }

    public Task(TaskToDo taskToDo, String name) {
        this.taskToDo = taskToDo;
        this.name = name;
        this.fileType = null;
    }

    public Task(TaskToDo taskToDo, String name, FileType fileType) {
        this.taskToDo = taskToDo;
        this.name = name;
        this.fileType = fileType;
    }

    public TaskToDo getTaskToDo() {
        return taskToDo;
    }

    public String getName() {
        return name;
    }

    public FileType getFileType() {
        return fileType;
    }
}
