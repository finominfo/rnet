package hu.finominfo.rnet.frontend.servant.common;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.JFrameHolder;
import hu.finominfo.rnet.common.Utils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.07.
 */
public class MessageDisplay extends JPanel{

    private final static Logger logger = Logger.getLogger(MessageDisplay.class);

    public static MessageDisplay get() {
        return ourInstance;
    }
    private static MessageDisplay ourInstance = new MessageDisplay();
    private Font customFont = UIManager.getDefaults().getFont("TabbedPane.font");
    private volatile JFrameHolder previousFrame = new JFrameHolder();
    private final JTextArea jTextArea = new JTextArea ();

    private static final Color BG = Color.BLACK;
    private static final Color FG = Color.GREEN;

    public MessageDisplay() {
        setBackground(BG);
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        setLayout(new GridLayout(1, 1));
        add(jTextArea);
    }

    public void show(final String message, final int seconds) {
        if (previousFrame != null) {
            try {
                Globals.get().status.setMessage(null);
                if (previousFrame != null && previousFrame.getFrame() != null) {
                    previousFrame.getFrame().dispose();
                }
            } catch (Exception e) {
                logger.error(Utils.getStackTrace(e));
            }
        }
        final JFrameHolder currentFrame = new JFrameHolder();
        currentFrame.setFrame(new JFrame());
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImg, new Point(0, 0), "blank cursor");
        previousFrame = currentFrame;
        jTextArea.setCursor(blankCursor);
        jTextArea.setLineWrap(true);
        jTextArea.setWrapStyleWord(true);
        jTextArea.setBackground(BG);
        jTextArea.setForeground(FG);
        jTextArea.setFont(customFont);
        jTextArea.setFont(new Font(jTextArea.getFont().getName(), Font.BOLD, 80));
        jTextArea.setText(message);
        jTextArea.setEditable(false);
        String shortMessage = message.length() <= 10 ? message : (message.substring(0, 10) + "...");
        Globals.get().status.setMessage("Showing: " + shortMessage);
        Utils.createAndShowGui(currentFrame, true, this, customFont, "Display message", new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Globals.get().status.setMessage(null);
                e.getWindow().dispose();
            }
        }, null);
        Globals.get().executor.schedule(() -> {
            try {
                Globals.get().status.setMessage(null);
                currentFrame.getFrame().dispose();
            } catch (Exception e) {
                logger.error(Utils.getStackTrace(e));
            }
        }, seconds, TimeUnit.SECONDS);
    }
}
