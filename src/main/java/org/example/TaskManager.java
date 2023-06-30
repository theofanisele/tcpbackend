package org.example;

import java.util.*;

public class TaskManager {
    public final Map<String, Queue<Task>> tasksMap = new HashMap<>();

    public synchronized void addTask(String userName, List<Wpt> wpts) {
        Task task = new Task(userName, wpts);
        tasksMap.putIfAbsent(userName, new LinkedList<>());
        tasksMap.get(userName).add(task);
    }

    public synchronized List<Task> getAllTasks(String userName) {
        Queue<Task> userTasks = tasksMap.get(userName);
        if (userTasks == null || userTasks.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<Task> allTasks = new ArrayList<>(userTasks);
            userTasks.clear();
            return allTasks;
        }
    }
    public List<Task> getTasks(String userName) {
        List<Task> tasks = new ArrayList<>();
        Queue<Task> userTasks = tasksMap.get(userName);

        if (userTasks != null) {
            for (Task task : userTasks) {
                tasks.add(task);
            }

        }

        return tasks;
    }


    public synchronized int size() {
        return tasksMap.values().stream().mapToInt(Queue::size).sum();
    }
}
