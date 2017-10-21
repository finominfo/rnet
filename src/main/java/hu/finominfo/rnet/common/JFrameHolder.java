package hu.finominfo.rnet.common;

import javax.swing.*;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.21.
 */
public class JFrameHolder {
    private volatile JFrame frame = null;

    public JFrame getFrame() {
        return frame;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }
}
