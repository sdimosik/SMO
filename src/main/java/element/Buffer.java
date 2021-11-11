package element;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class Buffer {

    private final Task[] data;
    private final int capacity;
    private int size;

    private final Queue<Integer> queue;

    private int writeIdx;
    private int readIdx;

    public Buffer(int capacity) {
        this.capacity = capacity;
        this.data = new Task[capacity];
        Arrays.fill(data, null);
        this.queue = new LinkedList<>();
        this.readIdx = 0;
        this.writeIdx = -1;
        this.size = 0;
    }

    // return task failure
    public Task offer(Task task, int time) {
        if (isFull()) {
            return deleteOldAndPutNew(task, time);
        }

        writeIdx++;
        skipIdx(writeIdx, true);

        data[idx(writeIdx)] = task;
        queue.offer(idx(writeIdx));
        size++;

        return null;
    }

    private void skipIdx(final int start, boolean skipFullIdx) {
        boolean finded = false;

        for (int i = idx(start); i < capacity; i++) {
            if (skipFullIdx == (data[i] == null)) {
                finded = true;
                writeIdx = i;
                break;
            }
        }
        if (finded) return;

        for (int i = 0; i < idx(start); i++) {
            if (skipFullIdx == (data[i] == null)) {
                writeIdx = i;
                break;
            }
        }
    }

    private Task deleteOldAndPutNew(Task task, int time) {

        int oldIdx;
        try {
            oldIdx = queue.poll();
        } catch (NullPointerException e) {
            // It should never be
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }

        Task failureTask = data[oldIdx];
        failureTask.setEnd(time);
        data[oldIdx] = task;
        queue.offer(oldIdx);
        return failureTask;
    }

    public Task poll() {
        if (isEmpty()) {
            return null;
        }
        skipIdx(idx(readIdx), false);
        Task nextValue = data[idx(readIdx)];
        data[idx(readIdx)] = null;
        readIdx++;
        size--;
        return nextValue;
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

    public int idx(int idx) {
        return idx % capacity;
    }

    @Override
    public String toString() {
        return "Buffer{" +
            "data=" + Arrays.toString(data) +
            '}';
    }
}
