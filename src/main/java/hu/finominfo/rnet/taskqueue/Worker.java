package hu.finominfo.rnet.taskqueue;

import hu.finominfo.rnet.common.Globals;
import org.apache.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kk on 2017.09.23..
 */
public abstract class Worker implements Runnable {

    private final static Logger logger = Logger.getLogger(Worker.class);

    protected volatile Task currentTask;
    private volatile long currentTaskStarted = 0;
    private final AtomicLong lastHandling = new AtomicLong(0);


    @Override
    public void run() {
        if (null == currentTask) {
            currentTask = getTask();
            if (null != currentTask && currentTask.getTaskToDo() == TaskToDo.SEND_FILE)  {
                Globals.get().shouldWait.set(0);
            }
            currentTaskStarted = System.currentTimeMillis();
            lastHandling.set(0);
        }
        if (null != currentTask) {
            try {
                Globals.get().currentTask = currentTask;
                //logger.info("CURRENT TASK: " + currentTask.getTaskToDo().toString());
                runCurrentTask();
            } catch (Exception e) {
                logger.error(currentTask, e);
                currentTaskFinished();
            }
        }
        checkNext();
    }

    private void checkNext() {
        long time = 100;
        if (null != currentTask && currentTask.getTaskToDo() == TaskToDo.SEND_FILE) {
            time = 200 + Globals.get().shouldWait.getAndSet(0);
        }
        Globals.get().executor.schedule(this, time, TimeUnit.MILLISECONDS);
    }

    protected void currentTaskFinished() {
        currentTask = null;
        Globals.get().currentTask = null;
    }

    protected boolean currentTaskRunning(long millis) {
        return System.currentTimeMillis() - currentTaskStarted > millis;
    }

    protected boolean shouldHandleAgain(long millis) {
        long now = System.currentTimeMillis();
        long last = lastHandling.get();
        return (now - last > millis) && lastHandling.compareAndSet(last, now);
    }

    protected abstract void runCurrentTask();

    protected abstract Task getTask();
}
