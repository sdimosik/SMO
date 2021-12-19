package com.sdimosik.smo.element;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Appliances {

    private final EndlessSource endlessSource;
    private final Task[] appliance;
    private final double[] time;
    private final int capacity;
    private int size;

    public Appliances(EndlessSource endlessSource, int capacity) {
        this.endlessSource = endlessSource;
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
                && (isEmptyQueueTask || ((appliance[i].isDone(currentTime) || checkQueueSource(i, currentTime))))
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

    private boolean checkQueueSource(int i, double currentTime) {
        Task task = appliance[i];

        AtomicBoolean res = new AtomicBoolean(false);
        endlessSource.taskQueue.forEach(tmp ->{
            if (task.getTimeToComplete() < tmp.startTime){
                res.set(true);
            }
        });

        return res.get();
    }

    /*
       Task task = appliance[i];

        AtomicDouble minStartTime = new AtomicDouble(Double.MAX_VALUE);
        endlessSource.taskQueue.forEach(tmp -> {
            if (tmp.startTime <= minStartTime.get()) {
                minStartTime.set(tmp.startTime);
            }
        });

        return task.getTimeToComplete() <= minStartTime.get();
     */

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
