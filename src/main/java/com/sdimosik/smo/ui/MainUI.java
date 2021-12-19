package com.sdimosik.smo.ui;

import com.sdimosik.smo.App;
import com.sdimosik.smo.Report;
import com.sdimosik.smo.Utils;
import com.sdimosik.smo.element.Task;
import dnl.utils.text.table.TextTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainUI {
    private JPanel rootPanel;
    private JTable manualTable;
    private JButton goButton;
    private JRadioButton isManualRadioButton;
    private JTextField taskTextField;
    private JTextField sourceTextField;
    private JTextField bufferTextField;
    private JTextField appliancesTextField;
    private JTextField lambdaTextField;
    private JTextField meanTextField;
    private JTextField variantsTextField;
    private JButton nextButton;
    public boolean isPressedNext = false;
    private JButton resetButton;
    private JScrollPane scrollPane;
    private JScrollPane scrollPane1;
    private JTable autoTable;
    private JTextField avgProbFailTextField;
    private JTextField avgKUsedTextField;
    private JPanel autoPanel2;
    private JPanel autoPanel1;

    private int taskCount;
    private double lambda;
    private double mean;
    private double variants;

    private int countSource;
    private int countBuffer;
    private int countAppliances;

    private Task[] sourceInfo;
    private Task[] bufferInfo;
    private Task[] applianceInfo;

    private DefaultTableModel model;
    private DefaultTableModel model2;

    private Thread thread;
    private App app;
    private int currentState = 0;
    private Task newTask;
    boolean isUpdateManual = false;

    private final Object lock = new Object();

    public MainUI() {
        autoPanel2.setVisible(false);
        autoPanel1.setVisible(false);
        isManualRadioButton.addItemListener(itemEvent -> {
            // TODO clear
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                //createManualTable();
            } else if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
                //createAutoTable();
            }
        });
        isManualRadioButton.setSelected(true);
        final int[] verticalScrollBarMaximumValue = {scrollPane.getVerticalScrollBar().getMaximum()};
        scrollPane.getVerticalScrollBar().addAdjustmentListener(
            e -> {
                if ((verticalScrollBarMaximumValue[0] - e.getAdjustable().getMaximum()) == 0)
                    return;
                e.getAdjustable().setValue(e.getAdjustable().getMaximum());
                verticalScrollBarMaximumValue[0] = scrollPane.getVerticalScrollBar().getMaximum();
            });

        goButton.addActionListener(actionEvent -> {
            countSource = Integer.parseInt(sourceTextField.getText());
            countBuffer = Integer.parseInt(bufferTextField.getText());
            countAppliances = Integer.parseInt(appliancesTextField.getText());

            sourceInfo = new Task[countSource];
            bufferInfo = new Task[countBuffer];
            applianceInfo = new Task[countAppliances];

            Arrays.fill(sourceInfo, null);
            Arrays.fill(bufferInfo, null);
            Arrays.fill(applianceInfo, null);

            taskCount = Integer.parseInt(taskTextField.getText());
            lambda = Double.parseDouble(lambdaTextField.getText());
            mean = Double.parseDouble(meanTextField.getText());
            variants = Double.parseDouble(variantsTextField.getText());


            goButton.setVisible(false);
            resetButton.setVisible(true);
            isManualRadioButton.setVisible(false);
            if (isManualRadioButton.isSelected()) {
                createManualTable();
                nextButton.setVisible(true);
                autoPanel2.setVisible(false);
                autoPanel1.setVisible(false);
            } else {
                createAutoTable();
                nextButton.setVisible(false);
                autoPanel2.setVisible(true);
                autoPanel1.setVisible(true);
            }

            app = new App(
                isManualRadioButton.isSelected() ? 0 : 1,
                taskCount,
                countSource,
                countBuffer,
                countAppliances,
                lambda,
                mean,
                variants,
                this);
            app.prepare();
            currentState = 0;

            if (!isManualRadioButton.isSelected()){
                app.calc();
            }
        });

        resetButton.addActionListener(actionEvent -> {
            nextButton.setVisible(false);
            goButton.setVisible(true);
            resetButton.setVisible(false);
            isManualRadioButton.setVisible(true);
            if (isManualRadioButton.isSelected()) {
                createManualTable();
            } else {
                createAutoTable();
            }
        });
        resetButton.setVisible(false);

        nextButton.addActionListener(actionEvent -> {
            if (!app.isTasksExist()){
                return;
            }

            isUpdateManual = false;
            do {
                nextStep();
            } while (!isUpdateManual);
        });
        nextButton.setVisible(false);
    }

    private void nextStep() {
        int idx = currentState % 5;
        System.out.println(idx);
        switch (idx) {
            case 0:
                newTask = null;
                if (!app.isTasksExist()) {
                    nextButton.setVisible(false);
                    break;
                }
                newTask = app.step1();
                break;
            case 1:
                app.step2(newTask);
                break;
            case 2:
                app.step3(newTask);
                break;
            case 3:
                app.step4();
                break;
            case 4:
                if (app.step5()) currentState--;
                break;
        }

        currentState++;
    }

    public void update(Task task) {
        int numSource = task.numSource;
        int numBuffer = task.numBuffer;
        int numAppliance = task.numAppliance;

        switch (task.state) {
            case START:
                sourceInfo[numSource] = task;
                break;
            case BUFFER:
                sourceInfo[numSource] = null;
                bufferInfo[numBuffer] = task;
                break;
            case APPLIANCE:
                bufferInfo[numBuffer] = null;
                applianceInfo[numAppliance] = task;
                break;
            case DONE:
                applianceInfo[numAppliance] = null;
                break;
            case FAIL:
                bufferInfo[numBuffer] = null;
                break;
        }

        printEvent(task);
    }

    private void printEvent(Task task) {
        Utils.State stage = task.state;

        List<Object> inf = new ArrayList<>();
        double time = getTimeForState(task);
        inf.add(time);

        for (int i = 0; i < countSource; i++) {
            if (sourceInfo[i] == null) {
                inf.add("");
                continue;
            }
            String sourceAndNum = "" + sourceInfo[i].numSource + "." + sourceInfo[i].num;
            inf.add(sourceAndNum);
        }

        for (int i = 0; i < countBuffer; i++) {
            if (bufferInfo[i] == null) {
                inf.add("");
                continue;
            }
            String sourceAndNum = "" + bufferInfo[i].numSource + "." + bufferInfo[i].num;
            inf.add(sourceAndNum);
        }

        for (int i = 0; i < countAppliances; i++) {
            if (applianceInfo[i] == null) {
                inf.add("");
                continue;
            }
            String sourceAndNum = "" + applianceInfo[i].numSource + "." + applianceInfo[i].num;
            inf.add(sourceAndNum);
        }

        if (stage != Utils.State.DONE) {
            inf.add("");
        } else {
            inf.add("" + task.numSource + "." + task.num);
        }

        if (stage != Utils.State.FAIL) {
            inf.add("");
        } else {
            inf.add("" + task.numSource + "." + task.num);
        }

        Object[] data = new Object[inf.size()];
        for (int i = 0; i < inf.size(); i++) {
            data[i] = inf.get(i);
        }


        model.addRow(data);
        System.out.println("add row");
        isUpdateManual = true;
    }

    private double getTimeForState(Task task) {
        switch (task.state) {
            case START:
                return task.startTime;
            case BUFFER:
                return task.getStartBufferTime();
            case APPLIANCE:
                return task.getStartAppliancesTime();
            case DONE:
                return task.getEndTime();
            case FAIL:
                return task.getFailTime();
            default:
                return 0;
        }
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    private void createManualTable() {
        model = new DefaultTableModel();
        manualTable.setModel(model);
        model.addColumn("Time");

        for (int i = 0; i < countSource; i++) {
            model.addColumn("И" + i);
        }

        for (int i = 0; i < countBuffer; i++) {
            model.addColumn("Б" + i);
        }

        for (int i = 0; i < countAppliances; i++) {
            model.addColumn("П" + i);
        }
        model.addColumn("OK");
        model.addColumn("ОТК");
    }

    private void createAutoTable() {
        model = new DefaultTableModel();
        model2 = new DefaultTableModel();
        manualTable.setModel(model);
        autoTable.setModel(model2);
        //model.addColumn("Auto");
    }

    public void updateReport(Report report) {
        String[] namesT1 = new String[8];
        namesT1[0] = "numOfSource";
        namesT1[1] = "countTask";
        namesT1[2] = "Prob of Fail";
        namesT1[3] = "T exist";
        namesT1[4] = "T buffer";
        namesT1[5] = "T appliance";
        namesT1[6] = "D buffer";
        namesT1[7] = "D appliance";

        for (String column : namesT1){
            model.addColumn(column);
        }

        int sizeT1 = report.countGeneratedTask4Source.size();
        Object[] dataT1 = new Object[namesT1.length];
        for (int i = 0; i < sizeT1; i++) {
            dataT1[0] = i;
            dataT1[1] = report.countGeneratedTask4Source.get(i);
            dataT1[2] = report.probabilityOfFailure4Source.get(i);
            dataT1[3] = report.avgTimeInBuffer.get(i) + report.avgTimeInAppliance.get(i);
            dataT1[4] = report.avgTimeInBuffer.get(i);
            dataT1[5] = report.avgTimeInAppliance.get(i);
            dataT1[6] = report.bufferDispersion.get(i);
            dataT1[7] = report.applianceDispersion.get(i);
            model.addRow(dataT1);
        }

        double avgProb = 0;
        for(double value : report.probabilityOfFailure4Source) avgProb += value;
        avgProb /= report.probabilityOfFailure4Source.size();
        avgProbFailTextField.setText(String.valueOf(avgProb));

        //---------------------------
        String[] namesT2 = new String[2];
        namesT2[0] = "numOfAppliance";
        namesT2[1] = "K of Used";
        for (String column : namesT2){
            model2.addColumn(column);
        }

        int sizeT2 = report.kUsedAppliance.size();
        Object[] dataT2 = new Object[namesT2.length];
        for (int i = 0; i < sizeT2; i++) {
            dataT2[0] = i;
            dataT2[1] = report.kUsedAppliance.get(i);
            model2.addRow(dataT2);
        }

        double avgK = 0;
        for(double value : report.kUsedAppliance) avgK += value;
        avgK /= report.kUsedAppliance.size();
        avgKUsedTextField.setText(String.valueOf(avgK));
    }
}
