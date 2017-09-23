package hu.finominfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.21..
 */
public class PropReader {

    private final Properties prop = new Properties();
    private static final Lock lock = new ReentrantLock();

    private static PropReader ourInstance = new PropReader();

    public static PropReader get() {
        return ourInstance;
    }


    private PropReader() {
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Properties getProperties() {
        return prop;
    }
}
