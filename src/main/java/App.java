import element.Appliances;
import element.Buffer;
import element.EndlessSource;
import element.Task;

import java.util.LinkedList;
import java.util.List;

public class App {

    private static final int INPUT_COUNT = 3;
    private static final int BUFFER_CAPACITY = 3;
    private static final int APPLIANCES_CAPACITY = 4;
    private static final long BARRIER_TASK_COUNT = 100;

    private static EndlessSource input;
    private static Buffer buffer;
    private static Appliances appliances;
    private static double currentTime = 0;

    private static void fill() {
        input = new EndlessSource(INPUT_COUNT, 2.5, 6);
        buffer = new Buffer(BUFFER_CAPACITY);
        appliances = new Appliances(APPLIANCES_CAPACITY);
    }

    public static void main(String[] args) {

        fill();

        while (isTasksExist()) {
            // Stage 0 - Дискретизация времени поминутно
            currentTime++;
            System.out.println("--------------- " + currentTime);

            // Step 1 - генерация тасков
            List<Task> newTasks = new LinkedList<>();
            if (allowedGenerate()) {
                input.updateWaitTime();
                System.out.println("");
                System.out.println("newTasks");
                System.out.println(newTasks);
            }

            // Step 2 - дисциплина буферизации и отказа
            bufferingAndFailingDiscipline(newTasks, buffer);

            // Step 3 - дисциплина постановки на обслуживание и вывод завершённых задач
            servicingDiscipline(buffer, appliances);
        }
    }

    private static void bufferingAndFailingDiscipline(List<Task> newTasks, Buffer buffer) {
        List<Task> failures = new LinkedList<>();
        for (Task task : newTasks) {
            Task failureTask = buffer.offer(task);
            if (failureTask != null) {
                failures.add(failureTask);
            }
        }

        System.out.println("");
        System.out.println(buffer);

        // Таски, которые отлетели
        System.out.println("");
        System.out.println("failures");
        System.out.println(failures);
    }

    private static void servicingDiscipline(Buffer buffer, Appliances appliances) {
        appliances.updateTimeInfo();
        List<Task> completeTasks = appliances.getCompleteTasksAndCleanUp();

        while (appliances.isNotFull() && buffer.isNotEmpty()) {
            Task task = buffer.poll();
            appliances.offer(task);
        }
        appliances.updateTimeInfo();

        System.out.println("");
        System.out.println(appliances);
        System.out.println("");
        System.out.println("completeTasks");
        System.out.println(completeTasks);
    }

    private static boolean isTasksExist() {
        return allowedGenerate() || buffer.isNotEmpty() || appliances.isNotEmpty();
    }

    private static boolean allowedGenerate() {
        return currentTime <= BARRIER_TASK_COUNT;
    }
}
