package hu.finominfo.rnet.frontend.counteronly;

import hu.finominfo.rnet.audio.AudioPlayer;
import hu.finominfo.rnet.audio.AudioPlayerContinuous;
import hu.finominfo.rnet.audio.AudioPlayerWrapper;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.frontend.servant.counter.io.HandlingIO;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class GameFrontEnd extends JPanel {

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
    private final boolean monkeysEnabled;

    public GameFrontEnd(Font customFont) {
        this.customFont = customFont;
        propertiesReader = new PropertiesReader();
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        diff = ((double) screenSize.width) / 1920;
        System.out.println("diff: " + diff);
        mainPanel = new JPanel(new GridLayout(ROW, COLUMN));
        executor = new ScheduledThreadPoolExecutor(4);
        this.monkeysEnabled = propertiesReader.isMonkeysEnabled();
    }

    public void start() {
        mainPanel.setBackground(BG);
        mainPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        AudioPlayerWrapper beep = new AudioPlayerWrapper(executor, propertiesReader.getBeep());
        if (monkeysEnabled) {
            monkeysEnabled();
        }
        GamePanel[][] panels = new GamePanel[ROW][COLUMN];
        int i = 0;
        for (GamePanel[] panel : panels) {
            for (int j = 0; j < panel.length; j++) {
                panel[j] = new GamePanel(
                        beep,
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
                for (GamePanel[] panelRow : panels) {
                    for (GamePanel panel : panelRow) {
                        panel.refresh.run();
                    }
                }
                new HandlingIO() {
                    @Override
                    public void stopButtonPressed() {
                        for (GamePanel[] panelRow : panels) {
                            for (GamePanel panel : panelRow) {
                                panel.makeStop();
                            }
                        }
                    }
                };
            }
        }, 1, TimeUnit.SECONDS);
    }

    private void monkeysEnabled() {
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
    }

    public static void createAndShowGui() {
        Font customFont = Utils.getCustomFont();
        GameFrontEnd gameFrontEnd = new GameFrontEnd(customFont);
        Utils.createAndShowGui(null, true, gameFrontEnd, customFont, "Counter", new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
                System.exit(0);
            }
        }, null);
        gameFrontEnd.start();
    }

    public static void main(String[] args) {
        createAndShowGui();
    }
}
