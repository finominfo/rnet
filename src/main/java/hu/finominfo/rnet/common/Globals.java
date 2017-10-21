package hu.finominfo.rnet.common;

import hu.finominfo.rnet.audio.AudioPlayer;
import hu.finominfo.rnet.audio.AudioPlayerContinuous;
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
import hu.finominfo.rnet.frontend.controller.allcounter.AllCounter;
import hu.finominfo.rnet.taskqueue.FrontEndTaskToDo;
import hu.finominfo.rnet.taskqueue.Task;
import hu.finominfo.rnet.taskqueue.TaskToDo;
import hu.finominfo.rnet.taskqueue.Worker;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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

    public final int width;
    public final int height;
    public final double diff;
    public static final int benchmarkWidth = 1920;


    private Globals() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        width = gd.getDisplayMode().getWidth();
        height = gd.getDisplayMode().getHeight();
        diff = ((double) width) / benchmarkWidth;
    }

    public final static int VERSION = 115;
    public final static String JAR_NAME = "rnet.jar";
    public final static String PROP_NAME = "config.properties";
    public volatile Task currentTask = null;
    public volatile Worker controller = null;
    public volatile Worker servant = null;
    public volatile Server server = null;
    public volatile Client client = null;
    public volatile ConnectionBroadcaster broadcaster = null;
    public volatile ConnectionMonitor monitor = null;
    public volatile hu.finominfo.rnet.frontend.servant.counter.Panel counter = null;
    public volatile AudioPlayer audioPlayer = null;
    public volatile AudioPlayerContinuous audioPlayerContinuous = null;
    public volatile Status status = new Status();


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
    private volatile AllCounter allCounter = null;

    public static String getVersion() {
        int main = VERSION / 100;
        int sub = VERSION - main * 100;
        String subStr;
        if (sub % 10 == 0) {
            sub /= 10;
        }
        return "" + main + "." + (sub < 10 ? ("0" + sub) : sub);
    }

    public FrontEnd getFrontEnd() {
        if (frontEnd == null) {
            frontEnd = new FrontEnd();
        }
        return frontEnd;
    }

    public AllCounter getAllCounter() {
        return allCounter;
    }

    public AllCounter getAllCounterWithCreateIfNecessary() {
        if (allCounter == null) {
            Font customFont = Utils.getCustomFont();
            allCounter = new AllCounter(customFont);
            Utils.createAndShowGui(null, true, allCounter, customFont, "All counters", new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    allCounter = null;
                    e.getWindow().dispose();
                }
            }, new KeyAdapter() {
                public void keyPressed(KeyEvent ke) {  // handler
                    if (ke.getKeyCode() == ke.VK_ESCAPE) {
                        allCounter = null;
                        ((JFrame) ke.getComponent()).dispose();
                    }
                }
            });
        }

    return allCounter;
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
