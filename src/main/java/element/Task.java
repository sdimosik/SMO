package element;

import java.util.UUID;

public class Task {
    private final String name;
    private double leftMinutesToComplete = 100;
    private int start;
    private int end;

    public final int numSource;

    public Task(int numSource) {
        this.numSource = numSource;
        this.name = UUID.randomUUID().toString();
    }

    public void executeTask(double minutes) {
        leftMinutesToComplete -= minutes;
    }

    public boolean isDone() {
        return leftMinutesToComplete <= 0;
    }

    public void setStart(int time) {
        start = time;
    }

    public void setEnd(int time) {
        end = time;
    }

    public int getTime() {
        return end - start;
    }

    @Override
    public String toString() {
        return "\nTask{" +
            "name='" + name + '\'' +
            ", leftMinutesToComplete=" + leftMinutesToComplete +
            "}";
    }
}
