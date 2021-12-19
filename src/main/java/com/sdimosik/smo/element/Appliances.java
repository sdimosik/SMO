package com.sdimosik.smo.element;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Appliances {

    private final Task[] appliance;
    private final double[] time;
    private final int capacity;
    private int size;

    public Appliances(int capacity) {
        this.capacity = capacity;
        this.appliance = new Task[capacity];
        Arrays.fill(appliance, null);
        this.size = 0;
        time = new double[capacity];
        Arrays.fill(time, 0);
    }

    public int offer(Task task) {
        int writeIdx = 0;

        boolean isFree = false;
        for (int i = 0; i < appliance.length; i++) {
            if (appliance[i] == null) {
                isFree = true;
                writeIdx = i;
                break;
            }
        }
        if (!isFree) return -1;

        appliance[writeIdx] = task;
        size++;

        time[writeIdx] += task.execTime;
        task.numAppliance = writeIdx;

        return writeIdx;
    }

    private void remove(int idx) {
        if (idx >= 0 && idx < capacity && appliance[idx] != null) {
            appliance[idx] = null;
            size--;
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isNotEmpty() {
        return size != 0;
    }

    public boolean isFull() {
        return size == capacity;
    }

    public boolean isNotFull() {
        return size != capacity;
    }

    public double getCompletionsTimeOfTask(double currentTime, EndlessSource input) {
        Task task = getTaskWhichFirstDone(currentTime, false, input.taskQueue.isEmpty());
        if (task == null) return currentTime;

        if (input.taskQueue.isEmpty()) {
            return task.getTimeToComplete();
        }

        if (task.getTimeToComplete() < input.taskQueue.peek().startTime) {
            return task.getTimeToComplete();
        }

        return currentTime;
    }

    public Task getCompleteTask(double currentTime, EndlessSource input) {
        return getTaskWhichFirstDone(currentTime, true, input.taskQueue.isEmpty());
    }

    private Task getTaskWhichFirstDone(double currentTime, boolean removeFlag, boolean isEmptyQueueTask) {
        Task task = null;
        double minTime = Double.MAX_VALUE;
        int pos = 0;
        for (int i = 0; i < capacity; i++) {
            if (appliance[i] != null
                && (appliance[i].isDone(currentTime) || (isEmptyQueueTask))
                && appliance[i].getTimeToComplete() < minTime
            ) {
                task = appliance[i];
                minTime = appliance[i].getTimeToComplete();
                pos = i;
            }
        }
        if (removeFlag) remove(pos);
        return task;
    }

    public List<Double> kUsedAppliance(double time) {
        List<Double> list = new LinkedList<>();
        for (int i = 0; i < capacity; i++) {
            double local = this.time[i];
            list.add(local / time);
        }
        return list;
    }

    public int size() {
        return size;
    }
}
