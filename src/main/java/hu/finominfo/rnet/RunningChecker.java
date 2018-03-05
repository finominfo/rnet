package hu.finominfo.rnet;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class RunningChecker {

    private volatile static File f;
    private volatile static FileChannel channel;
    private volatile static FileLock lock;

    public static boolean check() {
        try {
            f = new File("running.lock");
            if (f.exists()) {
                f.delete();
            }
            channel = new RandomAccessFile(f, "rw").getChannel();
            lock = channel.tryLock();
            if (lock == null) {
                channel.close();
                return false;
            }
            Runtime.getRuntime().addShutdownHook(new Thread(RunningChecker::unlockFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
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
