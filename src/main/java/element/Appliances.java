package element;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Appliances {

    private final Task[] appliance;
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
    }

    // return task failure
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

    public void updateTimeInfo() {
        for (int i = 0; i < capacity; i++) {
            if (appliance[i] != null) {
                appliance[i].executeTask(1);
            }
        }
    }

    public List<Task> getCompleteTasksAndCleanUp() {
        List<Task> list = new LinkedList<>();
        for (int i = 0; i < capacity; i++) {
            if (appliance[i] != null && appliance[i].isDone()) {
                list.add(appliance[i]);
                remove(i);
            }
        }
        return list;
    }

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
}
