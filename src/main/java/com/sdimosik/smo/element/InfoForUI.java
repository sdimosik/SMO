package com.sdimosik.smo.element;

import java.util.LinkedList;
import java.util.List;

public class InfoForUI {
    public final List<Task> eventStartTask = new LinkedList<>();
    public final List<Task> eventBufferTask = new LinkedList<>();
    public final List<Task> eventApplianceTask = new LinkedList<>();
    public final List<Task> eventDoneTask = new LinkedList<>();
    public final List<Task> eventFailTask = new LinkedList<>();

    public void add(Task task) {
        switch (task.state) {
            case START:
                eventStartTask.add(task);
                break;
            case BUFFER:
                eventBufferTask.add(task);
                break;
            case APPLIANCE:
                eventApplianceTask.add(task);
                break;
            case DONE:
                eventDoneTask.add(task);
                break;
            case FAIL:
                eventFailTask.add(task);
                break;
        }
    }

    @Override
    public String toString() {
        return "InfoForUI:" +
            "\neventStartTask=" + eventStartTask +
            "\neventBufferTask=" + eventBufferTask +
            "\neventApplianceTask=" + eventApplianceTask +
            "\neventDoneTask=" + eventDoneTask +
            "\neventFailTask=" + eventFailTask +
            "\n---------------------";
    }
}
