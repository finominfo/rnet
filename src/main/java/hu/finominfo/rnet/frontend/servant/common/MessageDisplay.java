package hu.finominfo.rnet.frontend.servant.common;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.07.
 */
public class MessageDisplay {

    private final static Logger logger = Logger.getLogger(MessageDisplay.class);

    private final String message;
    private final int seconds;

    public MessageDisplay(String message, int seconds) {
        this.message = message;
        this.seconds = seconds;
    }

    public void show() {
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
            Globals.get().executor.schedule(() -> {
                Globals.get().status.setMessage(null);
                dialog.dispose();
            }, seconds, TimeUnit.SECONDS);
            Globals.get().executor.schedule(() -> {
                String shortMessage = message.length() <= 10 ? message : (message.substring(0, 10) + "...");
                Globals.get().status.setMessage("Showing: " + shortMessage);
                dialog.setVisible(true);
            }, 10, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }

    }

    public static void main(String[] args) {
        new MessageDisplay("sdjfhsdjlv nlkirvh dgvh", 10).show();
        Globals.get().executor.shutdown();
    }
}
