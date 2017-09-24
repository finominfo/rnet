package hu.finominfo.rnet.common;

import hu.finominfo.rnet.communication.tcp.client.ServerParam;
import hu.finominfo.rnet.communication.tcp.events.file.FileType;
import hu.finominfo.rnet.communication.udp.Connection;
import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class Globals {

    private static Globals ourInstance = new Globals();

    public static Globals get() {
        return ourInstance;
    }

    public final static int VERSION = 1;
    public final static String ADDRESSES = "addresses.txt";
    public final String videoFolder = "video";
    public final String audioFolder = "audio";
    public final String pictureFolder = "picture";
    public final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(4);
    public final Queue<Event> events = new ConcurrentLinkedQueue<>();
    public final ConcurrentHashMap.KeySetView<Connection, Boolean> connections = ConcurrentHashMap.newKeySet();
    public final ConcurrentMap<String, ClientParam> serverClients = new ConcurrentHashMap<>(); //ip and port - context
    public final ConcurrentMap<String, ServerParam> connectedServers = new ConcurrentHashMap<>();
    public final ConcurrentMap<String, List<Long>> clientNameAddress = new ConcurrentHashMap<>();
    final Queue<Task> tasks = new ConcurrentLinkedQueue<>();

    public final void addToTasksIfNotExists(TaskToDo taskToDo) {
        Task task = new Task(taskToDo);
        if (!tasks.contains(task)) {
            tasks.add(task);
        }
    }

    public final void addToTasksIfNotExists(TaskToDo taskToDo, String name, FileType fileType) {
        Task task = new Task(taskToDo, name, fileType);
        if (!tasks.contains(task)) {
            tasks.add(task);
        }
    }
    public final void addToTasksIfNotExists(TaskToDo taskToDo, String name, FileType fileType, String toSend) {
        Task task = new Task(taskToDo, name, fileType, toSend);
        if (!tasks.contains(task)) {
            tasks.add(task);
        }
    }

    public final void addToTasksForced(TaskToDo taskToDo) {
        tasks.add(new Task(taskToDo));
    }

    public final String getIp(String ipAndPort) {
        int pos = ipAndPort.lastIndexOf(':');
        if (ipAndPort.startsWith("/")) {
            return ipAndPort.substring(1, pos);
        } else {
            return ipAndPort.substring(0, pos);
        }
    }
}
