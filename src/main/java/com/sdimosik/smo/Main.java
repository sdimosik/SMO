package com.sdimosik.smo;

import com.sdimosik.smo.ui.MainUI;

import javax.swing.*;

public class Main {

    private static MainUI ui;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createGUI);
    }

    private static void createGUI() {
        ui = new MainUI();
        JPanel root = ui.getRootPanel();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(root);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setSize(1400, 700);
        frame.setVisible(true);
    }

    static class AnswerWorker extends SwingWorker<Integer, Integer>{

        @Override
        protected Integer doInBackground() throws Exception {
            return null;
        }


    }
}

