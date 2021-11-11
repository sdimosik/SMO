import element.Appliances;
import element.Buffer;
import element.EndlessSource;
import element.Report;
import element.Task;

import java.util.LinkedList;
import java.util.List;

public class App {

    // 0 - step
    // 1 - report
    // 2 - ultimate
    private static final int MODE = 1;

    private static final int INPUT_COUNT = 3;
    private static final int BUFFER_CAPACITY = 3;
    private static final int APPLIANCES_CAPACITY = 4;
    private static final long BARRIER_TASK_COUNT = 100;

    private static EndlessSource input;
    private static Buffer buffer;
    private static Appliances appliances;
    private static int currentTime;

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
        input = new EndlessSource(INPUT_COUNT, 2.5, 6);
        buffer = new Buffer(BUFFER_CAPACITY);
        appliances = new Appliances(APPLIANCES_CAPACITY);
        currentTime = 0;
    }

    public static void main(String[] args) {

        showSettings();


        while (condition()) {
            fill();
            while (isTasksExist()) {
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
            }

            break;
        }
    }

    private static boolean condition() {
        return true;
    }

    private static void bufferingAndFailingDiscipline(List<Task> newTasks, Buffer buffer) {
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
    }

    private static boolean isTasksExist() {
        return allowedGenerate() || buffer.isNotEmpty() || appliances.isNotEmpty();
    }

    private static boolean allowedGenerate() {
        return input.countTasks() < BARRIER_TASK_COUNT;
    }
}
