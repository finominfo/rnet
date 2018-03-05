package hu.finominfo.rnet;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RunningChecker {

    private volatile static File f;
    private volatile static FileChannel channel;
    private volatile static FileLock lock;
    private static final Lock LOCK = new ReentrantLock();
    private volatile static Boolean RESULT = null;

    public static boolean check() {
        if (RESULT == null) {
            LOCK.lock();
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
                } else {
                    Runtime.getRuntime().addShutdownHook(new Thread(RunningChecker::unlockFile));
                    RESULT = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                RESULT = false;
            } finally {
                LOCK.unlock();
            }
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
