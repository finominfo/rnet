package hu.finominfo.rnet.frontend.controller.allcounter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by kalman.kovacs@globessey.local on 2017.10.19.
 */
public class AllCounter extends JPanel {

    private static final int ROW = 5;
    private static final int COLUMN = 2;
    private static final Color BG = Color.BLACK;
    private final CounterPanel[] panels = new CounterPanel[ROW * COLUMN];

    public AllCounter(Font font) {
        setBackground(BG);
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        setLayout(new GridLayout(ROW, COLUMN));
        for (int i = 0; i < ROW * COLUMN; i++) {
            CounterPanel counterPanel = new CounterPanel(font);
            add(counterPanel);
            panels[i] = counterPanel;
            counterPanel.setTitle("" + i);
            counterPanel.setCounter("" + i);
        }
    }

    public CounterPanel[] getPanels() {
        return panels;
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
        AllCounter allCounter = new AllCounter(customFont);
        JFrame frame = new JFrame("All Counters");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(allCounter);
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
