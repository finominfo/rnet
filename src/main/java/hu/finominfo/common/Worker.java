package hu.finominfo.common;

import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Created by kk on 2017.09.23..
 */
public abstract class Worker implements Runnable {

    private final static Logger logger = Logger.getLogger(Worker.class);

    protected volatile TaskToDo currentTask;
    private volatile long currentTaskStarted;


    @Override
    public void run() {
        if (null == currentTask) {
            if (!Globals.get().tasksToDo.isEmpty()) {
                currentTask = Globals.get().tasksToDo.poll();
                currentTaskStarted = System.currentTimeMillis();
            }
        }
        if (null != currentTask) {
            try {
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

    protected long currentTaskRunning() {
        return System.currentTimeMillis() - currentTaskStarted;
    }

    protected abstract void runCurrentTask();
}
