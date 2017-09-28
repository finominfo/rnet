package hu.finominfo.rnet.frontend.controller;

import hu.finominfo.rnet.common.Globals;

import javax.swing.*;
import java.awt.*;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.25.
 */
public class FrontEnd extends JFrame {

    private final JList<String> servants;
    public final DefaultListModel<String> servantsList = new DefaultListModel();
    private final String labels[] = { "A", "B", "C", "D","E", "F", "G", "H","I", "J" };
    private final JButton renameBtn;
    private final JLabel servantsLabel;
    private final JScrollPane servantsPane;

    private final JLabel audioLabel;
    private final JPanel audioPanel;
    private final JScrollPane audioPane;

    public FrontEnd() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("RNET Controller - version " + Globals.getVersion());
        servantsLabel = new JLabel("SERVANTS");
        servantsLabel.setFont(new Font(servantsLabel.getFont().getName(), Font.BOLD, 25));
        servantsLabel.setBounds(25, 5, 155, 30);
        servants = new JList<>(servantsList);
        servants.setSelectionMode(SINGLE_SELECTION);
        //servants.setBounds(5, 40, 200, 365);
        renameBtn = new JButton("RENAME");
        renameBtn.setBounds(40, 420, 120, 30);
        JPanel test = new JPanel();
        test.setBounds(5, 40, 200, 365);
        servantsPane = new JScrollPane(test,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,  HORIZONTAL_SCROLLBAR_AS_NEEDED);
        test.add(servants);
        test.setVisible(true);


        audioLabel = new JLabel("AUDIO");
        audioLabel.setFont(new Font(servantsLabel.getFont().getName(), Font.BOLD, 25));
        audioLabel.setBounds(225, 5, 155, 30);
        audioPanel = new JPanel();
        audioPanel.setBounds(220, 40, 200, 365);
        audioPane = new JScrollPane(audioPanel);
        getContentPane().add(audioPane, BorderLayout.CENTER);


        //add(servantsPane);

        add(servantsLabel);
        add(test);
        add(renameBtn);

        add(audioLabel);
        add(audioPanel);
        setSize(1024, 768);
        setLayout(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new FrontEnd();
    }
}
