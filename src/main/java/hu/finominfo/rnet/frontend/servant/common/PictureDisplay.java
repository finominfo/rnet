package hu.finominfo.rnet.frontend.servant.common;

import hu.finominfo.rnet.common.Globals;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.07.
 */
public class PictureDisplay {

    private final static Logger logger = Logger.getLogger(PictureDisplay.class);

    private final String pathAndName;
    private final int seconds;

    public PictureDisplay(String pathAndName, int seconds) {
        this.pathAndName = pathAndName;
        this.seconds = seconds;
    }

    public void display() {
        try {
            final ImageIcon icon = new ImageIcon(pathAndName);
            JOptionPane pane = new JOptionPane(icon);
            final JDialog dlg = pane.createDialog(null);
            //pane.getRootPane().setBorder( BorderFactory.createLineBorder(Color.RED) );
            dlg.setAlwaysOnTop(true);
            Globals.get().executor.schedule(() -> {
                Globals.get().status.setPicture(null);
                dlg.dispose();
            }, seconds, TimeUnit.SECONDS);
            Globals.get().executor.schedule(() -> {
                Globals.get().status.setPicture("Showing: " + pathAndName);
                dlg.setVisible(true);
            }, 10, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error(e);
        }
    }


    public static void main(String[] args) {
        new PictureDisplay("C:\\3.jpg", 5).display();
        Globals.get().executor.shutdown();
    }
}
