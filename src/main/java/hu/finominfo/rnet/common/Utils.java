package hu.finominfo.rnet.common;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.24.
 */
public class Utils {

    private final static Logger logger = Logger.getLogger(Utils.class);


    public static final String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();
        return sStackTrace;
    }

    public static final List<String> getFilesFromFolder(String folder) {
        List<String> files = new ArrayList<>();
        try {
            Files.newDirectoryStream(Paths.get(folder), path -> path.toFile().isFile())
                    .forEach(path -> files.add(path.toFile().getName()));
        } catch (Exception e) {
            logger.error(e);
        }
        return files;
    }

    public static void main(String[] args) {
        getFilesFromFolder(".idea").stream().forEach(System.out::println);
    }

}
