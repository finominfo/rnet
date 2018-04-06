package hu.finominfo.rnet.frontend.controller;

import hu.finominfo.rnet.common.Globals;
import hu.finominfo.rnet.common.Utils;
import hu.finominfo.rnet.communication.tcp.events.control.ControlEvent;
import hu.finominfo.rnet.communication.tcp.events.control.ControlType;
import hu.finominfo.rnet.communication.tcp.events.control.objects.*;
import hu.finominfo.rnet.communication.tcp.events.file.FileType;
import hu.finominfo.rnet.communication.tcp.server.ClientParam;
import hu.finominfo.rnet.taskqueue.FrontEndTaskToDo;
import hu.finominfo.rnet.taskqueue.TaskToDo;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static javax.swing.ScrollPaneConstants.*;

/**
 * Created by kalman.kovacs@gmail.com on 2017.10.08.
 */
public class FrontEndUtils extends JFrame implements Runnable {

    protected final ConcurrentMap<String, Long> resetNotEnabledUntil = new ConcurrentHashMap<>();

    protected volatile boolean shouldRefreshAll = false;

    protected final ImageIcon ServantsIcon = new ImageIcon("resources" + File.separator + "servants.png");
    protected final ImageIcon SendTextIcon = new ImageIcon("resources" + File.separator + "sendtext.png");
    protected final ImageIcon RenameIcon = new ImageIcon("resources" + File.separator + "rename.png");


    protected final JLabel servantsLabel = new JLabel(ServantsIcon);
    public final DefaultListModel<String> servantsListModel = new DefaultListModel();
    public final JList<String> servantsList = new JList<String>(servantsListModel);
    protected final JScrollPane servantsPane = new JScrollPane(servantsList, VERTICAL_SCROLLBAR_NEVER, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    protected final JButton renameBtn = new JButton(RenameIcon);
    protected final JButton sendTextBtn = new JButton(SendTextIcon);


    protected final JLabel counterLabel = new JLabel("COUNTER");
    protected final JLabel showPeriod = new JLabel("SHOW TIME (sec):");
    protected final JTextField showSeconds = new JTextField();


    protected final ImageIcon StartIcon = new ImageIcon("resources" + File.separator + "play.png");
    protected final ImageIcon StopIcon = new ImageIcon("resources" + File.separator + "stop.png");
    protected final ImageIcon ResetIcon = new ImageIcon("resources" + File.separator + "reset.png");
    protected final ImageIcon MinIcon = new ImageIcon("resources" + File.separator + "min.png");
    protected final ImageIcon AudioIcon = new ImageIcon("resources" + File.separator + "audio.png");
    protected final ImageIcon VideoIcon = new ImageIcon("resources" + File.separator + "video.png");
    protected final ImageIcon PictureIcon = new ImageIcon("resources" + File.separator + "picture.png");
    protected final ImageIcon SendIcon = new ImageIcon("resources" + File.separator + "send.png");
    protected final ImageIcon StopIcon2 = new ImageIcon("resources" + File.separator + "stop2.png");
    protected final ImageIcon AddIcon = new ImageIcon("resources" + File.separator + "add.png");
    protected final ImageIcon DelIcon = new ImageIcon("resources" + File.separator + "del.png");


    protected final JButton startBtn = new JButton(StartIcon);
    protected final JButton stopBtn = new JButton(StopIcon);
    protected final JButton resetBtn = new JButton(ResetIcon);
    protected final JButton resetLabel = new JButton(MinIcon);
    protected final JTextField resetMins = new JTextField();


    protected final JLabel audioLabel = new JLabel(AudioIcon);
    public final DefaultListModel<String> audioListModel = new DefaultListModel();
    protected final JList<String> audioList = new JList<>(audioListModel);
    protected final JScrollPane audioPane = new JScrollPane(audioList, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    protected final JButton audioPlay = new JButton(SendIcon);
    protected final JButton audioAdd = new JButton(AddIcon);
    protected final JButton audioDel = new JButton(DelIcon);
    protected final JButton audioContinuousPlay = new JButton(SendIcon);
    protected final JButton audioStop = new JButton(StopIcon2);

    protected final JLabel videoLabel = new JLabel(VideoIcon);
    public final DefaultListModel<String> videoListModel = new DefaultListModel();
    protected final JList<String> videoList = new JList<String>(videoListModel) {
        @Override
        public String getToolTipText(MouseEvent evt) {
            return getToolTip(evt, (String) (getModel().getElementAt(locationToIndex(evt.getPoint()))), Globals.videoFolder);
        }
    };

    protected final JScrollPane videoPane = new JScrollPane(videoList, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    protected final JButton videoPlay = new JButton(SendIcon);
    protected final JButton videoAdd = new JButton(AddIcon);
    protected final JButton videoDel = new JButton(DelIcon);
    protected final JButton videoContinuousPlay = new JButton("CONT PLAY");
    protected final JButton videoStop = new JButton(StopIcon2);


    protected final JLabel pictureLabel = new JLabel(PictureIcon);
    public final DefaultListModel<String> pictureListModel = new DefaultListModel();
    protected final JList<String> pictureList = new JList<String>(pictureListModel) {
        @Override
        public String getToolTipText(MouseEvent evt) {
            return getToolTip(evt, (getModel().getElementAt(locationToIndex(evt.getPoint()))), Globals.pictureFolder);
        }
    };
    ;
    protected final JScrollPane picturePane = new JScrollPane(pictureList, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    protected final JButton pictureShow = new JButton(SendIcon);
    protected final JButton pictureAdd = new JButton(AddIcon);
    protected final JButton pictureDel = new JButton(DelIcon);

    protected final JLabel taskTextLabel = new JLabel("Remaining tasks:");
    protected final JLabel taskNumber = new JLabel();

    protected final JTextArea status = new JTextArea();
    protected final JLabel statusLabel = new JLabel("STATUS");

    protected final static Logger logger = Logger.getLogger(FrontEnd.class);

    private String getToolTip(MouseEvent evt, String item, String folder) {
        String shortName = item.substring(0, item.lastIndexOf('.'));
        String miniName = shortName + "-mini" + ".jpg";
        String name = shortName + ".jpg";
        String pathAndMiniName = folder + File.separator + miniName;
        String pathAndName = folder + File.separator + name;
        File fileMiniName = new File(pathAndMiniName);
        File fileName = new File(pathAndName);
        if (fileMiniName.exists() && !fileMiniName.isDirectory()) {
            return "<html><img src=\"file:" + pathAndMiniName + "\"></html>";
        } else if (!fileMiniName.exists() && fileName.exists() && !fileName.isDirectory()) {
            makeMini(fileName, fileMiniName);
            return "<html><img src=\"file:" + pathAndMiniName + "\"></html>";
        } else {
            return "No tooltip found at: " + pathAndName;
        }
    }

    private void makeMini(File fileName, File fileMiniName) {
        try {
            BufferedImage image = ImageIO.read(fileName);
            int height = image.getHeight();
            int width = image.getWidth();
            double diff = 150;
            if (height > width) {
                diff /= height;
            } else {
                diff /= width;
            }
            Image scaledImage = image.getScaledInstance((int) (diff * width), (int) (diff * height), Image.SCALE_SMOOTH);
            BufferedImage bi = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g2 = bi.createGraphics();
            g2.drawImage(scaledImage, 0, 0, null);
            g2.dispose();
            ImageIO.write(bi, "jpg", fileMiniName);
        } catch (Exception e) {
            logger.error(Utils.getStackTrace(e));
        }
    }

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
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setPreferredSize(new Dimension(300, 200));
            JLabel label = new JLabel("Enter the message: ");
            //label.setBounds(50, 50, 200, 40);
            JTextArea text = new JTextArea();
            text.setFont(new Font("", Font.PLAIN, 20));
            text.setLineWrap(true);
            text.setWrapStyleWord(true);
            text.setRows(7);
            //text.setBounds(50, 100, 400, 250);
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
        String audioListSelectedValue = audioList.getSelectedValue();
        String videoListSelectedValue = videoList.getSelectedValue();
        String pictureListSelectedValue = pictureList.getSelectedValue();
        if (shouldRefreshAll) {
            shouldRefreshAll = false;
            videoListModel.clear();
            audioListModel.clear();
            pictureListModel.clear();
        }
        if (servantsList.getSelectedValuesList().isEmpty()) {
            startBtn.setEnabled(false);
            stopBtn.setEnabled(false);
            resetShouldDisabled();
        } else {
            startBtn.setEnabled(true);
            stopBtn.setEnabled(true);
            if (servantsList.getSelectedValuesList().stream().filter(s -> resetNotEnabledUntil.get(s) == null ||  resetNotEnabledUntil.get(s) < System.currentTimeMillis()).findAny().isPresent()) {
                resetShouldEnabled();
            }
        }
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
                audioList.setSelectedValue(audioListSelectedValue, false);
                videoList.setSelectedValue(videoListSelectedValue, false);
                pictureList.setSelectedValue(pictureListSelectedValue, false);
                status.setText(Utils.getClientParam(selectedValue).getStatus());
            } else {
                videoListModel.clear();
                audioListModel.clear();
                pictureListModel.clear();
                status.setText("");
            }
        } else {
            status.setText("");
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
                selectedValuesList.stream().forEach(selectedValue -> {
                    Globals.get().tasks.add(new hu.finominfo.rnet.taskqueue.Task(TaskToDo.SEND_FILE, selectedFile.getAbsolutePath(), fileType, Utils.getIp(selectedValue)));
                });
                if (selectedValuesList.size() != 1) {
                    servantsList.clearSelection();
                }
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
                if (selectedValuesList.size() != 1) {
                    servantsList.clearSelection();
                }
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
                    ControlEvent controlEvent = new ControlEvent(ControlType.SHOW_PICTURE, showPicture);
                    Globals.get().tasks.add(new hu.finominfo.rnet.taskqueue.Task(TaskToDo.SEND_CONTROL, controlEvent, Utils.getIp(selectedValue)));
                });
                if (selectedValuesList.size() != 1) {
                    servantsList.clearSelection();
                }

            }
        }
    }

    protected void playVideo(ControlType controlType) {
        List<String> selectedValuesList = servantsList.getSelectedValuesList();
        if (!selectedValuesList.isEmpty() && videoList.getSelectedValue() != null) {
            String fileName = videoList.getSelectedValue();
            if (fileName != null) {
                selectedValuesList.stream().forEach(selectedValue -> {
                    PlayVideo playVideo = new PlayVideo(Utils.getFileType(FileType.VIDEO), fileName, Integer.valueOf(showSeconds.getText()));
                    ControlEvent controlEvent = new ControlEvent(controlType, playVideo);
                    Globals.get().tasks.add(new hu.finominfo.rnet.taskqueue.Task(TaskToDo.SEND_CONTROL, controlEvent, Utils.getIp(selectedValue)));
                });
                if (selectedValuesList.size() != 1) {
                    servantsList.clearSelection();
                }
            }
        }
    }

    protected void playAudio(ControlType controlType) {
        List<String> selectedValuesList = servantsList.getSelectedValuesList();
        if (!selectedValuesList.isEmpty() && audioList.getSelectedValue() != null) {
            String fileName = audioList.getSelectedValue();
            if (fileName != null) {
                selectedValuesList.stream().forEach(selectedValue -> {
                    PlayAudio playVideo = new PlayAudio(Utils.getFileType(FileType.AUDIO), fileName, Integer.valueOf(showSeconds.getText()));
                    ControlEvent controlEvent = new ControlEvent(controlType, playVideo);
                    Globals.get().tasks.add(new hu.finominfo.rnet.taskqueue.Task(TaskToDo.SEND_CONTROL, controlEvent, Utils.getIp(selectedValue)));
                });
                if (selectedValuesList.size() != 1) {
                    servantsList.clearSelection();
                }
            }
        }
    }

    protected void sendResetCounter() {
        List<String> selectedValuesList = servantsList.getSelectedValuesList();
        selectedValuesList.stream().forEach(selectedValue -> {
            String resetMinsText = resetMins.getText();
            int minutes;
            try {
                minutes = Integer.valueOf(resetMinsText);
            } catch (Exception e) {
                minutes = 0;
            }
            if (minutes > 30) {
                resetMins.setText("");
            }
            ControlEvent controlEvent = new ControlEvent(ControlType.RESET_COUNTER, new ResetCounter(minutes));
            Globals.get().tasks.add(new hu.finominfo.rnet.taskqueue.Task(TaskToDo.SEND_CONTROL, controlEvent, Utils.getIp(selectedValue)));
            resetShouldDisabledThenEnabled(selectedValue);
        });
        if (selectedValuesList.size() != 1) {
            servantsList.clearSelection();
        }
    }

    protected void resetShouldDisabledThenEnabled(String selectedValue) {
        resetShouldDisabled();
        resetNotEnabledUntil.put(selectedValue, System.currentTimeMillis() + 2000);
    }

    protected void resetShouldDisabled() {
        resetBtn.setEnabled(false);
    }

    protected void resetShouldEnabled() {
        resetBtn.setEnabled(true);
    }

    protected void sendOnlyControl(ControlType controlType) {
        List<String> selectedValuesList = servantsList.getSelectedValuesList();
        selectedValuesList.stream().forEach(selectedValue -> {
            Globals.get().tasks.add(new hu.finominfo.rnet.taskqueue.Task(TaskToDo.SEND_CONTROL, new ControlEvent(controlType), Utils.getIp(selectedValue)));
        });
        if (selectedValuesList.size() != 1) {
            servantsList.clearSelection();
        }
    }

    protected void sendControlWithName(ControlType controlType, String name) {
        List<String> selectedValuesList = servantsList.getSelectedValuesList();
        selectedValuesList.stream().forEach(selectedValue -> {
            Globals.get().tasks.add(new hu.finominfo.rnet.taskqueue.Task(TaskToDo.SEND_CONTROL, new ControlEvent(controlType, new Name(name)), Utils.getIp(selectedValue)));
        });
        if (selectedValuesList.size() != 1) {
            servantsList.clearSelection();
        }
    }

    protected void showTimerGui() {
        Globals.get().getAllCounterWithCreateIfNecessary();
    }
}
