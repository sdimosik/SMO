package com.sdimosik.smo;

import com.sdimosik.smo.element.Appliances;
import com.sdimosik.smo.element.Buffer;
import com.sdimosik.smo.element.EndlessSource;
import com.sdimosik.smo.element.InfoForUI;
import com.sdimosik.smo.element.Report;
import com.sdimosik.smo.element.Task;

import java.util.Scanner;

import static com.sdimosik.smo.Utils.State;

public class App {

    // 0 - step
    // 1 - report
    // 2 - ultimate
    private static final int MODE = 2;

    private static final int INPUT_COUNT = 3;
    private static final int BUFFER_CAPACITY = 3;
    private static final int APPLIANCES_CAPACITY = 4;
    private static final long BARRIER_TASK_COUNT = 10;
    private static final Scanner IN = new Scanner(System.in);

    private static EndlessSource input;
    private static Buffer buffer;
    private static Appliances appliances;
    public static double currentTime;
    public static InfoForUI infoForUI;

    private static void showSettings() {
        System.out.printf("\nINPUT_COUNT %d", INPUT_COUNT);
        System.out.printf("\nBUFFER_CAPACITY %d", BUFFER_CAPACITY);
        System.out.printf("\nAPPLIANCES_CAPACITY %d", APPLIANCES_CAPACITY);
        System.out.printf("\nBARRIER_TASK_COUNT %d\n\n", BARRIER_TASK_COUNT);
    }

    private static boolean canShowReport() {
        return MODE != 0;
    }

    private static boolean canShowStep() {
        return MODE != 1;
    }

    private static void fill() {
        input = new EndlessSource(INPUT_COUNT, 4.8, 20, 0);
        buffer = new Buffer(BUFFER_CAPACITY);
        appliances = new Appliances(APPLIANCES_CAPACITY);
        currentTime = 0.0;
        infoForUI = new InfoForUI(INPUT_COUNT, BUFFER_CAPACITY, APPLIANCES_CAPACITY);
    }

    public static void main(String[] args) {
        showSettings();
        fill();
        infoForUI.startPrint();

        while (isTasksExist()) {
            Task newTask = input.takeAndRegenerateTask(BARRIER_TASK_COUNT);

            if (newTask != null) {
                updateData(newTask, State.START, currentTime);
                currentTime = newTask.startTime;

                Task failTask = buffer.offer(newTask, currentTime);
                updateData(newTask, State.BUFFER, currentTime);

                if (failTask != null) {
                    input.generators.get(failTask.numSource).addCountFailedTask(1);
                    updateData(failTask, State.FAIL, currentTime);
                }
            }

            if (appliances.isNotFull() && buffer.isNotEmpty()) {
                Task afterBufferTask = buffer.poll();
                appliances.offer(afterBufferTask);
                updateData(afterBufferTask, State.APPLIANCE, currentTime);
                input.generators
                    .get(afterBufferTask.numSource)
                    .addBufferTime(afterBufferTask.getStartAppliancesTime() - afterBufferTask.startTime);
            }

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
                appliances.kUsedAppliance(currentTime)
            );
            report.print();
        }
    }

    private static void updateData(Task newTask, State stage, double time) {
        newTask.updateState(stage, time);
        infoForUI.update(newTask);
        if (canShowStep() && MODE == 0) {
            IN.nextLine();
        }
    }

    /*    private static void bufferingAndFailingDiscipline(List<Task> newTasks, Buffer buffer) {
        List<Task> failures = new LinkedList<>();
        for (Task task : newTasks) {
            Task failureTask = buffer.offer(task, currentTime);
            if (failureTask != null) {
                failures.add(failureTask);
                input.generators.get(failureTask.numSource).addCountFailedTask(1);
                input.generators.get(failureTask.numSource).addSumExecuteTimeTask(failureTask.getTime());
            }
        }

        if (canShowStep()) {
            System.out.printf("\n\n%s", buffer);
            // Таски, которые отлетели
            System.out.printf("\n\nfailures\n%s", failures);
        }
    }

    private static void servicingDiscipline(Buffer buffer, Appliances appliances) {
        appliances.updateTimeInfo();
        List<Task> completeTasks = appliances.getCompleteTasksAndCleanUp(currentTime);
        completeTasks.forEach(task -> input.generators.get(task.numSource).addSumExecuteTimeTask(task.getTime()));

        while (appliances.isNotFull() && buffer.isNotEmpty()) {
            Task task = buffer.poll();
            appliances.offer(task);
        }

        if (canShowStep()) {
            System.out.printf("\n\nappliances%s", appliances);
            System.out.printf("\n\ncompleteTasks\n%s\n\n", completeTasks);
        }
    }*/

    private static boolean isTasksExist() {
        return input.allowedGenerate(BARRIER_TASK_COUNT) || buffer.isNotEmpty() || appliances.isNotEmpty();
    }
}
