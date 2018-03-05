package hu.finominfo.rnet;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class RunningChecker {

    private volatile static File f;
    private volatile static FileChannel channel;
    private volatile static FileLock lock;
    private final static AtomicBoolean FIRST_CHECK = new AtomicBoolean(false);
    private volatile static Boolean RESULT = null;

    public static boolean check() {
        while (RESULT == null) {
            if (FIRST_CHECK.compareAndSet(false, true)) {
                try {
                    f = new File("running.lock");
                    if (f.exists()) {
                        f.delete();
                    }
                    channel = new RandomAccessFile(f, "rw").getChannel();
                    lock = channel.tryLock();
                    if (lock == null) {
                        channel.close();
                        RESULT = false;
                    }
                    Runtime.getRuntime().addShutdownHook(new Thread(RunningChecker::unlockFile));
                } catch (Exception e) {
                    e.printStackTrace();
                    RESULT = false;
                }
            }
            RESULT = true;
        }
        return RESULT;
    }

    public static void unlockFile() {
        try {
            if (lock != null)
                lock.release();
            channel.close();
            f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
