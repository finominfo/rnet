package hu.finominfo.rnet.common;

import hu.finominfo.rnet.communication.tcp.events.file.FileType;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.24.
 */
public class Utils {

    private final static Logger logger = Logger.getLogger(Utils.class);

    public static String getIp(String name) {
        return getIpClientParam(name).getKey();
    }

    public static ClientParam getClientParam(String name) {
        return getIpClientParam(name).getValue();
    }

    public static Map.Entry<String, ClientParam> getIpClientParam(String name) {
        return Globals.get().serverClients.entrySet().stream().filter(entry -> entry.getValue().getName().equals(name)).findFirst().get();
    }

    public static String getFileType(FileType fileType) {
        String path = null;
        switch (fileType) {
            case MAIN:
                path = ".";
                break;
            case VIDEO:
                path = Globals.videoFolder;
                break;
            case AUDIO:
                path = Globals.audioFolder;
                break;
            case PICTURE:
                path = Globals.pictureFolder;
                break;
        }
        return path;
    }

    public static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static List<String> getFilesFromFolder(String folder) {
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


    public static void restartApplication()
    //TODO: Fejleszteni!!!
    {
//        if (Globals.get().server != null) {
//            Globals.get().server.stop();
//        }
//        if (Globals.get().client != null) {
//            Globals.get().client.stop();
//        }
//        if (Globals.get().broadcaster != null) {
//            Globals.get().broadcaster.stop();
//        }
//        if (Globals.get().monitor != null) {
//            Globals.get().monitor.stop();
//        }
        Globals.get().executor.shutdown();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            logger.error(e);
        }
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final File currentJar = new File(Globals.JAR_NAME);

        final ArrayList<String> command = new ArrayList<String>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());

        final ProcessBuilder builder = new ProcessBuilder(command);
        try {
            builder.start();
        } catch (IOException e) {
            logger.error(e);
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        System.out.println(isAddressEquals(Arrays.asList(1L, 7L, 8L), Arrays.asList(7L, 4L, 1L)));
    }

}
