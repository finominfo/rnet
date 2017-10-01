package hu.finominfo.rnet.common;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
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

    public static boolean isAddressEquals(List<Long> addr1, List<Long> addr2) {
        return addr1.stream().anyMatch(aLong -> addr2.contains(aLong));
    }

    public static <T, U> List<U> convertList(List<T> from, Function<T, U> func) {
        return from.stream().map(func).collect(Collectors.toList());
    }

    public static <T, U> U[] convertArray(T[] from, Function<T, U> func, IntFunction<U[]> generator) {
        return Arrays.stream(from).map(func).toArray(generator);
    }

}
