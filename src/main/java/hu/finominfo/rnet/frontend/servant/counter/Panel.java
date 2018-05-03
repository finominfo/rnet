package hu.finominfo.rnet.frontend.servant.counter;

import hu.finominfo.rnet.audio.AudioPlayer;
import hu.finominfo.rnet.audio.AudioPlayerWrapper;
import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.events.control.objects.PlayVideo;
import hu.finominfo.rnet.communication.tcp.events.control.objects.ShowPicture;
import hu.finominfo.rnet.communication.tcp.events.dir.media.TimeOrder;
import hu.finominfo.rnet.database.H2KeyValue;
import hu.finominfo.rnet.frontend.servant.common.PictureDisplay;
import hu.finominfo.rnet.frontend.servant.common.VideoPlayer;
import hu.finominfo.rnet.properties.Props;
import hu.finominfo.rnet.statistics.Clicker;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
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
 * @author kalman.kovacs@globessey.local
 */
public class Panel extends JPanel {

    public static final AtomicBoolean beeping = new AtomicBoolean(false);
    public static final AtomicBoolean starting = new AtomicBoolean(false);
    public static final AtomicBoolean successPlayed = new AtomicBoolean(false);
    public static final AtomicBoolean failedPlayed = new AtomicBoolean(false);
    public static final AtomicBoolean normalTextColor = new AtomicBoolean(true);
    public static final AtomicLong lastColorChanged = new AtomicLong(0);
    public final double diff;
    public final Color backGroundColor;
    public final Color successBackGroundColor;
    public final Color failedBackGroundColor;
    public final ScheduledExecutorService ses;
    public volatile long milliseconds;
    public volatile long start;
    public volatile long finished;
    public volatile JLabel timer;
    public volatile JLabel resultText;
    public volatile JButton stopStartButton;
    public volatile JButton resetButton;
    public final String stopStartTexts[] = new String[]{"STOP", "START"};
    public final String resetButtonText = "RESET";
    public volatile boolean resetState = true;
    public volatile long lastMovement;
    public final AtomicBoolean buttonsAreVisible = new AtomicBoolean(true);
    public final Font customFont;
    public final AudioPlayerWrapper beep;
    public static volatile AudioPlayer success;
    public static volatile AudioPlayer failed;
    public static final long DELAY = 10_000;
    public static final long DELAY_VISIBLE = 5_000;
    public final int invisibleAfter = Props.get().getInvisible() * 60_000;
    public volatile long lastMilliseconds = System.currentTimeMillis();

    private final static Logger logger = Logger.getLogger(Panel.class);

    public final Runnable refresh = new Runnable() {
        @Override
        public void run() {

            try {
                String succ = Globals.get().types.getAudioTypes().get(TimeOrder.SUCCESS);
                String fail = Globals.get().types.getAudioTypes().get(TimeOrder.FAILED);
                if (success != null) {
                    if (!success.getFile().getName().endsWith(succ)) {
                        success = new AudioPlayer(Globals.get().executor, Globals.audioFolder + File.separator + succ);
                    }
                } else if (succ != null && !succ.isEmpty()) {
                    success = new AudioPlayer(Globals.get().executor, Globals.audioFolder + File.separator + succ);
                }
                if (failed != null) {
                    if (!failed.getFile().getName().endsWith(fail)) {
                        failed = new AudioPlayer(Globals.get().executor, Globals.audioFolder + File.separator + fail);
                    }
                } else if (fail != null && !fail.isEmpty()) {
                    failed = new AudioPlayer(Globals.get().executor, Globals.audioFolder + File.separator + fail);
                }
                long now = System.currentTimeMillis();
                if (now - lastMovement > DELAY_VISIBLE) {
                    setInvisible();
                }
                long time = start - now + milliseconds;
                if (lastMilliseconds + invisibleAfter < now) {
                    timer.setVisible(false);
                    Globals.get().status.setCounter("invisible");
                }
                if (time > 0 && finished == 0) {
                    String timeString = getTime(time);
                    Globals.get().status.setCounter(timeString);
                    lastMilliseconds = now;
                    timer.setText(timeString);
                    Panel.this.setBackground(backGroundColor);
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
                    try {
                        if (failedPlayed.compareAndSet(false, true)) {
                            timer.setFont(customFont.deriveFont(Font.ITALIC, (float) (650d * diff)));
                            resultText.setForeground(Color.RED);
                            resultText.setText("GAME OVER");
                            if (failed != null) {
                                Globals.get().executor.submit(() -> failed.play(null));
                            }
                            Utils.closeAudio();
//                            int seconds = showPic(Globals.get().types.getPictureTypes().get(TimeOrder.FAILED), 30);
                            Globals.get().executor.schedule(() ->
                                            playMediaAtFinish(Globals.get().types.getVideoTypes().get(TimeOrder.FAILED)),
                                    0, TimeUnit.SECONDS);
                        }
                    } catch (Exception ex) {
                        logger.error(Utils.getStackTrace(ex));
                        VideoPlayer.get().play(new PlayVideo(".", "failed_counter.wav", 10));
                    }
                } else if (!resetState) {
                    changeColorIfPossible(successBackGroundColor);
                    try {
                        if (successPlayed.compareAndSet(false, true)) {
                            timer.setFont(customFont.deriveFont(Font.ITALIC, (float) (650d * diff)));
                            resultText.setForeground(Color.GREEN);
                            resultText.setText("CONGRATULATIONS");
                            if (success != null) {
                                Globals.get().executor.submit(() -> success.play(null));
                            }
                            Utils.closeAudio();
//                            int seconds = showPic(Globals.get().types.getPictureTypes().get(TimeOrder.SUCCESS), 30);

                            Globals.get().executor.schedule(() ->
                                            playMediaAtFinish(Globals.get().types.getVideoTypes().get(TimeOrder.SUCCESS)),
                                    0, TimeUnit.SECONDS);
                        }
                    } catch (Exception ex) {
                        logger.error(Utils.getStackTrace(ex));
                        VideoPlayer.get().play(new PlayVideo(".", "success.wav", 10));
                    }
                }

            } catch (Exception e) {
                logger.info(Utils.getStackTrace(e));
            }
            long now = System.currentTimeMillis();
            long time = (start - now + milliseconds) % 1000;
            time = time < 600 ? 650 : time + 50;
            ses.schedule(this, time, TimeUnit.MILLISECONDS);
        }
    };

    public static int showPic(String picName, int seconds) {
        if (picName != null && !picName.isEmpty()) {
            logger.info("Show picture ");
            ShowPicture showPicture = new ShowPicture(Globals.pictureFolder, picName, seconds);
            PictureDisplay.get().display(showPicture.getPathAndName(), showPicture.getShortName(), showPicture.getSeconds());
            return seconds;
        }
        return 0;
    }

    public static void playMediaAtFinish(String videoPlayAtFinish) {
        if (videoPlayAtFinish != null && !videoPlayAtFinish.isEmpty()) {
            PlayVideo play = new PlayVideo(Globals.videoFolder, videoPlayAtFinish, 30);
            VideoPlayer.get().play(play);
        }
    }

    public void beepAndFlash() {
        //beep.play(null);
        flash();
    }

    public void addMinutesIfPossible(int minutes) {
        long now = System.currentTimeMillis();
        long addMillis = minutes * 60_000L;
        if (now < addMillis + start) {
            start = now;
        } else {
            start += addMillis;
        }
    }

    public void flash() {
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

    public void moreBeepAndFlash() {
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

    public void changeColorIfPossible(Color newColor) {
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

    public Panel(AudioPlayerWrapper beep, Font customFont, Color backGroundColor, Color successBackGroundColor, Color failedBackGroundColor, double diff, long milliseconds, ScheduledExecutorService ses) {
        super(new FlowLayout());
        this.beep = beep;
        this.customFont = customFont;
        this.backGroundColor = backGroundColor;
        this.diff = diff;
        this.milliseconds = milliseconds;
        this.ses = ses;
        this.successBackGroundColor = successBackGroundColor;
        this.failedBackGroundColor = failedBackGroundColor;
    }

    public Panel make() {
        setBackground(backGroundColor);
        setBorder(BorderFactory.createEmptyBorder((int) (200d * diff), 0, 0, 0));
//        add(createLabel(title));
        add(createText());
        add(createTimer());
        add(createStopStartButton());
        add(createResetButton());
        resetButton.setVisible(false);
        lastMovement = System.currentTimeMillis();
        return this;
    }

    public JLabel createText() {
        resultText = new JLabel();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        resultText.setFont(new Font("Arial", Font.PLAIN, (int) (125d * diff)));
        resultText.setBackground(Color.BLACK);
        //resultText.setFont(customFont.deriveFont(Font.ITALIC, (float) (85d * diff)));
        resultText.setHorizontalAlignment(JLabel.CENTER);
        resultText.setForeground(Color.WHITE);
        return resultText;
    }

    public JLabel createTimer() {
        timer = new JLabel();
        timer.setBackground(Color.BLACK);
        timer.setForeground(Color.YELLOW);
//        timer.setFont(Counter.customFont.deriveFont(Font.ITALIC + Font.BOLD, (float) (670d * diff)));
        timer.setFont(customFont.deriveFont(Font.ITALIC, (float) (850d * diff)));
        timer.setHorizontalAlignment(JLabel.LEFT);
        timer.setText(getTime(milliseconds));
        Globals.get().status.setCounter(getTime(milliseconds));
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
                                Panel.this,
                                "Select time",
                                "Customized Dialog",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                possibilities,
                                "25:00");

//                        String newTime = JOptionPane.showInputDialog("New time (hh:mm): ");
                        String[] split = newTime.split(":");
                        milliseconds = Long.valueOf(split[0]) * 60_000L + Long.valueOf(split[1]) * 1_000;
                        timer.setText(getTime(milliseconds));
                        Globals.get().status.setCounter(getTime(milliseconds));
                    } catch (Throwable t) {
                        System.out.println(t.getMessage());
                    }
                }
            }
        });
        return timer;
    }

    public String getTime(long millisecs) {
        int secs = (int) (millisecs / 1000);
        int remSecs = secs % 60;
        int remMins = secs / 60;
        H2KeyValue.set(H2KeyValue.COUNTER_CURRENT_STATE, String.valueOf(remMins));
        return String.format("%02d:%02d", remMins, remSecs);
    }

    public void start() {
        finished = System.currentTimeMillis();
        start = System.currentTimeMillis();
    }

    public JButton createStopStartButton() {
        stopStartButton = new JButton();
        stopStartButton.setFont(stopStartButton.getFont().deriveFont(Font.BOLD, (float) (16d * diff)));
        stopStartButton.setHorizontalAlignment(JLabel.LEFT);
        stopStartButton.setText(stopStartTexts[1]);
        stopStartButton.addActionListener((ActionEvent e) -> {
            stopStartButtonPressed();
        });
        return stopStartButton;
    }

    public void stopStartButtonPressed() {
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

    public void makeStart() {
        if (finished != 0) {
            logger.info("makeStart()");
            //resultText.setText("");
            Globals.get().executor.submit(() -> Clicker.click());
            timer.setVisible(true);
            long now = System.currentTimeMillis();
            lastMilliseconds = now;
            timer.setVisible(true);
            start = now - finished + start;
            finished = 0;
            stopStartButton.setText(stopStartTexts[0]);
            resetButton.setVisible(false);
            if (normalTextColor.compareAndSet(false, true)) {
                timer.setForeground(Color.YELLOW);
            }
            resetState = false;
        }
    }

    public void makeStop() {
        logger.info("makeStop()");
        long now = System.currentTimeMillis();
        lastMilliseconds = now;
        timer.setVisible(true);
        beeping.set(false);
        finished = now;
        stopStartButton.setText(stopStartTexts[1]);
        stopStartButton.setVisible(false);
        resetButton.setVisible(false);
        resetState = false;
    }

    public boolean setVisible() {
        if (!starting.get()) {
            lastMovement = System.currentTimeMillis();
            boolean becameVisible = buttonsAreVisible.compareAndSet(false, true);
            if (becameVisible) {
                if (finished == 0 || resetState) {
                    stopStartButton.setVisible(true);
                }
                if (!resetState) {
                    resetButton.setVisible(false);
                }
            }
            return becameVisible;
        } else {
            return true;
        }
    }

    public void setInvisible() {
        if (buttonsAreVisible.compareAndSet(true, false)) {
            stopStartButton.setVisible(false);
            resetButton.setVisible(false);
        }
    }

    public JButton createResetButton() {
        resetButton = new JButton();
        resetButton.setFont(resetButton.getFont().deriveFont(Font.BOLD, (float) (16d * diff)));
        resetButton.setHorizontalAlignment(JLabel.LEFT);
        resetButton.setText(resetButtonText);
        resetButton.addActionListener((ActionEvent e) -> {
            resetButtonPressed();
        });
        return resetButton;
    }

    public void resetButtonPressed() {
        resultText.setText("");
        timer.setFont(customFont.deriveFont(Font.ITALIC, (float) (850d * diff)));
        long now = System.currentTimeMillis();
        lastMilliseconds = now;
        timer.setVisible(true);
        setVisible();
        if (finished != 0) {
            resetState = true;
            finished = System.currentTimeMillis();
            start = System.currentTimeMillis();
            timer.setText(getTime(milliseconds));
            Globals.get().status.setCounter(getTime(milliseconds));
            if (normalTextColor.compareAndSet(false, true)) {
                timer.setForeground(Color.YELLOW);
            }
            beeping.set(false);
            resetButton.setVisible(false);
            stopStartButton.setVisible(true);
        }
        successPlayed.set(false);
        failedPlayed.set(false);
    }
}
