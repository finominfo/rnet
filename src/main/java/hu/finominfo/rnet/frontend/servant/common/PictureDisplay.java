package hu.finominfo.rnet.frontend.servant.common;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.07.
 */
public class PictureDisplay {

    private final static Logger logger = Logger.getLogger(PictureDisplay.class);

    public static PictureDisplay get() {
        return ourInstance;
    }

    private static PictureDisplay ourInstance = new PictureDisplay();
    ScheduledFuture<?> lastSchedule = null;
    JDialog lastDialog = null;

    public void display(final String pathAndName, final int seconds) {
        try {
            ImageIcon icon = new ImageIcon(pathAndName);
            int iconHeight = icon.getIconHeight();
            int iconWidth = icon.getIconWidth();
            double widthScale = ((double) Globals.get().width) / (double) iconWidth;
            double heightScale = ((double) Globals.get().height) / (double) iconHeight;
            double resize = widthScale < heightScale ? widthScale : heightScale;
            int x = (int) (iconWidth * resize);
            int y = (int) (iconHeight * resize);
            int locationX = (Globals.get().width - x) / 2;
            int locationY = (Globals.get().height - y) / 2;
            Image scaledInstance = icon.getImage().getScaledInstance(x, y, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledInstance);  // transform it back
            JDialog dialog = new JDialog();
            dialog.setUndecorated(true);
            dialog.setLocation(locationX, locationY);
            JLabel label = new JLabel(icon);
            dialog.add(label);
            dialog.pack();
            dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            dialog.setAlwaysOnTop(true);
            close();
            lastDialog = dialog;
            Globals.get().executor.schedule(() -> show(pathAndName), 10, TimeUnit.MILLISECONDS);
            lastSchedule = Globals.get().executor.schedule(() -> close(), seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void show(String pathAndName) {
        try {
            if (lastDialog != null) {
                Globals.get().status.setPicture("Showing: " + pathAndName);
                lastDialog.setVisible(true);
                Globals.get().counter.timer.setVisible(false);
            }
        } catch (Exception ex) {
            logger.error(Utils.getStackTrace(ex));
        }
    }

    private void close() {
        try {
            if (lastSchedule != null) {
                lastSchedule.cancel(false);
                lastSchedule = null;
            }
            if (lastDialog != null) {
                Globals.get().status.setPicture(null);
                lastDialog.dispose();
                lastDialog = null;
                Globals.get().counter.timer.setVisible(true);
            }
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> PictureDisplay.get().display("C:\\images.jpg", 3));
    }
}
