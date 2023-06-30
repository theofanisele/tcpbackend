package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class Worker implements Runnable {
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    int taskSize = -1;
    private List<Task> tasks = new ArrayList<>();
    private IntermediateResult intResult = new IntermediateResult();

    public Worker(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        System.out.println("Worker " + this.socket.getInetAddress().getHostName() + " connected to the server.");

        // Initialize the input and output streams.

        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {

        while (true) {
            try {
                if (taskSize == -1) {
                    // Receive the task size
                    taskSize = ois.readInt();
                    System.out.println("Received task size: " + taskSize);
                    if (taskSize == 0) {
                        break; // No tasks to process, exit the loop
                    }
                }

                if (tasks.size() == taskSize) {
                    break; // Received all tasks, exit the loop
                }

                Task task = (Task) ois.readObject();
                System.out.println("Received task: " + task.toString());
                tasks.add(task);
                System.out.println("Total tasks received: " + tasks.size());



            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        processTasks();
        System.out.println("Intermediate results totalDistance: " + intResult.getTotalDistances());
        System.out.println("Intermediate results totalTime: " + intResult.getTotalTimes());
        System.out.println("Intermediate results totalElevationGain: " + intResult.getTotalElevationGains());
        sendIntermediateResultToServer();
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