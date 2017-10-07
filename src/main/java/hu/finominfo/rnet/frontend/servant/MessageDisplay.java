package hu.finominfo.rnet.frontend.servant;

import hu.finominfo.rnet.common.Globals;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.07.
 */
public class MessageDisplay {

    private final static Logger logger = Logger.getLogger(MessageDisplay.class);

    private final String message;

    public MessageDisplay(final String message) {
        this.message = message;
    }

    public void show() {
        try {
            final JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
            final JDialog dialog = new JDialog();
            dialog.setTitle("Message from operator");
            dialog.setModal(true);
            dialog.setContentPane(optionPane);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dialog.pack();
            Globals.get().executor.schedule(() -> dialog.setVisible(true), 10, TimeUnit.MILLISECONDS);
            Globals.get().executor.schedule(() -> dialog.dispose(), 2 + (message.length() >> 3), TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public static void main(String[] args) {
        new MessageDisplay("sdjfhsdjlv nlkirvh dgvh").show();
        Globals.get().executor.shutdown();
    }
}
