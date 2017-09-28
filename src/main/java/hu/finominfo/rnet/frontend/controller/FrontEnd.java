package hu.finominfo.rnet.frontend.controller;

import hu.finominfo.rnet.common.Globals;

import javax.swing.*;
import java.awt.*;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.25.
 */
public class FrontEnd extends JFrame {

    private final JList<String> servants;
    public final DefaultListModel<String> servantsList = new DefaultListModel();
    private final JButton renameBtn;
    private final JLabel servantsLabel;

    public FrontEnd() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("RNET Controller - version " + Globals.VERSION);
        servantsLabel = new JLabel("SERVANTS");
        servantsLabel.setFont(new Font(servantsLabel.getFont().getName(), Font.BOLD, 25));
        servantsLabel.setBounds(25, 5, 155, 30);
        servants = new JList<>(servantsList);
        servants.setSelectionMode(SINGLE_SELECTION);
        servants.setBounds(5, 40, 200, 365);
        servantsList.addElement("fdfdf");
        servantsList.addElement("fdfdf");
        servantsList.addElement("fd343");
        servantsList.addElement("fdfdf");
        servantsList.addElement("fdfdf");
        servantsList.addElement("fdfdf");
        servantsList.addElement("fdf3f");
        servantsList.addElement("fdf3f");
        servantsList.addElement("fdf3f");
        servantsList.addElement("fdf3f");
        servantsList.addElement("fdfdf");
        servantsList.addElement("fdfdf");
        servantsList.addElement("fsdff");
        servantsList.addElement("fdfdf");
        servantsList.addElement("fdfdf");
        servantsList.addElement("fwerw");
        servantsList.addElement("fdfdf");
        servantsList.addElement("fdfgf");
        servantsList.addElement("fdfdf");
        servantsList.addElement("qwewg");
        renameBtn = new JButton("RENAME");
        renameBtn.setBounds(40, 420, 120, 30);
        add(servants);
        //add(new JScrollPane(servants));
        add(servantsLabel);
        add(renameBtn);
        setSize(1024, 768);
        setLayout(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new FrontEnd();
    }
}
