package hu.finominfo.rnet.frontend;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.SynchronousWorker;
import hu.finominfo.rnet.common.Task;
import org.apache.log4j.Logger;

import java.util.Queue;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.25.
 */
public class FrontEndWorker extends SynchronousWorker {
    private final static Logger logger = Logger.getLogger(FrontEndWorker.class);

    @Override
    public void runCurrentAsynchronousTask() {
        logger.info("dfasdsdaerwqscderwfgdrewdfgsr");
        switch (currentTask.getTaskToDo()) {
            default:
                logger.error("Not implemented task: " + currentTask.getTaskToDo().toString());
                currentTaskFinished();
        }
    }

    @Override
    protected Queue<Task> getTaskQueue() {
        return Globals.get().frontEndTasks;
    }
}
