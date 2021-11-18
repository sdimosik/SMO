package com.sdimosik.smo.element;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Appliances {

    private final Task[] appliance;
    private final int[] time;
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
        time = new int[capacity];
        Arrays.fill(time, 0);
    }

    public boolean offer(Task task) {
        if (isFull()) {
            return false;
        }

        writeIdx++;
        skipIdx(writeIdx, true);

        appliance[idx(writeIdx)] = task;
        size++;

        return true;
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

        if (task.getTimeToComplete() < input.taskQueue.peek().start) {
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

/*    public void updateTimeInfo() {
        for (int i = 0; i < capacity; i++) {
            if (appliance[i] != null) {
                time[i]++;
                appliance[i].executeTask(1);
            }
        }
    }

    public List<Task> getCompleteTasksAndCleanUp(int time) {
        List<Task> list = new LinkedList<>();
        for (int i = 0; i < capacity; i++) {
            if (appliance[i] != null && appliance[i].isDone()) {
                list.add(appliance[i]);
                appliance[i].setEnd(time);
                remove(i);
            }
        }
        return list;
    }*/

    private void remove(int idx) {
        if (idx >= 0 && idx < capacity && appliance[idx] != null) {
            appliance[idx] = null;
            size--;
        }
    }

    @Override
    public String toString() {
        return "Appliances{" +
            "appliance=" + Arrays.toString(appliance) +
            '}';
    }

    public List<Double> kUsedAppliance(int time) {
        List<Double> list = new LinkedList<>();
        for (int i = 0; i < capacity; i++) {
            int local = this.time[i];
            list.add((double) (local) / (double) (time));
        }
        return list;
    }

    public int size() {
        return size;
    }
}
