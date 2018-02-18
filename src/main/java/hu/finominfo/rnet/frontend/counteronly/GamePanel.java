package hu.finominfo.rnet.frontend.counteronly;

import hu.finominfo.rnet.audio.AudioPlayer;
import hu.finominfo.rnet.audio.AudioPlayerWrapper;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author kalman.kovacs@globessey.local
 */
public class GamePanel extends JPanel {

    private static final AtomicBoolean beeping = new AtomicBoolean(false);
    private static final AtomicBoolean starting = new AtomicBoolean(false);
    private static final AtomicBoolean successPlayed = new AtomicBoolean(false);
    private static final AtomicBoolean failedPlayed = new AtomicBoolean(false);
    private static final AtomicBoolean normalTextColor = new AtomicBoolean(true);
    private static final AtomicLong lastColorChanged = new AtomicLong(0);
    public final double diff;
    private final Color backGroundColor;
    private final Color successBackGroundColor;
    private final Color failedBackGroundColor;
    private final ScheduledExecutorService ses;
    private volatile long milliseconds;
    private volatile long start;
    private volatile long finished;
    private volatile JLabel timer;
    private volatile JButton stopStartButton;
    private volatile JButton resetButton;
    private final String stopStartTexts[] = new String[]{"STOP", "START"};
    private final String resetButtonText = "RESET";
    private volatile boolean resetState = true;
    private volatile long lastMovement;
    private final AtomicBoolean buttonsAreVisible = new AtomicBoolean(true);
    private final Font customFont;
    private final AudioPlayerWrapper beep;
    private final AudioPlayer success;
    private final AudioPlayer failed;
    private static final long DELAY = 10_000;
    private static final long DELAY_VISIBLE = 5_000;

    public final Runnable refresh = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            if (now - lastMovement > DELAY_VISIBLE) {
                setInvisible();
            }
            long time = start - now + milliseconds;
            if (time > 0 && finished == 0) {
                timer.setText(getTime(time));
                GamePanel.this.setBackground(backGroundColor);
                int sec = (int) (time / 1000);
                if (sec % 60 == 0 && sec > 59) {
                    beepAndFlash();
                } else if (sec < 60 && sec > 15 && sec % 10 == 0) {
                    beepAndFlash();
                } else if (sec < 16 && sec > 10) {
                    beepAndFlash();
                } else if (sec < 11) {
                    moreBeepAndFlash();
                }
            } else if (time <= 0 && ((finished - start > milliseconds) || (finished == 0))) {
                changeColorIfPossible(failedBackGroundColor);
                if (failedPlayed.compareAndSet(false, true)) {
                    failed.play(null);
                }
            } else if (!resetState) {
                changeColorIfPossible(successBackGroundColor);
                if (successPlayed.compareAndSet(false, true)) {
                    success.play(null);
                }
            }
            now = System.currentTimeMillis();
            time = (start - now + milliseconds) % 1000;
            time = time < 600 ? 650 : time + 50;
            ses.schedule(this, time, TimeUnit.MILLISECONDS);
        }
    };

    private void beepAndFlash() {
        beep.play(null);
        flash();
    }

    private void flash() {
        if (normalTextColor.compareAndSet(true, false)) {
            timer.setForeground(Color.BLACK);
            ses.schedule(new Runnable() {
                @Override
                public void run() {
                    if (normalTextColor.compareAndSet(false, true)) {
                        timer.setForeground(Color.YELLOW);
                    }
                }
            }, 200, TimeUnit.MILLISECONDS);
        }
    }

    private void moreBeepAndFlash() {
        if (beeping.compareAndSet(false, true)) {
            ses.submit(new Runnable() {
                @Override
                public void run() {
                    long time = start - System.currentTimeMillis() + milliseconds;
                    if (time > 300 && beeping.get()) {
                        beepAndFlash();
                        int next = 500;
                        if (time < 5000) {
                            next = 320;
                        }
                        ses.schedule(this, next, TimeUnit.MILLISECONDS);
                    } else {
                        beeping.set(false);
                    }
                }
            });
        }
    }

    private void changeColorIfPossible(Color newColor) {
        long now = System.currentTimeMillis();
        long last = lastColorChanged.get();
        if (now - last > 700 && lastColorChanged.compareAndSet(last, now)) {
            if (normalTextColor.compareAndSet(true, false)) {
                timer.setForeground(newColor);
            } else if (normalTextColor.compareAndSet(false, true)) {
                timer.setForeground(Color.YELLOW);
            }
        }
    }

    public GamePanel(AudioPlayerWrapper beep, AudioPlayer success, AudioPlayer failed, Font customFont, Color backGroundColor, Color successBackGroundColor, Color failedBackGroundColor, double diff, long milliseconds, ScheduledExecutorService ses) {
        super(new FlowLayout());
        this.beep = beep;
        this.success = success;
        this.failed = failed;
        this.customFont = customFont;
        this.backGroundColor = backGroundColor;
        this.diff = diff;
        this.milliseconds = milliseconds;
        this.ses = ses;
        this.successBackGroundColor = successBackGroundColor;
        this.failedBackGroundColor = failedBackGroundColor;
    }

    public GamePanel make() {
        setBackground(backGroundColor);
        setBorder(BorderFactory.createEmptyBorder((int) (200d * diff), 0, 0, 0));
        //add(createLabel(title));
        add(createTimer());
        add(createStopStartButton());
        add(createResetButton());
        resetButton.setVisible(false);
        lastMovement = System.currentTimeMillis();
        return this;
    }

    private JLabel createTimer() {
        timer = new JLabel();
        timer.setBackground(Color.BLACK);
        timer.setForeground(Color.YELLOW);
//        timer.setFont(GameFrontEnd.customFont.deriveFont(Font.ITALIC + Font.BOLD, (float) (670d * diff)));
        timer.setFont(customFont.deriveFont(Font.ITALIC, (float) (850d * diff)));
        timer.setHorizontalAlignment(JLabel.LEFT);
        timer.setText(getTime(milliseconds));
        start();
        timer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!setVisible() && finished != 0 && resetState) {
                    try {
                        Object[] possibilities = {"00:10", "00:20", "00:30", "00:40", "00:50",
                            "01:00", "01:10", "01:20", "01:30", "01:40", "01:50",
                            "02:00", "02:10", "02:20", "02:30", "02:40", "02:50",
                            "03:00", "03:15", "03:30", "03:45", "04:00", "04:15", "04:30", "04:45",
                            "05:00", "05:15", "05:30", "05:45", "05:00", "05:30", "06:00", "06:30",
                            "07:00", "07:30", "08:00", "08:30", "09:00", "09:30",
                            "10:00", "11:00", "13:00", "14:00", "15:00", "16:00",
                            "17:00", "18:00", "19:00", "20:00", "21:00", "22:00",
                            "23:00", "24:00", "25:00", "26:00", "27:00", "28:00", "29:00",
                            "30:00", "35:00", "40:00", "45:00", "50:00", "55:00", "60:00",
                            "65:00", "70:00", "75:00", "80:00", "85:00", "90:00", "95:00", "99:59"
                        };
                        String newTime = (String) JOptionPane.showInputDialog(
                                GamePanel.this,
                                "Select time",
                                "Customized Dialog",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                possibilities,
                                "25:00");

//                        String newTime = JOptionPane.showInputDialog("New time (hh:mm): ");
                        String[] split = newTime.split(":");
                        long time = Long.valueOf(split[0]) * 60_000L + Long.valueOf(split[1]) * 1_000;
                        milliseconds = time;
                        timer.setText(getTime(milliseconds));
                    } catch (Throwable t) {
                        System.out.println(t.getMessage());
                    }
                }
            }
        });
        return timer;
    }

    private String getTime(long millisecs) {
        int secs = (int) (millisecs / 1000);
        int remSecs = secs % 60;
        int remMins = secs / 60;
        return String.format("%02d:%02d", remMins, remSecs);
    }

    private void start() {
        finished = System.currentTimeMillis();
        start = System.currentTimeMillis();
//        ses.submit(refresh);
    }

    private JButton createStopStartButton() {
        stopStartButton = new JButton();
        stopStartButton.setFont(stopStartButton.getFont().deriveFont(Font.BOLD, (float) (16d * diff)));
        stopStartButton.setHorizontalAlignment(JLabel.LEFT);
        stopStartButton.setText(stopStartTexts[1]);
        stopStartButton.addActionListener((ActionEvent e) -> {
            stopStartButtonPressed();
        });
        return stopStartButton;
    }

    private void stopStartButtonPressed() {
        setVisible();
        if (finished == 0) {
            makeStop();
        } else {
            if (starting.compareAndSet(false, true)) {
                setInvisible();
                ses.schedule(new Runnable() {
                    @Override
                    public void run() {
                        makeStart();
                        starting.set(false);
                    }
                }, resetState ? DELAY : 0, TimeUnit.MILLISECONDS);
            }
        }
    }

    private void makeStart() {
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

    private boolean setVisible() {
        if (!starting.get()) {
            lastMovement = System.currentTimeMillis();
            boolean becameVisible = buttonsAreVisible.compareAndSet(false, true);
            if (becameVisible) {
                if (finished == 0 || resetState) {
                    stopStartButton.setVisible(true);
                }
                if (!resetState) {
                    resetButton.setVisible(true);
                }
            }
            return becameVisible;
        } else {
            return true;
        }
    }

    private void setInvisible() {
        if (buttonsAreVisible.compareAndSet(true, false)) {
            stopStartButton.setVisible(false);
            resetButton.setVisible(false);
        }
    }

    private JButton createResetButton() {
        resetButton = new JButton();
        resetButton.setFont(resetButton.getFont().deriveFont(Font.BOLD, (float) (16d * diff)));
        resetButton.setHorizontalAlignment(JLabel.LEFT);
        resetButton.setText(resetButtonText);
        resetButton.addActionListener((ActionEvent e) -> {
            resetButtonPressed();
        });
        return resetButton;
    }

    private void resetButtonPressed() {
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