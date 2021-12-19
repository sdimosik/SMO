package com.sdimosik.smo;

import com.sdimosik.smo.element.Appliances;
import com.sdimosik.smo.element.Buffer;
import com.sdimosik.smo.element.EndlessSource;
import com.sdimosik.smo.element.Task;
import com.sdimosik.smo.ui.MainUI;

import static com.sdimosik.smo.Utils.State;

public class App {

    // 0 - step
    // 1 - report
    // 2 - ultimate
    private final MainUI mainUI;

    private final int MODE;

    private final long BARRIER_TASK_COUNT;
    private final int INPUT_COUNT;
    private final int BUFFER_CAPACITY;
    private final int APPLIANCES_CAPACITY;
    private final double LAMBDA;
    private final double MEAN;
    private final double VARIANCE;

    //private static final Scanner IN = new Scanner(System.in);

    private EndlessSource input;
    private Buffer buffer;
    private Appliances appliances;
    public double currentTime;
    //public InfoForUI infoForUI;

    public App(int mode,
               long barrier_task_count,
               int input_count,
               int buffer_capacity,
               int appliances_capacity,
               double lambda,
               double mean,
               double variance,
               MainUI mainUI
    ) {
        MODE = mode;
        BARRIER_TASK_COUNT = barrier_task_count;
        INPUT_COUNT = input_count;
        BUFFER_CAPACITY = buffer_capacity;
        APPLIANCES_CAPACITY = appliances_capacity;
        LAMBDA = lambda;
        MEAN = mean;
        VARIANCE = variance;
        this.mainUI = mainUI;
    }

    private void showSettings() {
        System.out.printf("\nINPUT_COUNT %d", INPUT_COUNT);
        System.out.printf("\nBUFFER_CAPACITY %d", BUFFER_CAPACITY);
        System.out.printf("\nAPPLIANCES_CAPACITY %d", APPLIANCES_CAPACITY);
        System.out.printf("\nBARRIER_TASK_COUNT %d\n\n", BARRIER_TASK_COUNT);
    }

    private boolean canShowReport() {
        return MODE != 0;
    }

    private boolean canShowStep() {
        return MODE != 1;
    }

    private void fill() {
        input = new EndlessSource(INPUT_COUNT, LAMBDA, MEAN, VARIANCE, 0);
        buffer = new Buffer(BUFFER_CAPACITY);
        appliances = new Appliances(input, APPLIANCES_CAPACITY);
        currentTime = 0.0;
        //infoForUI = new InfoForUI(INPUT_COUNT, BUFFER_CAPACITY, APPLIANCES_CAPACITY);
    }

    public void prepare() {
        fill();
    }

    public Task step1() {
        Task newTask = input.takeAndRegenerateTask(BARRIER_TASK_COUNT);
        if (newTask != null) {
            updateData(newTask, State.START, currentTime);
            currentTime = newTask.startTime;
        }
        return newTask;
    }

    public Task step2(Task newTask) {
        if (newTask != null) {
            Task failTask = buffer.offer(newTask, currentTime);
            if (failTask != null) {
                input.generators.get(failTask.numSource).addCountFailedTask(1);
                updateData(failTask, State.FAIL, currentTime);
            }
            return failTask;
        }
        return null;
    }

    public boolean step3(Task newTask) {
        if (newTask != null) {
            updateData(newTask, State.BUFFER, currentTime);
            return true;
        }
        return false;
    }

    public boolean step4() {
        if (appliances.isNotFull() && buffer.isNotEmpty()) {
            Task afterBufferTask = buffer.poll();
            appliances.offer(afterBufferTask);
            updateData(afterBufferTask, State.APPLIANCE, currentTime);
            input.generators
                .get(afterBufferTask.numSource)
                .addBufferTime(afterBufferTask.getStartAppliancesTime() - afterBufferTask.startTime);
            return true;
        }
        return false;
    }

    public boolean step5() {
        double completionTime = appliances.getCompletionsTimeOfTask(currentTime, input);

        if (currentTime != completionTime) {
            Task completedTask = appliances.getCompleteTask(currentTime, input);
            currentTime = completionTime;
            updateData(completedTask, State.DONE, completionTime);
            input.generators
                .get(completedTask.numSource)
                .addAppliance(completedTask.getEndTime() - completedTask.getStartAppliancesTime());
            return true;
        }
        return false;
    }

    public void calc() {
        showSettings();
        fill();
        //if (canShowStep()) infoForUI.startPrint();

        while (isTasksExist()) {
            // -------------- 1
            Task newTask = input.takeAndRegenerateTask(BARRIER_TASK_COUNT);

            if (newTask != null) {
                updateData(newTask, State.START, currentTime);
                currentTime = newTask.startTime;

                // -------------- 2
                Task failTask = buffer.offer(newTask, currentTime);
                if (failTask != null) {
                    input.generators.get(failTask.numSource).addCountFailedTask(1);
                    updateData(failTask, State.FAIL, currentTime);
                }

                // -------------- 3
                updateData(newTask, State.BUFFER, currentTime);
            }

            // -------------- 4
            if (appliances.isNotFull() && buffer.isNotEmpty()) {
                Task afterBufferTask = buffer.poll();
                appliances.offer(afterBufferTask);
                updateData(afterBufferTask, State.APPLIANCE, currentTime);
                input.generators
                    .get(afterBufferTask.numSource)
                    .addBufferTime(afterBufferTask.getStartAppliancesTime() - afterBufferTask.startTime);
            }

            // -------------- 5
            double completionTime = appliances.getCompletionsTimeOfTask(currentTime, input);

            if (currentTime != completionTime) {
                Task completedTask = appliances.getCompleteTask(currentTime, input);
                currentTime = completionTime;
                updateData(completedTask, State.DONE, completionTime);
                input.generators
                    .get(completedTask.numSource)
                    .addAppliance(completedTask.getEndTime() - completedTask.getStartAppliancesTime());
            }
        }

        if (canShowReport()) {
            Report report = new Report(
                input.countGeneratedTask4Source(),
                input.probabilityOfFailure4Source(),
                input.avgBufferTime(),
                input.avgApplianceTime(),
                appliances.kUsedAppliance(currentTime),
                input.bufferDispersion(),
                input.applianceDispersion()
            );
            mainUI.updateReport(report);
        }
    }

    private void updateData(Task newTask, State stage, double time) {
        newTask.updateState(stage, time);
        if (canShowStep()) {
            /*if (MODE == 0) {
                //System.out.println();
                //IN.nextLine();
                waiting();
            } else {
                System.out.println();
            }*/
            mainUI.update(newTask);
            //infoForUI.update(newTask);
        }
    }

    private void waiting() {
        while (!mainUI.isPressedNext) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
        mainUI.isPressedNext = false;
    }

    public boolean isTasksExist() {
        return input.allowedGenerate(BARRIER_TASK_COUNT) || buffer.isNotEmpty() || appliances.isNotEmpty();
    }
}
