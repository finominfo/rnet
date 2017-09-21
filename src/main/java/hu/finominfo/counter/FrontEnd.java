package hu.finominfo.counter;

import hu.finominfo.common.PropertiesReader;
import hu.finominfo.rpi.io.HandlingIO;
import hu.finominfo.audio.AudioPlayer;
import hu.finominfo.audio.AudioPlayerContinuous;
import hu.finominfo.audio.AudioPlayerWrapper;
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

public class FrontEnd extends JPanel {

    private static final int ROW = 1;
    private static final int COLUMN = 1;
    private static final Color BG = Color.BLACK;

    private final JPanel mainPanel;
    public final double diff;
    public final ScheduledThreadPoolExecutor executor;
    private final PropertiesReader propertiesReader;
    private final Font customFont;
    private final Set<AudioPlayerContinuous> continuousPlayers = new HashSet<>();
    private final List<String> animalVoices = new ArrayList<>();
    private final Random random = new Random(0xdf435fa2187L);

    public FrontEnd(Font customFont) {
        this.customFont = customFont;
        propertiesReader = new PropertiesReader();
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        diff = ((double) screenSize.width) / 1920;
        System.out.println("diff: " + diff);
        mainPanel = new JPanel(new GridLayout(ROW, COLUMN));
        executor = new ScheduledThreadPoolExecutor(4);
    }

    public void start() {
        mainPanel.setBackground(BG);
        mainPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        AudioPlayerWrapper beep = new AudioPlayerWrapper(executor, propertiesReader.getBeep());
        AudioPlayer success = new AudioPlayer(executor, propertiesReader.getSuccess());
        AudioPlayer failed = new AudioPlayer(executor, propertiesReader.getFailed());
        for (String name : propertiesReader.getBaseAudio()) {
            AudioPlayerContinuous ap = new AudioPlayerContinuous(executor, name);
            ap.play(null);
            continuousPlayers.add(ap);
        }
        for (String name : propertiesReader.getAnimalVoices()) {
            try {
                animalVoices.add(name);
                System.out.println(name);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        executor.submit(new Runnable() {
            private volatile AudioPlayer player = null;

            @Override
            public void run() {
                try {
                    if (player != null) {
                        player.close();
                    }
                    player = new AudioPlayer(executor, animalVoices.get(random.nextInt(animalVoices.size())));
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
                        propertiesReader.getTimes().get(i),
                        executor).make();
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
//                if (running) {
//                    executor.schedule(this, 1, TimeUnit.SECONDS);
//                }
            }
        }, 1, TimeUnit.SECONDS);
    }

    private static void createAndShowGui() {
        Font customFont = null;
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("./Crysta.ttf"));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException(e);
        }
        FrontEnd gameFrontEnd = new FrontEnd(customFont);
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
