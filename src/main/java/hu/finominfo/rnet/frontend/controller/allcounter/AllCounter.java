package hu.finominfo.rnet.frontend.controller.allcounter;

import hu.finominfo.rnet.common.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

/**
 * Created by kalman.kovacs@globessey.local on 2017.10.19.
 */
public class AllCounter extends JPanel {

    private static final int ROW = 3;
    private static final int COLUMN = 3;
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
            counterPanel.setTitle("" + (i + 1));
            counterPanel.setCounter("" + (i + 1));
        }
    }

    public CounterPanel[] getPanels() {
        return panels;
    }


    public static void main(String[] args) {
        Font customFont = Utils.getCustomFont();
        Utils.createAndShowGui(null, false, new AllCounter(customFont), customFont, "All counters", new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
            }
        }, null);
    }
}
