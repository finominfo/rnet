package hu.finominfo.rnet.common;

import hu.finominfo.rnet.audio.AudioPlayer;
import hu.finominfo.rnet.audio.AudioPlayerContinuous;
import hu.finominfo.rnet.communication.tcp.events.control.objects.PlayAudio;
import hu.finominfo.rnet.communication.tcp.events.control.objects.PlayVideo;
import hu.finominfo.rnet.communication.tcp.events.control.objects.ResetCounter;
import hu.finominfo.rnet.communication.tcp.events.control.objects.ShowPicture;
import hu.finominfo.rnet.communication.tcp.events.file.FileType;
import hu.finominfo.rnet.communication.tcp.events.message.MessageEvent;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import hu.finominfo.rnet.frontend.servant.common.MessageDisplay;
import hu.finominfo.rnet.frontend.servant.common.PictureDisplay;
import hu.finominfo.rnet.frontend.servant.common.VideoPlayer;
import hu.finominfo.rnet.frontend.servant.common.VideoPlayerContinuous;
import hu.finominfo.rnet.properties.Props;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
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
        final ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", "sudo java -Dpi4j.linking=dynamic -cp \"rnet.jar:lib/*\" hu.finominfo.rnet.Main");
        try {
            processBuilder.start();
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
                    Globals.get().videoPlayerContinuous = VideoPlayerContinuous.get();
                    Globals.get().videoPlayerContinuous.play(new PlayVideo(Globals.audioFolder, contMusicAtCounterStart, 100));
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
        VideoPlayerContinuous videoPlayerContinuous = Globals.get().videoPlayerContinuous;
        if (videoPlayerContinuous != null) {
            videoPlayerContinuous.stop();
            Globals.get().videoPlayerContinuous = null;
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
            BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                    cursorImg, new Point(0, 0), "blank cursor");
            frame1.getContentPane().setCursor(blankCursor);
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

    public static void playAudio(PlayAudio playAudio) {
        closeAudio();
        Globals.get().audioPlayer = new AudioPlayer(Globals.get().executor, playAudio.getPathAndName());
        Globals.get().audioPlayer.play(null);
        Globals.get().executor.schedule(() -> {
            Globals.get().audioPlayer.close();
            Globals.get().audioPlayer = null;
        }, (Globals.get().audioPlayer.getClip().getMicrosecondLength() / 1000) + 200, TimeUnit.MILLISECONDS);
    }

    public static void playAudioContinuous(PlayAudio playAudioContinuous) {
        closeAudio();
        Globals.get().videoPlayerContinuous = VideoPlayerContinuous.get();
        Globals.get().videoPlayerContinuous.play(new PlayVideo(Globals.audioFolder, playAudioContinuous.getShortName(), 100));
    }

    public static void showPicture(ShowPicture showPicture) {
        attention(() -> PictureDisplay.get().display(showPicture.getPathAndName(), showPicture.getSeconds()));
    }

    public static void showMessage(MessageEvent msg) {
        attention(() -> MessageDisplay.get().show(msg.getText(), msg.getSeconds()));
    }

    public static void playVideo(PlayVideo playVideo) {
        attention(() -> VideoPlayer.get().play(playVideo));
    }


    private static void attention(final Runnable runnable) {
        final AtomicBoolean wasRun = new AtomicBoolean(false);
        String attention = Props.get().getAttention();
        if (attention != null && !attention.isEmpty()) {
            String fileName = Globals.audioFolder + File.separator + attention;
            File f = new File(fileName);
            if (f.exists() && !f.isDirectory()) {
                try {
                    final AudioPlayer player = new AudioPlayer(Globals.get().executor, fileName);
                    player.play(null);
                    Globals.get().executor.schedule(() -> {
                        player.close();
                    }, (player.getClip().getMicrosecondLength() / 1000) + 200, TimeUnit.MILLISECONDS);
                    Globals.get().executor.schedule(() -> {
                        if (runnable != null && wasRun.compareAndSet(false, true)) {
                            Globals.get().executor.submit(runnable);
                        }
                    }, 3, TimeUnit.SECONDS);

                } catch (Exception e) {
                    logger.error(getStackTrace(e));
                    if (runnable != null && wasRun.compareAndSet(false, true)) {
                        Globals.get().executor.submit(runnable);
                    }
                }
            } else {
                if (runnable != null && wasRun.compareAndSet(false, true)) {
                    Globals.get().executor.submit(runnable);
                }
            }
        } else {
            if (runnable != null && wasRun.compareAndSet(false, true)) {
                Globals.get().executor.submit(runnable);
            }
        }
    }

    private volatile static int lastVolume = 100;
    private volatile static AtomicLong lastCheck = new AtomicLong(0);

    public static int getOmxVolume() {
        long now = System.currentTimeMillis();
        long last = lastCheck.get();
        if (now - last > 60_000L && lastCheck.compareAndSet(last, now)) { //Check only once per minute
            try {
                new ProcessBuilder("chown", "-R", "user:").start();
            } catch (IOException e) {
                logger.error(getStackTrace(e));
            }
        }
        return lastVolume;
    }

}
