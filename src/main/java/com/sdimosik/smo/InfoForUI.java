package com.sdimosik.smo;

import com.sdimosik.smo.element.Task;

import static com.sdimosik.smo.Utils.State;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class InfoForUI {
    private final int countSource;
    private final int countBuffer;
    private final int countAppliances;

    private final String formatTime = "%-15s";
    private final String formatTimeDouble = "%-15.3f";
    private final String formatSource = "%-7s";

    private final Task[] source;
    private final Task[] buffer;
    private final Task[] appliance;

    public InfoForUI(int countSource, int countBuffer, int countAppliances) {
        this.countSource = countSource;
        this.countBuffer = countBuffer;
        this.countAppliances = countAppliances;

        source = new Task[countSource];
        buffer = new Task[countBuffer];
        appliance = new Task[countAppliances];

        Arrays.fill(source, null);
        Arrays.fill(buffer, null);
        Arrays.fill(appliance, null);
    }

    public void startPrint() {
        System.out.printf(formatTime, "Time");

        for (int i = 0; i < countSource; i++) {
            System.out.printf(formatSource, String.format("%s%d", "И", i));
        }
        for (int i = 0; i < countBuffer; i++) {
            System.out.printf(formatSource, String.format("%s%d", "Б", i));
        }
        for (int i = 0; i < countAppliances; i++) {
            System.out.printf(formatSource, String.format("%s%d", "П", i));
        }
        System.out.printf(formatSource, "OK");
        System.out.printf(formatSource, "ОТК");
    }

    public void update(Task task) {
        int numSource = task.numSource;
        int numBuffer = task.numBuffer;
        int numAppliance = task.numAppliance;

        switch (task.state) {
            case START:
                source[numSource] = task;
                break;
            case BUFFER:
                source[numSource] = null;
                buffer[numBuffer] = task;
                break;
            case APPLIANCE:
                buffer[numBuffer] = null;
                appliance[numAppliance] = task;
                break;
            case DONE:
                appliance[numAppliance] = null;
                break;
            case FAIL:
                buffer[numBuffer] = null;
                break;
        }

        printEvent(task);
    }

    private void printEvent(Task task) {
        State stage = task.state;

        double time = getTimeForState(task);
        System.out.printf(formatTimeDouble, time);

        formatPrintOurArr(countSource, source);

        formatPrintOurArr(countBuffer, buffer);

        formatPrintOurArr(countAppliances, appliance);

        if (stage != State.DONE) {
            System.out.printf(formatSource, "");
        } else {
            System.out.printf(formatSource, "" + task.numSource + "." + task.num);
        }

        if (stage != State.FAIL) {
            System.out.printf(formatSource, "");
        } else {
            System.out.printf(formatSource, "" + task.numSource + "." + task.num);
        }
    }

    private void formatPrintOurArr(int size, Task[] arr) {
        for (int i = 0; i < size; i++) {
            if (arr[i] == null) {
                System.out.printf(formatSource, "");
                continue;
            }
            String sourceAndNum = "" + arr[i].numSource + "." + arr[i].num;
            System.out.printf(formatSource, sourceAndNum);
        }
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
}