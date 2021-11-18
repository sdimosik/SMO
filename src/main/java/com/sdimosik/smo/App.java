package com.sdimosik.smo;

import com.sdimosik.smo.element.Appliances;
import com.sdimosik.smo.element.Buffer;
import com.sdimosik.smo.element.EndlessSource;
import com.sdimosik.smo.element.InfoForUI;
import com.sdimosik.smo.element.Task;

import java.util.Scanner;

import static com.sdimosik.smo.Utils.*;

public class App {

    // 0 - step
    // 1 - report
    // 2 - ultimate
    private static final int MODE = 2;

    private static final int INPUT_COUNT = 3;
    private static final int BUFFER_CAPACITY = 3;
    private static final int APPLIANCES_CAPACITY = 4;
    private static final long BARRIER_TASK_COUNT = 100;
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
            System.out.println("\n-----------" + iter + "-----------");
            Task newTask = input.takeAndRegenerateTask(BARRIER_TASK_COUNT);
            System.out.print(String.format("%,.3f  ", currentTime)
                + String.format("%d  ", input.taskQueue.size())
                + String.format("%d  ", buffer.size())
                + String.format("%d  ", appliances.size())
                + "Взяли задачу: ");

            if (newTask != null) {
                System.out.println(newTask.name + "  " + newTask.start);
                updateData(newTask, State.START);
                currentTime = newTask.start;

                updateData(newTask, State.BUFFER);
                Task failTask = buffer.offer(newTask, currentTime);
                System.out.println(String.format("%,.3f  ", currentTime)
                    + String.format("%d  ", input.taskQueue.size())
                    + String.format("%d  ", buffer.size())
                    + String.format("%d  ", appliances.size())
                    + "Добавляем В БУФЕР задачу: " + newTask.name);
                if (failTask != null) {
                    System.out.println(String.format("%,.3f  ", currentTime)
                        + String.format("%d  ", input.taskQueue.size())
                        + String.format("%d  ", buffer.size())
                        + String.format("%d  ", appliances.size())
                        + "Сработала дисциплина отказа, задача: " + failTask.name);
                    updateData(failTask, State.FAIL);
                }
            } else {
                System.out.println("не существует");
            }


            if (appliances.isNotFull() && buffer.isNotEmpty()) {
                Task afterBufferTask = buffer.poll();
                updateData(afterBufferTask, State.APPLIANCE);
                appliances.offer(afterBufferTask);
                System.out.println(String.format("%,.3f  ", currentTime)
                    + String.format("%d  ", input.taskQueue.size())
                    + String.format("%d  ", buffer.size())
                    + String.format("%d  ", appliances.size())
                    + "Берём ИЗ БУФЕРА и добавляем В ПРИБОРЫ задачу: " + afterBufferTask.name);
            }

            double completionTime = appliances.getCompletionsTimeOfTask(currentTime, input);

            if (currentTime != completionTime) {
                // prev more than current because
                // current is completion time of task
                // prev is olderCurrent

                Task completedTask = appliances.getCompleteTask(currentTime, input);
                currentTime = completionTime;
                updateData(completedTask, State.DONE);
                System.out.println(String.format("%,.3f  ", completionTime)
                    + String.format("%d  ", input.taskQueue.size())
                    + String.format("%d  ", buffer.size())
                    + String.format("%d  ", appliances.size())
                    + "Удаление ИЗ ПРИБОРА выполненной задачи: " + completedTask.name);
            }

            iter++;
        }

        System.out.println("\n\nFinish");

        /*while (isTasksExist()) {
            // Stage 0 - Дискретизация времени поминутно
            currentTime++;
            if (canShowStep()) System.out.println("--------------- " + currentTime);

            // Step 1 - генерация тасков
            List<Task> newTasks = new LinkedList<>();
            if (allowedGenerate()) {
                newTasks = input.updateWaitTime(currentTime);
                if (canShowStep()) {
                    System.out.printf("newTasks\n%s", newTasks);
                }
            }

            // Step 2 - дисциплина буферизации и отказа
            bufferingAndFailingDiscipline(newTasks, buffer);

            // Step 3 - дисциплина постановки на обслуживание и вывод завершённых задач
            servicingDiscipline(buffer, appliances);
        }

        if (canShowReport()) {
            Report report = new Report(
                input.countGeneratedTask4Source(),
                input.probabilityOfFailure4Source(),
                input.avgTimeOfExistTask4Source(),
                appliances.kUsedAppliance(currentTime)
            );
            System.out.printf("\n%s", report);
        }*/
    }

    private static void updateData(Task newTask, State start) {
        newTask.updateState(start, currentTime);
        infoForUI.add(newTask);
        //IN.nextLine();
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
