package hu.finominfo.rnet.frontend.controller;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.communication.tcp.events.control.ControlType;
import hu.finominfo.rnet.communication.tcp.events.file.FileType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
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

        //PreviewImage.setVisible(false);
        //add(PreviewImage);


        servantsLabel.setFont(new Font(servantsLabel.getFont().getName(), Font.BOLD, 25));
        add(servantsLabel);
        servantsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        servantsLabel.setBounds(98, 5, 48, 48);
        servantsList.setBounds(30, 55, 190, 365);
        servantsList.setFont(new Font(servantsList.getFont().getName(), Font.BOLD, 18));
        servantsPane.setBounds(30, 55, 190, 365);
        getContentPane().add(servantsPane);
        renameBtn.setBounds(30, 420, 85, 48);
        add(renameBtn);
        sendTextBtn.setBounds(120, 420, 100, 48);
        add(sendTextBtn);

        showPeriod.setFont(new Font(showPeriod.getFont().getName(), Font.BOLD, 14));
        showPeriod.setBounds(800, 680, 150, 30);
        showPeriod.setHorizontalAlignment(JTextField.RIGHT);
        add(showPeriod);
        showSeconds.setFont(new Font(showPeriod.getFont().getName(), Font.BOLD, 20));
        showSeconds.setBounds(950, 680, 40, 30);
        showSeconds.setHorizontalAlignment(JTextField.CENTER);
        showSeconds.setText("30");
        add(showSeconds);

        counterLabel.setFont(new Font(counterLabel.getFont().getName(), Font.BOLD, 25));
        counterLabel.setBounds(30, 500, 180, 30);
        add(counterLabel);
        startBtn.setBounds(30, 550, 64, 64);
        startBtn.setOpaque(false);
        startBtn.setContentAreaFilled(false);
        startBtn.setBorder(null);
        add(startBtn);
        stopBtn.setBounds(130, 550, 64, 64);
        stopBtn.setOpaque(false);
        stopBtn.setContentAreaFilled(false);
        stopBtn.setBorder(null);
        add(stopBtn);
        resetBtn.setBounds(30, 620, 64, 64);
        resetBtn.setOpaque(false);
        resetBtn.setContentAreaFilled(false);
        resetBtn.setBorder(null);
        add(resetBtn);
        resetBtn.addActionListener(e -> sendResetCounter());
        startBtn.addActionListener(e -> sendOnlyControl(ControlType.START_COUNTER));
//        startBtn.addActionListener(e -> playStartVideo(ControlType.PLAY_VIDEO));
        stopBtn.addActionListener(e -> sendOnlyControl(ControlType.STOP_COUNTER));
        resetLabel.setFont(new Font(resetLabel.getFont().getName(), Font.BOLD, 30));
        resetLabel.addActionListener(e -> showTimerGui());
        resetLabel.setBounds(155, 490, 48, 48);
        resetLabel.setOpaque(false);
        resetLabel.setContentAreaFilled(false);
        resetLabel.setBorder(null);
        add(resetLabel);
        resetMins.setFont(new Font(resetMins.getFont().getName(), Font.BOLD, 54));
        resetMins.setBounds(130, 620, 64, 64);
        resetMins.setHorizontalAlignment(JTextField.CENTER);
        resetMins.setText("25");
        resetMins.setBorder(null);
        add(resetMins);

        statusLabel.setFont(new Font(counterLabel.getFont().getName(), Font.BOLD, 25));
        statusLabel.setBounds(280, 530, 180, 30);
        add(statusLabel);
        status.setFont(new Font(counterLabel.getFont().getName(), Font.BOLD, 25));
        status.setBounds(280, 570, 450, 120);
        add(status);

        audioLabel.setFont(new Font(audioLabel.getFont().getName(), Font.BOLD, 25));
        add(audioLabel);
        audioList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof String && ((String) value).equals(defAudio)) {
                    setBackground(Color.YELLOW);
                    setForeground(Color.RED);
                    if (isSelected) {
                        setBackground(getBackground().darker());
                    }
                }
                return c;
            }
        });
        audioList.setSelectionMode(SINGLE_SELECTION);
        audioList.setFont(new Font(audioList.getFont().getName(), Font.BOLD, 18));
        audioLabel.setBounds(348, 5, 48, 48);
        audioPane.setBounds(280, 55, 200, 365);
        getContentPane().add(audioPane);
        audioPlay.setBounds(280, 420, 50, 48);
        add(audioPlay);
        audioStop.setBounds(330, 420, 50, 48);
        add(audioAdd);
        audioDel.setBounds(430, 420, 50, 48);
        add(audioDel);
        audioContinuousPlay.setBounds(280, 470, 0, 0);
        audioAdd.setBounds(380, 420, 50, 48);
        add(audioContinuousPlay);

        add(audioStop);
        audioPlay.addActionListener(e -> playAudio(ControlType.PLAY_AUDIO_CONTINUOUS));
        audioContinuousPlay.addActionListener(e -> playAudio(ControlType.PLAY_AUDIO_CONTINUOUS));
        audioStop.addActionListener(e -> sendControlWithName(ControlType.STOP_AUDIO, audioList.getSelectedValue()));
        audioAdd.addActionListener(e -> sendFile(Globals.audioFolder, FileType.AUDIO));
        audioDel.addActionListener(e -> deleteFile(Globals.audioFolder, FileType.AUDIO, audioList));


        videoLabel.setFont(new Font(videoLabel.getFont().getName(), Font.BOLD, 25));
        add(videoLabel);
        videoList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof String && ((String) value).equals(defVideo)) {
                    setBackground(Color.YELLOW);
                    setForeground(Color.RED);
                    if (isSelected) {
                        setBackground(getBackground().darker());
                    }
                }
                return c;
            }
        });
        videoList.setSelectionMode(SINGLE_SELECTION);
        videoList.setFont(new Font(videoList.getFont().getName(), Font.BOLD, 18));
        videoLabel.setBounds(598, 5, 48, 48);
        videoPane.setBounds(530, 55, 200, 365);
        getContentPane().add(videoPane);
        videoPlay.setBounds(530, 420, 60, 48);
        add(videoPlay);
        videoAdd.setBounds(600, 420, 60, 48);
        add(videoAdd);
        videoDel.setBounds(670, 420, 60, 48);
        add(videoDel);
        videoContinuousPlay.setBounds(530, 470, 0, 0);
        videoStop.setBounds(530, 470, 0, 0);
        add(videoContinuousPlay);
        add(videoStop);
        videoAdd.addActionListener(e -> sendFile(Globals.videoFolder, FileType.VIDEO));
        videoDel.addActionListener(e -> deleteFile(Globals.videoFolder, FileType.VIDEO, videoList));
        videoPlay.addActionListener(e -> playVideo(ControlType.PLAY_VIDEO));
        videoContinuousPlay.addActionListener(e -> playVideo(ControlType.PLAY_VIDEO_CONTINUOUS));
        videoStop.addActionListener(e -> sendControlWithName(ControlType.STOP_VIDEO, videoList.getSelectedValue()));



        pictureLabel.setFont(new Font(pictureLabel.getFont().getName(), Font.BOLD, 25));
        add(pictureLabel);
        pictureList.setSelectionMode(SINGLE_SELECTION);
        pictureList.setFont(new Font(pictureList.getFont().getName(), Font.BOLD, 18));
        pictureLabel.setBounds(853, 5, 48, 48);
        picturePane.setBounds(785, 55, 200, 365);
        getContentPane().add(picturePane);
        pictureShow.setBounds(785, 420, 60, 48);
        add(pictureShow);
        pictureAdd.setBounds(855, 420, 60, 48);
        add(pictureAdd);
        pictureDel.setBounds(925, 420, 60, 48);
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
