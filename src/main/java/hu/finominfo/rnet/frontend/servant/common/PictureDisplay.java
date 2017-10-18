package hu.finominfo.rnet.frontend.servant.common;

import hu.finominfo.rnet.common.Globals;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
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
            ImageIcon icon = new ImageIcon(pathAndName);
            int iconHeight = icon.getIconHeight();
            int iconWidth = icon.getIconWidth();
            double widthScale = ((double)Globals.get().width) / (double)iconWidth;
            double heightScale = ((double)Globals.get().height) / (double)iconHeight;
            double resize = widthScale < heightScale ? widthScale : heightScale;
//            logger.info("widthScale: " + widthScale);
//            logger.info("heightScale: " + heightScale);
//            logger.info("resize: " + resize);
            Image scaledInstance = icon.getImage().getScaledInstance((int) (iconWidth * resize), (int) (iconHeight * resize), Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledInstance);  // transform it back

            //pane.getRootPane().setBorder( BorderFactory.createLineBorder(Color.RED) );
            JOptionPane pane = new JOptionPane(icon);
            final JDialog dlg = pane.createDialog(null);
            dlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            dlg.setAlwaysOnTop(true);
            Globals.get().executor.schedule(() -> {
                Globals.get().status.setPicture(null);
                Globals.get().counter.timer.setVisible(true);
                dlg.dispose();
            }, seconds, TimeUnit.SECONDS);
            Globals.get().executor.schedule(() -> {
                Globals.get().status.setPicture("Showing: " + pathAndName);
                Globals.get().counter.timer.setVisible(false);
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
