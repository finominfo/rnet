package hu.finominfo.rnet.common;

import hu.finominfo.rnet.communication.tcp.client.Client;
import hu.finominfo.rnet.communication.tcp.client.ServerParam;
import hu.finominfo.rnet.communication.tcp.events.file.FileType;
import hu.finominfo.rnet.communication.tcp.server.Server;
import hu.finominfo.rnet.communication.udp.Connection;
import hu.finominfo.rnet.communication.tcp.events.Event;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import hu.finominfo.rnet.communication.udp.in.ConnectionMonitor;
import hu.finominfo.rnet.communication.udp.out.ConnectionBroadcaster;
import hu.finominfo.rnet.frontend.controller.FrontEnd;
import hu.finominfo.rnet.taskqueue.FrontEndTaskToDo;
import hu.finominfo.rnet.taskqueue.Task;
import hu.finominfo.rnet.taskqueue.TaskToDo;
import hu.finominfo.rnet.taskqueue.Worker;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class Globals {

    private static Globals ourInstance = new Globals();

    public static Globals get() {
        return ourInstance;
    }

    public final static int VERSION = 44;
    public final static String JAR_NAME = "rnet.jar";
    public volatile Task currentTask = null;
    public volatile Worker controller = null;
    public volatile Worker servant = null;
    public volatile Server server = null;
    public volatile Client client = null;
    public volatile ConnectionBroadcaster broadcaster= null;
    public volatile ConnectionMonitor monitor = null;
    public volatile hu.finominfo.rnet.frontend.servant.counter.Panel counter = null;


    private final static Logger logger = Logger.getLogger(Globals.class);
    public final AtomicLong shouldWait = new AtomicLong(0);
    public final static String ADDRESSES = "addresses.txt";
    public final static String ADDRESS = "status.txt";
    public final static String videoFolder = "video";
    public final static String audioFolder = "audio";
    public final static String pictureFolder = "picture";
    public final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(4);
    public final Queue<Event> events = new ConcurrentLinkedQueue<>();
    public final ConcurrentHashMap.KeySetView<Connection, Boolean> connections = ConcurrentHashMap.newKeySet();
    public final ConcurrentMap<String, ClientParam> serverClients = new ConcurrentHashMap<>(); //ip and port - context
    public final ConcurrentMap<String, ServerParam> connectedServers = new ConcurrentHashMap<>();
    public final ConcurrentMap<String, List<Long>> clientNameAddress = new ConcurrentHashMap<>();
    public final Queue<Task> tasks = new ConcurrentLinkedQueue<>();
    public final Queue<Task> frontEndTasks = new ConcurrentLinkedQueue<>();
    private volatile FrontEnd frontEnd = null;

    public static String getVersion() {
        int main = VERSION / 100;
        int sub = VERSION - main * 100;
        if (sub % 10 == 0) {
            sub /= 10;
        }
        return "" + main + "." + sub;
    }

    public FrontEnd getFrontEnd() {
        if (frontEnd == null) {
            frontEnd = new FrontEnd();
        }
        return frontEnd;
    }

    public boolean isTasksEmpty() {
        return tasks.isEmpty();
    }

    public boolean isFrontEndTasksEmpty() {
        return frontEndTasks.isEmpty();
    }

    public final void addToTasksIfNotExists(TaskToDo taskToDo) {
        Task task = new Task(taskToDo);
        if (!tasks.contains(task)) {
            tasks.add(task);
        }
    }

    public final void addToFrontEndTasksIfNotExists(FrontEndTaskToDo taskToDo) {
        Task task = new Task(taskToDo);
        if (!frontEndTasks.contains(task)) {
            frontEndTasks.add(task);
        }
    }

    public final void addToTasksIfEmpty(TaskToDo taskToDo) {
        if (tasks.isEmpty()) {
            tasks.add(new Task(taskToDo));
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

    public void restart() {
        //TODO: Executor-okat shutdown-nolni!!!
        StringBuilder cmd = new StringBuilder();
        cmd.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java ");
        for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            cmd.append(jvmArg + " ");
        }
        cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
        cmd.append(Window.class.getName()).append(" ");

        try {
            Runtime.getRuntime().exec(cmd.toString());
        } catch (IOException e) {
            logger.error(Utils.getStackTrace(e));
        }
        System.exit(0);
    }
}
