package hu.finominfo.rnet.frontend.servant.counter;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.properties.Props;
import hu.finominfo.rnet.frontend.servant.counter.io.HandlingIO;
import hu.finominfo.rnet.audio.AudioPlayer;
import hu.finominfo.rnet.audio.AudioPlayerWrapper;
import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class Counter extends JPanel {

    private final static Logger logger = Logger.getLogger(Counter.class);

    private static final int ROW = 1;
    private static final int COLUMN = 1;
    private static final Color BG = Color.BLACK;

    private final JPanel mainPanel;
    public final double diff;
    public final ScheduledExecutorService executor;
    private final Props props = Props.get();
    private final Font customFont;

    public Counter(Font customFont) {
        this.customFont = customFont;
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        diff = ((double) screenSize.width) / 1920;
        logger.info("diff: " + diff);
        mainPanel = new JPanel(new GridLayout(ROW, COLUMN));
        executor = Globals.get().executor;
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
        } catch (Exception e) {
            logger.error(e);
        }
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
            }
        }, 1, TimeUnit.SECONDS);
    }

    public static void createAndShowGui() {
        Font customFont = Utils.getCustomFont();
        Counter counter = new Counter(customFont);
        Utils.createAndShowGui(null, true, counter, customFont, "Counter", new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
                System.exit(0);
            }
        });
        counter.start();
    }

    public static void main(String[] args) {
        createAndShowGui();
    }
}
