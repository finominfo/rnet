package hu.finominfo.rnet.taskqueue;

import hu.finominfo.rnet.common.Globals;
import io.netty.util.internal.ThreadLocalRandom;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.24.
 */
public class ServantRepeater implements Runnable {
    private final static Logger logger = Logger.getLogger(ServantRepeater.class);
    private volatile long nextDirSend = System.currentTimeMillis();

    @Override
    public void run() {
        try {
            long now = System.currentTimeMillis();
            if (nextDirSend < now && Globals.get().isTasksEmpty()) {
                nextDirSend = now + ThreadLocalRandom.current().nextInt(1000, 2001);
                Globals.get().addToTasksIfNotExists(TaskToDo.SEND_DIR);
            }
        }catch (Exception e) {
            logger.error(e);
        }
        Globals.get().executor.schedule(this, 100, TimeUnit.MILLISECONDS);
    }
}
