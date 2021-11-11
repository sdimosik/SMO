package element;

import java.util.UUID;

public class Task {
    private final String name;
    private double leftMinutesToComplete = 100;

    public Task() {
        this.name = UUID.randomUUID().toString();
    }

    public void executeTask(double minutes) {
        leftMinutesToComplete -= minutes;
    }

    public boolean isDone() {
        return leftMinutesToComplete <= 0;
    }

    @Override
    public String toString() {
        return "\nTask{" +
            "name='" + name + '\'' +
            ", leftMinutesToComplete=" + leftMinutesToComplete +
            "}";
    }
}
