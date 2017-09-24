package hu.finominfo.rnet.common;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.24.
 */
public class Utils {

    public static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();
        return sStackTrace;
    }
}
