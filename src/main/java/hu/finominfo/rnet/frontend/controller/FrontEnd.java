package hu.finominfo.rnet.frontend.controller;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static javax.swing.ScrollPaneConstants.*;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.25.
 */
public class FrontEnd extends JFrame implements Runnable {

    private final static Logger logger = Logger.getLogger(FrontEnd.class);

    @Override
    public void run() {
        try {
            refreshDirs();
        } catch (Exception e) {
            logger.error(e);
        }

        Globals.get().executor.schedule(this, 3, TimeUnit.SECONDS);

    }

    private void refreshDirs() {
        String selectedValue = servantsList.getSelectedValue();
        if (selectedValue != null) {
            Map<String, List<String>> dirs = Globals.get().serverClients.values().stream().filter(param -> param.getName().equals(selectedValue)).findFirst().get().getDirs();
            if (videoListModel.getSize() != dirs.get(Globals.videoFolder).size() ||
                    !dirs.get(Globals.videoFolder).stream().allMatch(str -> videoListModel.contains(str))) {

                videoListModel.clear();
                dirs.get(Globals.videoFolder).stream().forEach(str -> videoListModel.addElement(str));
            }
            if (audioListModel.getSize() != dirs.get(Globals.audioFolder).size() ||
                    !dirs.get(Globals.audioFolder).stream().allMatch(str -> audioListModel.contains(str))) {
                audioListModel.clear();
                dirs.get(Globals.audioFolder).stream().forEach(str -> audioListModel.addElement(str));
            }
            if (pictureListModel.getSize() != dirs.get(Globals.pictureFolder).size() ||
                    !dirs.get(Globals.pictureFolder).stream().allMatch(str -> pictureListModel.contains(str))) {
                pictureListModel.clear();
                dirs.get(Globals.pictureFolder).stream().forEach(str -> pictureListModel.addElement(str));
            }
        } else {
            videoListModel.clear();
            audioListModel.clear();
            pictureListModel.clear();
        }
    }


    private final JLabel servantsLabel = new JLabel("SERVANTS");
    public final DefaultListModel<String> servantsListModel = new DefaultListModel();
    private final JList<String> servantsList = new JList<>(servantsListModel);
    private final JScrollPane servantsPane = new JScrollPane(servantsList, VERTICAL_SCROLLBAR_NEVER, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    private final JButton renameBtn = new JButton("RENAME");
    private final JButton sendTextBtn = new JButton("SEND TEXT");

    private final JLabel counterLabel = new JLabel("COUNTER");
    private final JButton startBtn = new JButton("START");
    private final JButton stopBtn = new JButton("STOP");
    private final JButton resetBtn = new JButton("RESET");


    private final JLabel audioLabel = new JLabel("AUDIO");
    public final DefaultListModel<String> audioListModel = new DefaultListModel();
    private final JList<String> audioList = new JList<>(audioListModel);
    private final JScrollPane audioPane = new JScrollPane(audioList, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    private final JButton audioPlay = new JButton("PLAY");
    private final JButton audioAdd = new JButton("ADD");
    private final JButton audioContinuousPlay = new JButton("CONTINUOUS PLAY");

    private final JLabel videoLabel = new JLabel("VIDEO");
    public final DefaultListModel<String> videoListModel = new DefaultListModel();
    private final JList<String> videoList = new JList<>(videoListModel);
    private final JScrollPane videoPane = new JScrollPane(videoList, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    private final JButton videoPlay = new JButton("PLAY");
    private final JButton videoAdd = new JButton("ADD");


    private final JLabel pictureLabel = new JLabel("PICTURE");
    public final DefaultListModel<String> pictureListModel = new DefaultListModel();
    private final JList<String> pictureList = new JList<>(pictureListModel);
    private final JScrollPane picturePane = new JScrollPane(pictureList, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    private final JButton pictureShow = new JButton("SHOW");
    private final JButton pictureAdd = new JButton("ADD");

    private final ListSelectionListener dirsSelectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            refreshDirs();
        }
    };


    public FrontEnd() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("RNET Controller - version " + Globals.getVersion());

        Globals.get().executor.schedule(this, 3, TimeUnit.SECONDS);

        servantsList.addListSelectionListener(dirsSelectionListener);

        servantsLabel.setFont(new Font(servantsLabel.getFont().getName(), Font.BOLD, 25));
        add(servantsLabel);
        servantsList.setSelectionMode(SINGLE_SELECTION);
        servantsLabel.setBounds(30, 5, 170, 30);
        servantsList.setBounds(30, 40, 170, 365);
        servantsPane.setBounds(30, 40, 170, 365);
        getContentPane().add(servantsPane);
        renameBtn.setBounds(30, 420, 120, 30);
        add(renameBtn);
        sendTextBtn.setBounds(30, 460, 120, 30);
        add(sendTextBtn);

        counterLabel.setFont(new Font(counterLabel.getFont().getName(), Font.BOLD, 25));
        counterLabel.setBounds(30, 530, 180, 30);
        add(counterLabel);
        startBtn.setBounds(30, 570, 80, 30);
        add(startBtn);
        stopBtn.setBounds(30, 610, 80, 30);
        add(stopBtn);
        resetBtn.setBounds(30, 650, 80, 30);
        add(resetBtn);


        audioLabel.setFont(new Font(audioLabel.getFont().getName(), Font.BOLD, 25));
        add(audioLabel);
        audioList.setSelectionMode(SINGLE_SELECTION);
        audioLabel.setBounds(280, 5, 155, 30);
        audioPane.setBounds(280, 40, 200, 365);
        getContentPane().add(audioPane);
        audioPlay.setBounds(280, 420, 80, 30);
        add(audioPlay);
        audioAdd.setBounds(380, 420, 80, 30);
        add(audioAdd);
        audioContinuousPlay.setBounds(280, 460, 180, 30);
        add(audioContinuousPlay);


        videoLabel.setFont(new Font(videoLabel.getFont().getName(), Font.BOLD, 25));
        add(videoLabel);
        videoList.setSelectionMode(SINGLE_SELECTION);
        videoLabel.setBounds(530, 5, 155, 30);
        videoPane.setBounds(530, 40, 200, 365);
        getContentPane().add(videoPane);
        videoPlay.setBounds(530, 420, 80, 30);
        add(videoPlay);
        videoAdd.setBounds(630, 420, 80, 30);
        add(videoAdd);


        pictureLabel.setFont(new Font(pictureLabel.getFont().getName(), Font.BOLD, 25));
        add(pictureLabel);
        pictureList.setSelectionMode(SINGLE_SELECTION);
        pictureLabel.setBounds(785, 5, 155, 30);
        picturePane.setBounds(785, 40, 200, 365);
        getContentPane().add(picturePane);
        pictureShow.setBounds(785, 420, 80, 30);
        add(pictureShow);
        pictureAdd.setBounds(885, 420, 80, 30);
        add(pictureAdd);


        setSize(1024, 768);
        setLayout(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new FrontEnd();
    }

}
