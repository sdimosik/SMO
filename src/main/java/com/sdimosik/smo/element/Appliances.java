package com.sdimosik.smo.element;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Appliances {

    private final Task[] appliance;
    private final double[] time;
    private final int capacity;
    private int size;

    private int writeIdx;
    private int readIdx;

    public Appliances(int capacity) {
        this.capacity = capacity;
        this.appliance = new Task[capacity];
        Arrays.fill(appliance, null);
        this.readIdx = 0;
        this.writeIdx = -1;
        this.size = 0;
        time = new double[capacity];
        Arrays.fill(time, 0);
    }

    public int offer(Task task) {
        if (isFull()) {
            return -1;
        }

        writeIdx++;
        skipIdx(writeIdx, true);

        appliance[idx(writeIdx)] = task;
        size++;

        time[idx(writeIdx)] += task.execTime;
        task.numAppliance = idx(writeIdx);

        return idx(writeIdx);
    }

    private void remove(int idx) {
        if (idx >= 0 && idx < capacity && appliance[idx] != null) {
            appliance[idx] = null;
            size--;
        }
    }

    private int idx(int idx) {
        return idx % capacity;
    }

    private void skipIdx(final int start, boolean skipFullIdx) {
        boolean finded = false;

        for (int i = idx(start); i < capacity; i++) {
            if (skipFullIdx == (appliance[i] == null)) {
                finded = true;
                writeIdx = i;
                break;
            }
        }
        if (finded) return;

        for (int i = 0; i < idx(start); i++) {
            if (skipFullIdx == (appliance[i] == null)) {
                writeIdx = i;
                break;
            }
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

    @Override
    public String toString() {
        return "Appliances{" +
            "appliance=" + Arrays.toString(appliance) +
            '}';
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
