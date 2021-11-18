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
        infoForUI = new InfoForUI();
    }

    public static void main(String[] args) {
        showSettings();
        fill();

        int iter = 0;
        while (isTasksExist()) {
            Task newTask = input.takeAndRegenerateTask(BARRIER_TASK_COUNT);

            if (canShowStep()) {
                System.out.println("\n-----------" + iter + "-----------");
                System.out.print(String.format("%,.3f  ", currentTime)
                    + String.format("%d  ", input.taskQueue.size())
                    + String.format("%d  ", buffer.size())
                    + String.format("%d  ", appliances.size())
                    + "Взяли задачу: ");
            }

            if (newTask != null) {
                if (canShowStep()) System.out.println(newTask.name + "  " + newTask.START_TIME);
                updateData(newTask, State.START, currentTime);
                currentTime = newTask.START_TIME;

                updateData(newTask, State.BUFFER, currentTime);
                Task failTask = buffer.offer(newTask, currentTime);

                if (canShowStep()) {
                    System.out.println(String.format("%,.3f  ", currentTime)
                        + String.format("%d  ", input.taskQueue.size())
                        + String.format("%d  ", buffer.size())
                        + String.format("%d  ", appliances.size())
                        + "Добавляем В БУФЕР задачу: " + newTask.name);
                }
                if (failTask != null) {
                    if (canShowStep()) {
                        System.out.println(String.format("%,.3f  ", currentTime)
                            + String.format("%d  ", input.taskQueue.size())
                            + String.format("%d  ", buffer.size())
                            + String.format("%d  ", appliances.size())
                            + "Сработала дисциплина отказа, задача: " + failTask.name);
                    }
                    updateData(failTask, State.FAIL, currentTime);

                    input.generators.get(failTask.numSource).addCountFailedTask(1);
                }
            } else {
                if (canShowStep()) {
                    System.out.println("не существует");
                }
            }

            if (appliances.isNotFull() && buffer.isNotEmpty()) {
                Task afterBufferTask = buffer.poll();
                updateData(afterBufferTask, State.APPLIANCE, currentTime);
                appliances.offer(afterBufferTask);
                if (canShowStep()) {
                    System.out.println(String.format("%,.3f  ", currentTime)
                        + String.format("%d  ", input.taskQueue.size())
                        + String.format("%d  ", buffer.size())
                        + String.format("%d  ", appliances.size())
                        + "Берём ИЗ БУФЕРА и добавляем В ПРИБОРЫ задачу: " + afterBufferTask.name);
                }
                input.generators
                    .get(afterBufferTask.numSource)
                    .addBufferTime(afterBufferTask.getStartExecute() - afterBufferTask.START_TIME);
            }

            double completionTime = appliances.getCompletionsTimeOfTask(currentTime, input);

            if (currentTime != completionTime) {
                Task completedTask = appliances.getCompleteTask(currentTime, input);
                currentTime = completionTime;
                updateData(completedTask, State.DONE, completionTime);
                if (canShowStep()) {
                    System.out.println(String.format("%,.3f  ", completionTime)
                        + String.format("%d  ", input.taskQueue.size())
                        + String.format("%d  ", buffer.size())
                        + String.format("%d  ", appliances.size())
                        + "Удаление ИЗ ПРИБОРА выполненной задачи: " + completedTask.name);
                }
                input.generators
                    .get(completedTask.numSource)
                    .addAppliance(completedTask.getEndTime() - completedTask.getStartExecute());
            }

            iter++;
        }

        System.out.println("\n\nFinish");

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
        infoForUI.add(newTask);
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
