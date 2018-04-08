package hu.finominfo.rnet.frontend.servant.common;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
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

    public void display(final String pathAndName, final String name, final int seconds) {
        try {
            ImageIcon icon = new ImageIcon(PictureResize.get().getResizedImage(pathAndName, name));
            JDialog dialog = new JDialog();
            dialog.setUndecorated(true);
            int locationX = (Globals.get().width - icon.getIconWidth()) / 2;
            int locationY = (Globals.get().height - icon.getIconHeight()) / 2;
            dialog.setLocation(locationX, locationY);
            JLabel label = new JLabel(icon);
            BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                    cursorImg, new Point(0, 0), "blank cursor");
            dialog.setCursor(blankCursor);
            dialog.add(label);
            dialog.pack();
            dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            dialog.setAlwaysOnTop(true);
            close();
            lastDialog = dialog;
            Globals.get().executor.schedule(() -> show(pathAndName), 10, TimeUnit.MILLISECONDS);
            lastSchedule = Globals.get().executor.schedule(() -> close(), seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
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
        SwingUtilities.invokeLater(() -> PictureDisplay.get().display("C:\\images.jpg", "images.jpg",  3));
    }
}
