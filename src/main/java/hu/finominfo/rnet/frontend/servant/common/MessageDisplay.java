package hu.finominfo.rnet.frontend.servant.common;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.07.
 */
public class MessageDisplay {

    private final static Logger logger = Logger.getLogger(MessageDisplay.class);

    public static MessageDisplay get() {
        return ourInstance;
    }
    private static MessageDisplay ourInstance = new MessageDisplay();
    Queue<JDialog> dialogs = new ConcurrentLinkedQueue<JDialog>();



    public void show(final String message, final int seconds) {
        closeAllDialogs();
        try {
            final JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
            final JDialog dialog = new JDialog();
            Map<TextAttribute, Object> attributes = new HashMap<>();
            //dialog.setFont(Font.createFont(Font.TRUETYPE_FONT, new File("./Crysta.ttf")).deriveFont(Font.ITALIC, (float) (35)));
            //dialog.setFont(new Font("Serif", Font.BOLD, 30));
            dialog.setTitle("Message from operator");
            dialog.setModal(true);
            dialog.setUndecorated(true);
            dialog.setContentPane(optionPane);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dialog.setAlwaysOnTop(true);
            dialog.pack();
            closeAllDialogs();
            dialogs.add(dialog);
            Globals.get().executor.schedule(() -> show(dialog, message), 100, TimeUnit.MILLISECONDS);
            Globals.get().executor.schedule(() -> close(dialog), seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }

    }

    private void closeAllDialogs() {
        while (!dialogs.isEmpty()) {
            close(dialogs.poll());
        }
    }

    private void show(JDialog dialog, String message) {
        String shortMessage = message.length() <= 10 ? message : (message.substring(0, 10) + "...");
        Globals.get().status.setMessage("Showing: " + shortMessage);
        dialog.setVisible(true);
    }

    private void close(JDialog dialog) {
        try {
            Globals.get().status.setMessage(null);
            dialog.dispose();
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }

//    public static void main(String[] args) {
//        new MessageDisplay("sdjfhsdjlv nlkirvh dgvh", 10).show();
//        Globals.get().executor.shutdown();
//    }
}
