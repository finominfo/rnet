package hu.finominfo.rnet.node.controller;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.TaskToDo;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.24.
 */
public class Repeater implements Runnable {
    private final static Logger logger = Logger.getLogger(Repeater.class);

    @Override
    public void run() {
        try {
            if (Globals.get().isTasksEmpty()) {
                Globals.get().addToTasksIfNotExists(TaskToDo.SEND_BROADCAST);
            }
        }catch (Exception e) {
            logger.error(e);
        }
        Globals.get().executor.schedule(this, 30, TimeUnit.SECONDS);
    }
}
