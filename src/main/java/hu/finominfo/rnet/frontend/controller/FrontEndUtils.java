package hu.finominfo.rnet.frontend.controller;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.events.control.ControlEvent;
import hu.finominfo.rnet.communication.tcp.events.control.ControlType;
import hu.finominfo.rnet.communication.tcp.events.control.objects.ShowPicture;
import hu.finominfo.rnet.communication.tcp.events.file.FileType;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import hu.finominfo.rnet.taskqueue.FrontEndTaskToDo;
import hu.finominfo.rnet.taskqueue.TaskToDo;
import org.apache.log4j.Logger;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.08.
 */
public class FrontEndUtils extends JFrame implements Runnable {

    protected final JLabel servantsLabel = new JLabel("SERVANTS");
    public final DefaultListModel<String> servantsListModel = new DefaultListModel();
    protected final JList<String> servantsList = new JList<>(servantsListModel);
    protected final JScrollPane servantsPane = new JScrollPane(servantsList, VERTICAL_SCROLLBAR_NEVER, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    protected final JButton renameBtn = new JButton("RENAME");
    protected final JButton sendTextBtn = new JButton("SEND TEXT");

    protected final JLabel counterLabel = new JLabel("COUNTER");
    protected final JLabel showPeriod = new JLabel("SHOW PERIOD (sec):");
    protected final JTextField showSeconds = new JTextField();
    protected final JButton startBtn = new JButton("START");
    protected final JButton stopBtn = new JButton("STOP");
    protected final JButton resetBtn = new JButton("RESET");


    protected final JLabel audioLabel = new JLabel("AUDIO");
    public final DefaultListModel<String> audioListModel = new DefaultListModel();
    protected final JList<String> audioList = new JList<>(audioListModel);
    protected final JScrollPane audioPane = new JScrollPane(audioList, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    protected final JButton audioPlay = new JButton("PLAY");
    protected final JButton audioAdd = new JButton("ADD");
    protected final JButton audioDel = new JButton("DEL");
    protected final JButton audioContinuousPlay = new JButton("CONT PLAY");
    protected final JButton audioStop = new JButton("STOP");

    protected final JLabel videoLabel = new JLabel("VIDEO");
    public final DefaultListModel<String> videoListModel = new DefaultListModel();
    protected final JList<String> videoList = new JList<>(videoListModel);
    protected final JScrollPane videoPane = new JScrollPane(videoList, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    protected final JButton videoPlay = new JButton("PLAY");
    protected final JButton videoAdd = new JButton("ADD");
    protected final JButton videoDel = new JButton("DEL");


    protected final JLabel pictureLabel = new JLabel("PICTURE");
    public final DefaultListModel<String> pictureListModel = new DefaultListModel();
    protected final JList<String> pictureList = new JList<>(pictureListModel);
    protected final JScrollPane picturePane = new JScrollPane(pictureList, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    protected final JButton pictureShow = new JButton("SHOW");
    protected final JButton pictureAdd = new JButton("ADD");
    protected final JButton pictureDel = new JButton("DEL");

    protected final JLabel taskTextLabel = new JLabel("Remaining tasks:");
    protected final JLabel taskNumber = new JLabel();

    protected final static Logger logger = Logger.getLogger(FrontEnd.class);

    @Override
    public void run() {
        try {
            refreshDirs();
            refreshTaskInfo();
        } catch (Exception e) {
            logger.error(e);
        }

        Globals.get().executor.schedule(this, 2, TimeUnit.SECONDS);

    }

    protected void rename() {
        String selectedValue = servantsList.getSelectedValue();
        if (servantsList.getSelectedValuesList().size() == 1 && selectedValue != null) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            JLabel label = new JLabel("Enter a new name:");
            JTextField text = new JTextField();
            panel.add(label);
            panel.add(text);
            String[] options = new String[]{"OK", "Cancel"};
            int option = JOptionPane.showOptionDialog(null, panel, "Rename",
                    JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[1]);
            if (option == 0) { // pressing OK button
                String newName = text.getText();
                if (!Globals.get().clientNameAddress.keySet().contains(newName)) {
                    ClientParam clientParam = Utils.getClientParam(selectedValue);
                    String oldName = clientParam.getName();
                    List<Long> address = Globals.get().clientNameAddress.remove(oldName);
                    Globals.get().clientNameAddress.put(newName, address);
                    clientParam.setName(newName);
                    Globals.get().addToFrontEndTasksIfNotExists(FrontEndTaskToDo.SAVE_NAME_ADDRESS);
                } else {
                    String message = newName + " already exists.";
                    JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    protected void sendText() {
        if (servantsList.getSelectedValuesList().size() > 0) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            JLabel label = new JLabel("Enter the message: ");
            JTextField text = new JTextField();
            panel.add(label);
            panel.add(text);
            String[] options = new String[]{"OK", "Cancel"};
            int option = JOptionPane.showOptionDialog(null, panel, "Message",
                    JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[1]);
            if (option == 0) { // pressing OK button
                String message = text.getText();
                List<String> selectedValuesList = servantsList.getSelectedValuesList();
                final AtomicInteger seconds = new AtomicInteger(30);
                try {
                    seconds.set(Integer.valueOf(showSeconds.getText()));
                } catch (Exception e) {
                    logger.error(e);
                }
                if (!selectedValuesList.isEmpty()) {
                    selectedValuesList.stream().forEach(selectedValue -> {
                        Globals.get().tasks.add(new hu.finominfo.rnet.taskqueue.Task(TaskToDo.SEND_MESSAGE, message, null, Utils.getIp(selectedValue), seconds.get()));
                    });
                }
            }
        }
    }

    protected void refreshDirs() {
        if (servantsList.getSelectedValuesList().size() < 2) {
            String selectedValue = servantsList.getSelectedValue();
            if (selectedValue != null) {
                Map<String, List<String>> dirs = Utils.getClientParam(selectedValue).getDirs();
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
    }

    private void refreshTaskInfo() {
        taskNumber.setText(String.valueOf(Globals.get().tasks.size() + (null == Globals.get().currentTask ? 0 : 1)));
    }

    protected void sendFile(final String destFolder, final FileType fileType) {
        List<String> selectedValuesList = servantsList.getSelectedValuesList();
        if (!selectedValuesList.isEmpty()) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            fileChooser.setDialogTitle("Select " + destFolder + " file");
            int result = fileChooser.showOpenDialog(FrontEndUtils.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                //System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                selectedValuesList.stream().forEach(selectedValue -> {
                    Globals.get().tasks.add(new hu.finominfo.rnet.taskqueue.Task(TaskToDo.SEND_FILE, selectedFile.getAbsolutePath(), fileType, Utils.getIp(selectedValue)));
                });
                servantsList.clearSelection();
            }
        }
    }


    protected void deleteFile(final String destFolder, final FileType fileType, JList<String> list) {
        List<String> selectedValuesList = servantsList.getSelectedValuesList();
        if (!selectedValuesList.isEmpty() && list.getSelectedValue() != null) {
            String fileName = list.getSelectedValue();
            if (fileName != null) {
                selectedValuesList.stream().forEach(selectedValue -> {
                    Globals.get().tasks.add(new hu.finominfo.rnet.taskqueue.Task(TaskToDo.DEL_FILE, fileName, fileType, Utils.getIp(selectedValue)));
                });
                servantsList.clearSelection();
            }
        }
    }

    protected void showPicture() {
        List<String> selectedValuesList = servantsList.getSelectedValuesList();
        if (!selectedValuesList.isEmpty() && pictureList.getSelectedValue() != null) {
            String fileName = pictureList.getSelectedValue();
            if (fileName != null) {
                selectedValuesList.stream().forEach(selectedValue -> {
                    ShowPicture showPicture = new ShowPicture(Utils.getFileType(FileType.PICTURE), fileName, Integer.valueOf(showSeconds.getText()));
                    Globals.get().tasks.add(new hu.finominfo.rnet.taskqueue.Task(TaskToDo.SEND_CONTROL, new ControlEvent(ControlType.SHOW_PICTURE, showPicture)));
                });
                servantsList.clearSelection();
            }
        }
    }

}
