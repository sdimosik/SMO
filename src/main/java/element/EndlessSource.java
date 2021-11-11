package element;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class EndlessSource {

    public final List<Generator> generators;

    public static class Generator {
        private final Random random;
        private final double avg;
        private final double dev;
        private int waitTime = 0;
        private int countGenerateTask = 0;
        private int countFailedTask = 0;
        private int sumExecuteTimeTask = 0;
        private int sumWaitTimeGenerate = 0;

        public final int numSource;

        public Generator(double dev, double avg, int numSource) {
            this.avg = avg;
            this.dev = dev;
            this.numSource = numSource;

            this.random = new Random();
        }

        public Task updateWaitTime() {
            if (isReady()) {
                waitTime = nextTime();
                sumWaitTimeGenerate += waitTime;
                countGenerateTask++;
                return new Task(numSource);
            }
            waitTime--;
            return null;
        }

        public int getCountGenerateTask() {
            return countGenerateTask;
        }

        public int getCountFailedTask() {
            return countFailedTask;
        }

        public void addCountFailedTask(int count) {
            countFailedTask += count;
        }

        public void addSumExecuteTimeTask(int time) {
            sumExecuteTimeTask += time;
        }

        public int getSumExecuteTimeTask() {
            return sumExecuteTimeTask;
        }

        public int getSumWaitTimeGenerate() {
            return sumWaitTimeGenerate;
        }

        private boolean isReady() {
            return waitTime <= 0;
        }

        private int nextTime() {
            return (int) (random.nextGaussian() * dev + avg);
        }
    }

    public EndlessSource(int capacity, double dev, double avg) {
        generators = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            generators.add(new Generator(dev, avg, i));
        }
    }

    public List<Task> updateWaitTime(int start) {
        List<Task> list = new LinkedList<>();
        for (Generator generator : generators) {
            Task task = generator.updateWaitTime();
            if (task != null) {
                task.setStart(start);
                list.add(task);
            }
        }
        return list;
    }

    public List<Integer> countGeneratedTask4Source() {
        List<Integer> list = new LinkedList<>();
        for (Generator generator : generators) {
            list.add(generator.getCountGenerateTask());
        }
        return list;
    }

    public Integer countTasks() {
        int sum = 0;
        for (Generator generator : generators) {
            sum += generator.getCountGenerateTask();
        }
        return sum;
    }

    public List<Double> probabilityOfFailure4Source() {
        List<Double> list = new LinkedList<>();
        for (Generator generator : generators) {
            int a = generator.getCountGenerateTask();
            int b = generator.getCountFailedTask();
            list.add((double) (b) / (double) (a));
        }
        return list;
    }

    public Double probOfFailure() {
        double prob = 0;
        for (Generator generator : generators) {
            int a = generator.getCountGenerateTask();
            int b = generator.getCountFailedTask();
            prob += (double) (b) / (double) (a);
        }
        prob /= generators.size();
        return prob;
    }

    public List<Double> avgTimeOfExistTask4Source() {
        List<Double> list = new LinkedList<>();
        for (Generator generator : generators) {
            double count = generator.getCountGenerateTask();
            double timeExecute = generator.getSumExecuteTimeTask();
            double timeWait = generator.getSumWaitTimeGenerate();

            list.add((timeExecute + timeWait) / count);
        }
        return list;
    }
}
