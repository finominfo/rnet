package hu.finominfo.rnet.frontend.counteronly;

import hu.finominfo.rnet.audio.AudioPlayer;
import hu.finominfo.rnet.audio.AudioPlayerWrapper;
import hu.finominfo.rnet.frontend.servant.counter.Panel;

import java.awt.Color;
import java.awt.Font;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author kalman.kovacs@globessey.local
 */
public class GamePanel extends Panel {

    public void beepAndFlash() {
        beep.play(null);
        flash();
    }

    public GamePanel(AudioPlayerWrapper beep, Font customFont, Color backGroundColor, Color successBackGroundColor, Color failedBackGroundColor, double diff, long milliseconds, ScheduledExecutorService ses) {
        super(beep, customFont, backGroundColor, successBackGroundColor, failedBackGroundColor, diff, milliseconds, ses);
    }

    public GamePanel make() {
        return (GamePanel) super.make();
    }


    public String getTime(long millisecs) {
        int secs = (int) (millisecs / 1000);
        int remSecs = secs % 60;
        int remMins = secs / 60;
        return String.format("%02d:%02d", remMins, remSecs);
    }

    public void makeStart() {
        start = System.currentTimeMillis() - finished + start;
        finished = 0;
        stopStartButton.setText(stopStartTexts[0]);
        resetButton.setVisible(false);
        if (normalTextColor.compareAndSet(false, true)) {
            timer.setForeground(Color.YELLOW);
        }
        resetState = false;
    }

    public void makeStop() {
        beeping.set(false);
        finished = System.currentTimeMillis();
        stopStartButton.setText(stopStartTexts[1]);
        stopStartButton.setVisible(false);
        resetButton.setVisible(true);
        resetState = false;
    }

    public void resetButtonPressed() {
        setVisible();
        if (finished != 0) {
            resetState = true;
            finished = System.currentTimeMillis();
            start = System.currentTimeMillis();
            timer.setText(getTime(milliseconds));
            if (normalTextColor.compareAndSet(false, true)) {
                timer.setForeground(Color.YELLOW);
            }
            beeping.set(false);
            successPlayed.set(false);
            failedPlayed.set(false);
            resetButton.setVisible(false);
            stopStartButton.setVisible(true);
        }
    }
}
