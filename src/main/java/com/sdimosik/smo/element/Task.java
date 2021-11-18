package com.sdimosik.smo.element;

import java.util.UUID;

import static com.sdimosik.smo.Utils.*;

public class Task implements Comparable<Task> {
    public final String name;
    public final int numSource;

    public State state;

    public final double TIME_TO_EXEC;
    public final double START_TIME;

    private double startExecute;
    private double endTime;

    public Task(int numSource, double startTime, double TIME_TO_EXEC) {
        this.START_TIME = startTime;
        this.endTime = startTime;
        this.state = State.START;
        this.TIME_TO_EXEC = TIME_TO_EXEC;

        this.numSource = numSource;
        this.name = UUID.randomUUID().toString();
    }

    public void updateState(State state, double time) {
        this.state = state;
        switch (this.state) {
            case FAIL:
            case DONE:
                this.endTime = time;
                break;
            case APPLIANCE:
                this.startExecute = time;
                break;
        }
    }

    public double getTimeToComplete(){
        return startExecute + TIME_TO_EXEC;
    }

    public boolean isDone(double currentTime) {
        return getTimeToComplete() < currentTime;
    }

    public void setEndTime(double time) {
        endTime = time;
    }

    public double getTime() {
        return endTime - START_TIME;
    }

    @Override
    public String toString() {
        return "\nTask{" +
            "name='" + name + '\'' +
            ", leftMinutesToComplete=" + TIME_TO_EXEC +
            "}";
    }

    @Override
    public int compareTo(Task task) {
        return Double.compare(START_TIME, task.START_TIME);
    }

    public double getStartExecute() {
        return startExecute;
    }

    public double getEndTime() {
        return endTime;
    }
}
