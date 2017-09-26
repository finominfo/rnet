package hu.finominfo.rnet.taskqueue;

import hu.finominfo.rnet.common.Globals;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.24.
 */
public class ControllerRepeater implements Runnable {
    private final static Logger logger = Logger.getLogger(ControllerRepeater.class);
    private volatile long counter = 0;

    @Override
    public void run() {
        try {
            counter++;
            if (Globals.get().isTasksEmpty() && ((counter & 0x1f) == 0)) {
                Globals.get().addToTasksIfNotExists(TaskToDo.SEND_BROADCAST);
            }
            if (Globals.get().isFrontEndTasksEmpty()) {
                Globals.get().addToFrontEndTasksIfNotExists(TaskToDo.REFRESH_SERVANT_LIST);
            }
        }catch (Exception e) {
            logger.error(e);
        }
        Globals.get().executor.schedule(this, 1, TimeUnit.SECONDS);
    }
}
