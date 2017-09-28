package hu.finominfo.rnet.frontend.controller;

/**
 * Created by kalman.kovacs@gmail.com on 2017.09.28.
 */
import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

public class ListSample {
    public static void main(String args[]) {
        String labels[] = { "A", "B", "C", "D","E", "F", "G", "H","I", "J" };

        String title = "JList Sample";
        JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JList list = new JList(labels);
        JScrollPane scrollPane = new JScrollPane(list);

        Container contentPane = f.getContentPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);

        f.setSize(200, 200);
        f.setVisible(true);
    }
}

