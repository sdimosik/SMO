package com.sdimosik.smo.element;

import java.util.UUID;

import static com.sdimosik.smo.Utils.*;

public class Task implements Comparable<Task> {
    public final String name;
    public final int numSource;
    public final int num;

    public int numBuffer;
    public int numAppliance;

    public State state;

    public final double startTime;
    private double startBufferTime;
    private double startAppliancesTime;
    public final double execTime;
    private double failTime;
    private double endTime;

    public Task(int numSource, int num, double startTime, double execTime) {
        this.startTime = startTime;
        this.state = State.START;
        this.execTime = execTime;

        this.numSource = numSource;
        this.num = num;
        this.name = UUID.randomUUID().toString();
    }

    public void updateState(State state, double time) {
        this.state = state;
        switch (this.state) {
            case BUFFER:
                this.startBufferTime = time;
                break;
            case APPLIANCE:
                this.startAppliancesTime = time;
                break;
            case FAIL:
                this.failTime = time;
            case DONE:
                this.endTime = time;
                break;
        }
    }

    public double getTimeToComplete() {
        return startAppliancesTime + execTime;
    }

    public boolean isDone(double currentTime) {
        return getTimeToComplete() < currentTime;
    }

    public void setEndTime(double time) {
        endTime = time;
    }

    public double getStartAppliancesTime() {
        return startAppliancesTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public double getStartBufferTime() {
        return startBufferTime;
    }

    public double getFailTime() {
        return failTime;
    }

    @Override
    public int compareTo(Task task) {
        return Double.compare(startTime, task.startTime);
    }
}
