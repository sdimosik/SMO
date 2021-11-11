package element;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class EndlessSource {

    private final List<Generator> generators;

    private static class Generator {
        private final Random random;
        private final double avg;
        private final double dev;
        private long waitTime = 0;
        private int countGenerateTask = 0;

        public Generator(double dev, double avg) {
            this.avg = avg;
            this.dev = dev;

            this.random = new Random();
        }

        public Task updateWaitTime() {
            if (isReady()) {
                waitTime = nextTime();
                countGenerateTask++;
                return new Task();
            }
            waitTime--;
            return null;
        }

        private boolean isReady() {
            return waitTime <= 0;
        }

        private long nextTime() {
            return (long) (random.nextGaussian() * dev + avg);
        }
    }

    public EndlessSource(int capacity, double dev, double avg) {
        generators = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            generators.add(new Generator(dev, avg));
        }
    }

    public List<Task> updateWaitTime() {
        List<Task> list = new LinkedList<>();
        for (Generator generator : generators) {
            Task task = generator.updateWaitTime();
            if (task != null) {
                list.add(task);
            }
        }
        return list;
    }
}
