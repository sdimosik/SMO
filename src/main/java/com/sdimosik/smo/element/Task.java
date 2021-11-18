package com.sdimosik.smo.element;

import java.util.UUID;

import static com.sdimosik.smo.Utils.*;

public class Task implements Comparable<Task> {
    public final String name;
    public final int numSource;

    public State state;

    public final double timeToExec;
    public final double start;

    private double startExecute;
    private double end;

    public Task(int numSource, double startTime, double timeToExec) {
        this.start = startTime;
        this.end = startTime;
        this.state = State.START;
        this.timeToExec = timeToExec;

        this.numSource = numSource;
        this.name = UUID.randomUUID().toString();
    }

    public void updateState(State state, double time) {
        this.state = state;
        switch (this.state) {
            case FAIL:
            case DONE:
                this.end = time;
                break;
            case APPLIANCE:
                this.startExecute = time;
                break;
        }
    }

    public double getTimeToComplete(){
        return startExecute + timeToExec;
    }

    public boolean isDone(double currentTime) {
        return getTimeToComplete() < currentTime;
    }

    public void setEnd(double time) {
        end = time;
    }

    public double getTime() {
        return end - start;
    }

    @Override
    public String toString() {
        return "\nTask{" +
            "name='" + name + '\'' +
            ", leftMinutesToComplete=" + timeToExec +
            "}";
    }

    @Override
    public int compareTo(Task task) {
        return Double.compare(start, task.start);
    }
}
