package hu.finominfo.rnet.frontend.servant.common;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    ScheduledFuture<?> schedule = null;
    Queue<JDialog> dialogs = new ConcurrentLinkedQueue<JDialog>();

    public void display(final String pathAndName, final int seconds) {
        closeAllDialogs();
        try {
            ImageIcon icon = new ImageIcon(pathAndName);
            int iconHeight = icon.getIconHeight();
            int iconWidth = icon.getIconWidth();
            double widthScale = ((double) Globals.get().width) / (double) iconWidth;
            double heightScale = ((double) Globals.get().height) / (double) iconHeight;
            double resize = widthScale < heightScale ? widthScale : heightScale;
//            logger.info("widthScale: " + widthScale);
//            logger.info("heightScale: " + heightScale);
//            logger.info("resize: " + resize);
            Image scaledInstance = icon.getImage().getScaledInstance((int) (iconWidth * resize), (int) (iconHeight * resize), Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledInstance);  // transform it back

            //pane.getRootPane().setBorder( BorderFactory.createLineBorder(Color.RED) );
            JDialog dialog = new JDialog();
            dialog.setUndecorated(true);
            JLabel label = new JLabel(icon);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            dialog.add( label );
            dialog.pack();
            final JDialog dlg = dialog;
            dlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            dlg.setAlwaysOnTop(true);
            closeAllDialogs();
            dialogs.add(dlg);
            //while (!dialogs.isEmpty()) {
            //    Globals.get().executor.schedule(() -> show(dlg, pathAndName), 100, TimeUnit.MILLISECONDS);
            //    Globals.get().executor.schedule(() -> close(dlg), seconds+remaining, TimeUnit.SECONDS);
            //}else{
            Globals.get().executor.schedule(() -> show(dlg, pathAndName), 100, TimeUnit.MILLISECONDS);
            schedule = Globals.get().executor.schedule(() -> close(dlg), seconds, TimeUnit.SECONDS);
            //}
        } catch (Exception e) {
            logger.error(e);
        }
    }


    private void closeAllDialogs() {
        if (schedule != null)
            schedule.cancel(false);
        while (!dialogs.isEmpty()) {
            close(dialogs.poll());
        }
    }

    private void show(JDialog dialog, String pathAndName) {
        Globals.get().status.setPicture("Showing: " + pathAndName);
        Globals.get().counter.timer.setVisible(false);
        dialog.setVisible(true);
    }

    private void close(JDialog dialog) {
        try {
            Globals.get().status.setPicture(null);
            Globals.get().counter.timer.setVisible(true);
            dialog.dispose();
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }

//    public static void main(String[] args) {
//        new PictureDisplay("C:\\3.jpg", 5).display();
//        Globals.get().executor.shutdown();
//    }
}
