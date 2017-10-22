package hu.finominfo.rnet.common;

import hu.finominfo.rnet.audio.AudioPlayer;
import hu.finominfo.rnet.audio.AudioPlayerContinuous;
import hu.finominfo.rnet.communication.tcp.events.control.objects.PlayVideo;
import hu.finominfo.rnet.communication.tcp.events.control.objects.ResetCounter;
import hu.finominfo.rnet.communication.tcp.events.file.FileType;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import hu.finominfo.rnet.frontend.servant.common.VideoPlayer;
import hu.finominfo.rnet.properties.Props;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
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
        if (name.trim().endsWith(")")) {
            name = name.substring(0, name.indexOf('(')).trim();
        }
        final String finalName = name;
        return Globals.get().serverClients.entrySet().stream().filter(entry -> entry.getValue().getName().equals(finalName)).findFirst().get();
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


    public static void restartApplication() {
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


    public static void startCounterVideo() {
        String videoPlayAtCounterStart = Props.get().getVideoPlayAtCounterStart();
        if (videoPlayAtCounterStart != null && !videoPlayAtCounterStart.isEmpty()) {
            PlayVideo playVideo = new PlayVideo(Globals.videoFolder, videoPlayAtCounterStart, 30);
            VideoPlayer.get().play(playVideo);
            Globals.get().executor.submit(new Runnable() {
                @Override
                public void run() {
                    if (VideoPlayer.get().isPlaying()) {
                        Globals.get().executor.schedule(this, 1, TimeUnit.SECONDS);
                    } else {
                        startCounterMusic();
                    }
                }
            });
        } else {
            startCounterMusic();
        }
    }

    private static void startCounterMusic() {
        String contMusicAtCounterStart = Props.get().getContMusicAtCounterStart();
        if (contMusicAtCounterStart != null && !contMusicAtCounterStart.isEmpty()) {
            String fileName = Globals.audioFolder + File.separator + contMusicAtCounterStart;
            File f = new File(fileName);
            if (f.exists() && !f.isDirectory()) {
                try {
                    closeAudio();
                    Globals.get().audioPlayerContinuous = new AudioPlayerContinuous(Globals.get().executor, fileName);
                    Globals.get().audioPlayerContinuous.play(null);
                } catch (Exception e) {
                    logger.error(getStackTrace(e));
                }
            }
        }
        Globals.get().counter.makeStart();
    }

    public static void closeAudio() {
        AudioPlayer audioPlayer = Globals.get().audioPlayer;
        if (audioPlayer != null) {
            audioPlayer.close();
            Globals.get().audioPlayer = null;
        }
        AudioPlayerContinuous audioPlayerContinuous = Globals.get().audioPlayerContinuous;
        if (audioPlayerContinuous != null) {
            audioPlayerContinuous.stop();
            audioPlayerContinuous.close();
            Globals.get().audioPlayerContinuous = null;
        }
    }


    public static void createAndShowGui(final JFrameHolder jFrameHolder, boolean undecorated, JPanel panel, Font customFont, String frameName, WindowAdapter windowAdapter, KeyListener keyListener) {
        SwingUtilities.invokeLater(() -> {
            final JFrame frame1 = jFrameHolder == null ? (new JFrame(frameName)) : jFrameHolder.getFrame();
            frame1.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame1.addWindowListener(windowAdapter);
            if (keyListener != null) {
                frame1.addKeyListener(keyListener);
            }
            frame1.getContentPane().add(panel);
            if (undecorated) {
                frame1.setUndecorated(undecorated);
                frame1.pack();
            } else {
                frame1.setOpacity(1.0f);
            }
            frame1.setVisible(true);
            frame1.setLocationRelativeTo(null);
            if (jFrameHolder != null) {
                jFrameHolder.setFrame(frame1);
            }
        });
    }

    public static Font getCustomFont() {
        Font customFont = null;
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("./Crysta.ttf"));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException(e);
        }
        return customFont;
    }


}
