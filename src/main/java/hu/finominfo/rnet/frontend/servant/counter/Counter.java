package hu.finominfo.rnet.frontend.servant.counter;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.properties.Props;
import hu.finominfo.rnet.frontend.servant.counter.io.HandlingIO;
import hu.finominfo.rnet.audio.AudioPlayer;
import hu.finominfo.rnet.audio.AudioPlayerContinuous;
import hu.finominfo.rnet.audio.AudioPlayerWrapper;
import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class Counter extends JPanel {

    private final static Logger logger = Logger.getLogger(Counter.class);

    private static final int ROW = 1;
    private static final int COLUMN = 1;
    private static final Color BG = Color.BLACK;

    private final JPanel mainPanel;
    public final double diff;
    public final ScheduledThreadPoolExecutor executor;
    private final Props props = Props.get();
    private final Font customFont;
    private final Set<AudioPlayerContinuous> continuousPlayers = new HashSet<>();
    private final List<String> animalVoices = new ArrayList<>();
    private final Random random = new Random(0xdf435fa2187L);

    public Counter(Font customFont) {
        this.customFont = customFont;
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        diff = ((double) screenSize.width) / 1920;
        System.out.println("diff: " + diff);
        mainPanel = new JPanel(new GridLayout(ROW, COLUMN));
        executor = new ScheduledThreadPoolExecutor(4);
    }

    public void start() {
        mainPanel.setBackground(BG);
        mainPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        AudioPlayerWrapper beep = null;
        AudioPlayer success = null;
        AudioPlayer failed = null;
        try {
            beep = new AudioPlayerWrapper(executor, props.getBeep());
            success = new AudioPlayer(executor, props.getSuccess());
            failed = new AudioPlayer(executor, props.getFailed());
            for (String name : props.getBaseAudio()) {
                AudioPlayerContinuous ap = new AudioPlayerContinuous(executor, name);
                ap.play(null);
                continuousPlayers.add(ap);
            }
            for (String name : props.getAnimalVoices()) {
                try {
                    animalVoices.add(name);
                    System.out.println(name);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        executor.submit(new Runnable() {
            private volatile AudioPlayer player = null;

            @Override
            public void run() {
                try {
                    if (player != null) {
                        player.close();
                    }
                    if (!animalVoices.isEmpty()) {
                        player = new AudioPlayer(executor, animalVoices.get(random.nextInt(animalVoices.size())));
                    }
                    player.play(null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                executor.schedule(this, random.nextInt(60), TimeUnit.SECONDS);
            }
        });
        Panel[][] panels = new Panel[ROW][COLUMN];
        int i = 0;
        for (Panel[] panel : panels) {
            for (int j = 0; j < panel.length; j++) {
                panel[j] = new Panel(
                        beep,
                        success,
                        failed,
                        customFont,
                        Color.BLACK,
                        Color.GREEN,
                        Color.RED,
                        diff,
                        props.getTimes().get(i),
                        executor).make();
                Globals.get().counter = panel[j];
                i++;
                mainPanel.add(panel[j]);
            }
        }
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                for (Panel[] panelRow : panels) {
                    for (Panel panel : panelRow) {
                        panel.refresh.run();
                    }
                }
                try {
                    new HandlingIO() {
                        @Override
                        public void stopButtonPressed() {
                            for (Panel[] panelRow : panels) {
                                for (Panel panel : panelRow) {
                                    panel.makeStop();
                                }
                            }
                        }
                    };
                } catch (Exception e) {
                    logger.error(e);
                }
//                if (running) {
//                    executor.schedule(this, 1, TimeUnit.SECONDS);
//                }
            }
        }, 1, TimeUnit.SECONDS);
    }

    public static void createAndShowGui() {
        Font customFont = null;
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("./Crysta.ttf"));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException(e);
        }
        Counter gameFrontEnd = new Counter(customFont);
        gameFrontEnd.start();
        JFrame frame = new JFrame("Game Panel");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(gameFrontEnd);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGui();
        });
    }
}
