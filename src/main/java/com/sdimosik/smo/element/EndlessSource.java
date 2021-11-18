package com.sdimosik.smo.element;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class EndlessSource {
    public final List<Generator> generators;
    public final Queue<Task> taskQueue;
    private long countGeneratedTasks;

    public static class Generator {
        private final Random random;
        private final double avg;
        private final double dev;

        private int countGenerateTask = 0;
        private int countFailedTask = 0;
        private double bufferTime = 0;
        private double applianceTime = 0;

        public final int numSource;

        public Generator(double dev, double avg, int numSource) {
            this.avg = avg;
            this.dev = dev;
            this.numSource = numSource;
            this.random = new Random();
        }

        public Task createTask(double currentTime) {
            double nextTime = currentTime + nextTime();
            countGenerateTask++;
            return new Task(numSource, nextTime, 100);
        }

        private double nextTime() {
            return random.nextGaussian() * dev + avg;
        }

        public void addCountFailedTask(int count) {
            countFailedTask += count;
        }

        public void addBufferTime(double time) {
            bufferTime += time;
        }

        public void addAppliance(double time) {
            applianceTime += time;
        }
    }

    public EndlessSource(int capacity, double dev, double avg, int startTime) {
        this.countGeneratedTasks = 0;

        this.generators = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            this.generators.add(new Generator(dev, avg, i));
        }

        this.taskQueue = new PriorityQueue<>();
        for (Generator generator : this.generators) {
            this.taskQueue.add(generator.createTask(startTime));
            countGeneratedTasks++;
        }
    }

    public List<Integer> countGeneratedTask4Source() {
        List<Integer> list = new LinkedList<>();
        for (Generator generator : generators) {
            list.add(generator.countGenerateTask);
        }
        return list;
    }

    public Integer countTasks() {
        int sum = 0;
        for (Generator generator : generators) {
            sum += generator.countGenerateTask;
        }
        return sum;
    }

    public List<Double> probabilityOfFailure4Source() {
        List<Double> list = new LinkedList<>();
        for (Generator generator : generators) {
            int a = generator.countGenerateTask;
            int b = generator.countFailedTask;
            list.add((double) (b) / (double) (a));
        }
        return list;
    }

    public Double probOfFailure() {
        double prob = 0;
        for (Generator generator : generators) {
            int a = generator.countGenerateTask;
            int b = generator.countFailedTask;
            prob += (double) (b) / (double) (a);
        }
        prob /= generators.size();
        return prob;
    }

    public List<Double> avgBufferTime() {
        List<Double> list = new LinkedList<>();
        for (Generator generator : generators) {
            double count = generator.countGenerateTask;
            double timeBuffer = generator.bufferTime;
            list.add(timeBuffer / count);
        }
        return list;
    }

    public List<Double> avgApplianceTime() {
        List<Double> list = new LinkedList<>();
        for (Generator generator : generators) {
            double count = generator.countGenerateTask;
            double timeaAppliance = generator.applianceTime;
            list.add(timeaAppliance / count);
        }
        return list;
    }

    public Task takeAndRegenerateTask(long barrier) {
        Task task = taskQueue.poll();
        if (task == null || countGeneratedTasks >= barrier) return task;

        this.taskQueue.add(generators.get(task.numSource).createTask(task.START_TIME));
        countGeneratedTasks++;
        return task;
    }

    public boolean queueTaskIsEmpty() {
        return taskQueue.isEmpty();
    }

    public boolean allowedGenerate(long barrier) {
        return !queueTaskIsEmpty() || countGeneratedTasks < barrier;
    }
}
