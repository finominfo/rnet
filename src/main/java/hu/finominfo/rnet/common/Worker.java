package hu.finominfo.rnet.common;

import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
            if (!Globals.get().tasks.isEmpty()) {
                currentTask = Globals.get().tasks.poll();
                currentTaskStarted = System.currentTimeMillis();
                lastHandling.set(0);
            }
        }
        if (null != currentTask) {
            try {
                //logger.info("CURRENT TASK: " + currentTask.getTaskToDo().toString());
                runCurrentTask();
            } catch(Exception e) {
                logger.error(currentTask, e);
                currentTaskFinished();
            }
        }
        checkNext();
    }

    private void checkNext() {
        Globals.get().executor.schedule(this, 100, TimeUnit.MILLISECONDS);
    }

    protected void currentTaskFinished() {
        currentTask = null;
    }

    protected boolean currentTaskRunning(long millis) {
        return System.currentTimeMillis() - currentTaskStarted > millis;
    }

    protected boolean shouldHandleAgain(long millis) {
        long now = System.currentTimeMillis();
        long last = lastHandling.get();
        if ((now - last > millis) && lastHandling.compareAndSet(last, now)) {
            return true;
        }
        return false;
    }
    protected abstract void runCurrentTask();
}
