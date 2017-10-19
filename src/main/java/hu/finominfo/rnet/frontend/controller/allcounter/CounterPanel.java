package hu.finominfo.rnet.frontend.controller.allcounter;

import hu.finominfo.rnet.common.Globals;

import javax.swing.*;
import java.awt.*;

/**
 * Created by kalman.kovacs@globessey.local on 2017.10.19.
 */
public class CounterPanel extends JPanel {
    private final JLabel title;
    private final JLabel counter;

    public CounterPanel(Font font) {
        setLayout(new GridLayout(2, 1));
        title = new JLabel();
        title.setFont(font.deriveFont(Font.BOLD, (float) (85d * Globals.get().diff)));
        counter = new JLabel();
        counter.setFont(font.deriveFont(Font.BOLD, (float) (185d * Globals.get().diff)));
        add(title);
        add(counter);

    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setCounter(String counter) {
        this.counter.setText(counter);
    }

}
