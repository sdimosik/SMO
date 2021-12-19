package com.sdimosik.smo.element;

import cern.jet.random.Normal;
import cern.jet.random.Poisson;
import cern.jet.random.engine.DRand;
import cern.jet.random.engine.RandomEngine;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class EndlessSource {
    public static final RandomEngine randomEngine = new DRand();
    public final List<Generator> generators;
    public final Queue<Task> taskQueue;
    private long countGeneratedTasks;

    public static class Generator {
        private final double lambda;
        private final Poisson paus;
        private final Random random = new Random();
        private final Normal normal;
        private int countGenerateTask = 0;
        private int countFailedTask = 0;
        private double bufferTime = 0;
        private List<Double> bufferTimeList = new LinkedList<>();
        private double applianceTime = 0;
        private List<Double> applianceTimeList = new LinkedList<>();

        public final int numSource;

        public Generator(double lambda, Poisson paus, Normal normal, int numSource) {
            this.lambda = lambda;
            this.paus = paus;
            this.normal = normal;
            this.numSource = numSource;
        }

        public Task createTask(double currentTime) {
            double nextTime = currentTime + paus.nextDouble();//(-1 * lambda) * Math.log(random.nextGaussian());
            // random.nextGaussian() * 15 + 100
            Task task = new Task(numSource, countGenerateTask, nextTime, normal.nextDouble());
            countGenerateTask++;
            return task;
        }

        public void addCountFailedTask(int count) {
            countFailedTask += count;
        }

        public void addBufferTime(double time) {
            bufferTimeList.add(time);
            bufferTime += time;
        }

        public void addAppliance(double time) {
            applianceTimeList.add(time);
            applianceTime += time;
        }
    }

    public EndlessSource(int capacity, double lambda, double mean, double variance, int startTime) {
        this.countGeneratedTasks = 0;

        this.generators = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            this.generators.add(new Generator(
                lambda,
                new Poisson(lambda, randomEngine),
                //new Normal(lambda, Math.sqrt(lambda), randomEngine),
                new Normal(mean, variance, randomEngine), i));
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

    public List<Double> probabilityOfFailure4Source() {
        List<Double> list = new LinkedList<>();
        for (Generator generator : generators) {
            int a = generator.countGenerateTask;
            int b = generator.countFailedTask;
            list.add((double) (b) / (double) (a));
        }
        return list;
    }

    public List<Double> avgBufferTime() {
        List<Double> list = new LinkedList<>();
        for (Generator generator : generators) {
            double count = generator.countGenerateTask;
            double inf = 0;
            for (Double timeBuffer : generator.bufferTimeList) {
                double tmp = timeBuffer / count;
                inf += (tmp);
            }
            //double timeBuffer = generator.bufferTime;
            list.add(inf);
        }
        return list;
    }

    // disp = (sum ((Ti - Tср)^2) ) / n
    public List<Double> bufferDispersion() {
        List<Double> avgBufferTime = avgBufferTime();

        List<Double> list = new LinkedList<>();
        for (int i = 0, generatorsSize = generators.size(); i < generatorsSize; i++) {
            Generator generator = generators.get(i);
            double sum = 0;
            for (Double time : generator.bufferTimeList) {
                sum += Math.pow(time - avgBufferTime.get(i), 2);
            }
            list.add(sum / generators.size());
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

    // disp = (sum ((Ti - Tср)^2) ) / n
    public List<Double> applianceDispersion() {
        List<Double> avgApplianceTime = avgApplianceTime();

        List<Double> list = new LinkedList<>();
        for (int i = 0, generatorsSize = generators.size(); i < generatorsSize; i++) {
            Generator generator = generators.get(i);
            double sum = 0;
            for (Double time : generator.applianceTimeList) {
                sum += Math.pow(time - avgApplianceTime.get(i), 2);
            }
            list.add(sum / generators.size());
        }
        return list;
    }

    public Task takeAndRegenerateTask(long barrier) {
        Task task = taskQueue.poll();
        if (task == null || countGeneratedTasks >= barrier) return task;

        this.taskQueue.add(generators.get(task.numSource).createTask(task.startTime));
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
