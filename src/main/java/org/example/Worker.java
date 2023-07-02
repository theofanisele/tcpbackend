package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class Worker implements Runnable {
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    int taskSize = -1;
    private List<Task> tasks = new ArrayList<>();
    private IntermediateResult intResult = new IntermediateResult();
    private final AtomicBoolean running = new AtomicBoolean(false);




    public Worker(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        System.out.println("Worker " + this.socket.getInetAddress().getHostName() + " connected to the server.");

        // Initialize the input and output streams.

        this.ois = new ObjectInputStream(socket.getInputStream());
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        running.set(true);
    }


    @Override
    public void run() {
        while (true) {
            try {
                // If the worker is not free, then skip this iteration
                if (!running.get()) {
                    Thread.sleep(1000); // Avoid busy-waiting, make the thread sleep for a short duration
                    continue;
                }

                // Mark the worker as busy before starting the task
                running.set(false);

                if (taskSize == -1) {
                    // Receive the task size
                    taskSize = ois.readInt();
                    System.out.println("Received task size: " + taskSize);
                    if (taskSize == 0) {
                        running.set(true); // Make the worker free before starting the next round
                        continue; // No tasks to process for this round, start the next round
                    }
                }

                // Try to receive a task if there is one to receive
                while (tasks.size() < taskSize) {
                    Task task = (Task) ois.readObject();
                    System.out.println("Received task: " + task.toString());
                    tasks.add(task);
                    System.out.println("Total tasks received: " + tasks.size());
                }

                if (tasks.size() == taskSize) {
                    // Process the tasks and send the results back to the server
                    processTasks();
                    System.out.println("Intermediate results totalDistance: " + intResult.getTotalDistances());
                    System.out.println("Intermediate results totalTime: " + intResult.getTotalTimes());
                    System.out.println("Intermediate results totalElevationGain: " + intResult.getTotalElevationGains());
                    sendIntermediateResultToServer();

                    // Prepare for the next round
                    running.set(true); // Make the worker free before starting the next round
                    taskSize = -1; // Reset task size
                    clear(); // Clear tasks
                    resetIntermediateResult();
                    continue; // Start the next round
                }

            } catch (IOException e) {
                running.set(true); // Make the worker free in case of exceptions
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                running.set(true); // Make the worker free in case of exceptions
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                // Handle the exception when the thread is interrupted during sleep
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isFree() {
        return running.get();
    }

    public void markAsFree() {
        running.set(true);
    }

    public void markAsBusy() {
        running.set(false);
    }
    private void resetIntermediateResult() {
        intResult = new IntermediateResult();
    }

    public void processTasks() {
        for (Task task : tasks) {
            List<Wpt> waypoints = task.getWaypoints();

            // Calculate total distance
            double totalDistance = IntStream.range(0, waypoints.size() - 1)
                    .mapToDouble(i -> waypoints.get(i).getLocation().distance(waypoints.get(i + 1).getLocation()))
                    .sum();

            // Calculate total time
            long totalTime = IntStream.range(0, waypoints.size() - 1)
                    .mapToLong(i -> waypoints.get(i + 1).getTime().longValue() - waypoints.get(i).getTime().longValue())
                    .sum();

            // Calculate total elevation gain
            double totalElevationGain = IntStream.range(0, waypoints.size() - 1)
                    .mapToDouble(i -> {
                        double elevationChange = waypoints.get(i + 1).getElevation() - waypoints.get(i).getElevation();
                        return elevationChange > 0 ? elevationChange : 0.0;
                    })
                    .sum();
            intResult.addResult(totalDistance,totalTime,totalElevationGain);

        }
        intResult.setName(tasks.get(0).getUsername());
    }
    public void clear() {
        intResult.getTotalDistances().clear();
        intResult.getTotalTimes().clear();
        intResult.getTotalElevationGains().clear();
        tasks.clear();
    }
    public void sendIntermediateResultToServer() {
        try {
            oos.writeObject(intResult);
            oos.flush();
            // Clear the IntermediateResult
            clear();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            Worker worker = new Worker("localhost", 8001);
            new Thread(worker).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}