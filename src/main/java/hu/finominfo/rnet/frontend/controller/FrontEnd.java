package hu.finominfo.rnet.frontend.controller;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.communication.tcp.events.control.ControlType;
import hu.finominfo.rnet.communication.tcp.events.file.FileType;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.25.
 */
public class FrontEnd extends FrontEndUtils {


    public FrontEnd() {
        super();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("RNET Controller - version " + Globals.getVersion());

        Globals.get().executor.schedule(this, 2, TimeUnit.SECONDS);

        servantsList.addListSelectionListener(e -> refreshDirs());

        renameBtn.addActionListener(e -> rename());

        sendTextBtn.addActionListener(e -> sendText());


        servantsLabel.setFont(new Font(servantsLabel.getFont().getName(), Font.BOLD, 25));
        add(servantsLabel);
        servantsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        servantsLabel.setBounds(30, 5, 170, 30);
        servantsList.setBounds(30, 40, 190, 365);
        servantsPane.setBounds(30, 40, 190, 365);
        getContentPane().add(servantsPane);
        renameBtn.setBounds(30, 420, 85, 30);
        add(renameBtn);
        sendTextBtn.setBounds(120, 420, 100, 30);
        add(sendTextBtn);

        showPeriod.setFont(new Font(showPeriod.getFont().getName(), Font.BOLD, 14));
        showPeriod.setBounds(30, 460, 150, 30);
        add(showPeriod);
        showSeconds.setFont(new Font(showPeriod.getFont().getName(), Font.BOLD, 20));
        showSeconds.setBounds(180, 460, 40, 30);
        showSeconds.setHorizontalAlignment(JTextField.RIGHT);
        showSeconds.setText("30");
        add(showSeconds);

        counterLabel.setFont(new Font(counterLabel.getFont().getName(), Font.BOLD, 25));
        counterLabel.setBounds(30, 530, 180, 30);
        add(counterLabel);
        startBtn.setBounds(30, 570, 90, 30);
        add(startBtn);
        stopBtn.setBounds(130, 570, 90, 30);
        add(stopBtn);
        resetBtn.setBounds(30, 620, 90, 30);
        add(resetBtn);
        resetBtn.addActionListener(e -> sendResetCounter());
        startBtn.addActionListener(e -> sendOnlyControl(ControlType.START_COUNTER));
        stopBtn.addActionListener(e -> sendOnlyControl(ControlType.STOP_COUNTER));
        resetLabel.setFont(new Font(resetLabel.getFont().getName(), Font.BOLD, 20));
        resetLabel.setBounds(185, 620, 250, 30);
        add(resetLabel);
        resetMins.setFont(new Font(resetMins.getFont().getName(), Font.BOLD, 20));
        resetMins.setBounds(140, 620, 40, 30);
        resetMins.setHorizontalAlignment(JTextField.RIGHT);
        resetMins.setText("25");
        add(resetMins);

        statusLabel.setFont(new Font(counterLabel.getFont().getName(), Font.BOLD, 25));
        statusLabel.setBounds(280, 530, 180, 30);
        add(statusLabel);
        status.setBounds(280, 570, 450, 120);
        add(status);

        audioLabel.setFont(new Font(audioLabel.getFont().getName(), Font.BOLD, 25));
        add(audioLabel);
        audioList.setSelectionMode(SINGLE_SELECTION);
        audioLabel.setBounds(280, 5, 155, 30);
        audioPane.setBounds(280, 40, 200, 365);
        getContentPane().add(audioPane);
        audioPlay.setBounds(280, 420, 65, 30);
        add(audioPlay);
        audioAdd.setBounds(350, 420, 60, 30);
        add(audioAdd);
        audioDel.setBounds(420, 420, 60, 30);
        add(audioDel);
        audioContinuousPlay.setBounds(280, 460, 110, 30);
        audioStop.setBounds(410, 460, 70, 30);
        add(audioContinuousPlay);
        add(audioStop);
        audioPlay.addActionListener(e -> playAudio(ControlType.PLAY_AUDIO));
        audioContinuousPlay.addActionListener(e -> playAudio(ControlType.PLAY_AUDIO_CONTINUOUS));
        audioStop.addActionListener(e -> sendOnlyControl(ControlType.STOP_AUDIO));
        audioAdd.addActionListener(e -> sendFile(Globals.audioFolder, FileType.AUDIO));
        audioDel.addActionListener(e -> deleteFile(Globals.audioFolder, FileType.AUDIO, audioList));


        videoLabel.setFont(new Font(videoLabel.getFont().getName(), Font.BOLD, 25));
        add(videoLabel);
        videoList.setSelectionMode(SINGLE_SELECTION);
        videoLabel.setBounds(530, 5, 155, 30);
        videoPane.setBounds(530, 40, 200, 365);
        getContentPane().add(videoPane);
        videoPlay.setBounds(530, 420, 65, 30);
        add(videoPlay);
        videoAdd.setBounds(600, 420, 60, 30);
        add(videoAdd);
        videoDel.setBounds(670, 420, 60, 30);
        add(videoDel);
        videoContinuousPlay.setBounds(530, 460, 110, 30);
        videoStop.setBounds(660, 460, 70, 30);
        add(videoContinuousPlay);
        add(videoStop);
        videoAdd.addActionListener(e -> sendFile(Globals.videoFolder, FileType.VIDEO));
        videoDel.addActionListener(e -> deleteFile(Globals.videoFolder, FileType.VIDEO, videoList));
        videoPlay.addActionListener(e -> playVideo(ControlType.PLAY_VIDEO));
        videoContinuousPlay.addActionListener(e -> playVideo(ControlType.PLAY_VIDEO_CONTINUOUS));
        videoStop.addActionListener(e -> sendOnlyControl(ControlType.STOP_VIDEO));


        pictureLabel.setFont(new Font(pictureLabel.getFont().getName(), Font.BOLD, 25));
        add(pictureLabel);
        pictureList.setSelectionMode(SINGLE_SELECTION);
        pictureLabel.setBounds(785, 5, 155, 30);
        picturePane.setBounds(785, 40, 200, 365);
        getContentPane().add(picturePane);
        pictureShow.setBounds(785, 420, 74, 30);
        add(pictureShow);
        pictureAdd.setBounds(864, 420, 58, 30);
        add(pictureAdd);
        pictureDel.setBounds(927, 420, 58, 30);
        add(pictureDel);
        pictureAdd.addActionListener(e -> sendFile(Globals.pictureFolder, FileType.PICTURE));
        pictureDel.addActionListener(e -> deleteFile(Globals.pictureFolder, FileType.PICTURE, pictureList));
        pictureShow.addActionListener(e -> showPicture());


        add(taskTextLabel);
        add(taskNumber);
        taskTextLabel.setBounds(30, 700, 100, 30);
        taskNumber.setBounds(140, 700, 50, 30);

        setSize(1024, 768);
        setLayout(null);
        setVisible(true);
    }


    public static void main(String[] args) {
        new FrontEnd();
    }

}
