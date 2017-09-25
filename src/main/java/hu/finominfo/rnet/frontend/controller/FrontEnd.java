package hu.finominfo.rnet.frontend.controller;

import javax.swing.*;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.25.
 */
public class FrontEnd extends JFrame {



    public JPanel mainPanel;
    public JList servants;
    public DefaultListModel<String> servantsList = new DefaultListModel();

    public FrontEnd() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanel = new JPanel();
        servants = new JList(servantsList);
        add(mainPanel);
        add(servants);
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        new FrontEnd();
    }
}
