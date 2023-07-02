package org.example;

import com.google.gson.Gson;
import io.jenetics.jpx.GPX;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class Server {
    private final List<WorkerHandler> workers = new ArrayList<>();
    private final List<WorkerHandler> freeWorkers = new ArrayList<>();
    private final Map<WorkerHandler,Socket> workerUserMap = new HashMap<>();
    private final ServerSocket ws;
    private final ServerSocket cs;
    private final TaskManager tasks;
    private final Object lock = new Object();
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    private Repository repo = new Repository();
    private Map<String, Double> percentDiffs = new HashMap<>();

    public Server() throws IOException {
        this.cs = new ServerSocket(8000);
        this.ws = new ServerSocket(8001);
        this.tasks = new TaskManager();
//        int nThreads = Runtime.getRuntime().availableProcessors();

        for (int i = 0; i < 2; i++) {
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", System.getProperty("java.class.path"), "org.example.Worker");
            pb.redirectOutput(new File("worker" + i + ".log"));
            pb.start();

            Socket workerSocket = new Socket();
            workerSocket =  ws.accept();
            WorkerHandler handler = new WorkerHandler(workerSocket);
            workers.add(handler);
            freeWorkers.add(handler);
            Thread thread = new Thread(handler);
            thread.start();

            System.out.println(thread.getName());
            System.out.println("Worker connected at: " + workerSocket.getInetAddress());
            System.out.println("Available workers: "+freeWorkers.size());

        }
    }
    public Object getLock1() {
        return lock1;
    }

    public void start() throws IOException, ClassNotFoundException, InterruptedException {

        while (true) {

            try {
                System.out.println("Waiting for clients...");

                Socket clientSocket = new Socket();
                clientSocket = cs.accept();

                System.out.println(clientSocket.getInetAddress() + " connected.");
                Thread thread = new Thread(new ClientHandler(clientSocket));
                thread.start();
                System.out.println(thread.isAlive());
                System.out.println(thread.getName());
            } catch (IOException e) {
                System.out.println("ERRORE RE PELLE 2");
                System.out.println("Error accepting client connection: " + e);
                break; // Stop accepting if server socket is closed
            }
        }
    }


    public static void main(String[] args) {
        try {

            Server server = new Server();
            server.start();

        } catch(IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("ERRORE RE PELLE 3");
        }
    }

    private class WorkerHandler implements Runnable{
        private final Socket socket;
        private final ObjectInputStream ois;
        private final ObjectOutputStream oos;
        private final AtomicBoolean running = new AtomicBoolean(false);




        public WorkerHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());

        }

        @Override
        public void run() {
            while(true){

                try {


                        IntermediateResult intResult = new IntermediateResult();
                        intResult = receiveIntermediateResult();


                        Results res = reduce(intResult);
                        System.out.println("worker handler " + res.toString());

                    synchronized (lock) {

                        repo.addResults(res);
                        percentDiffs = PercentDiffCalculator.calculatePercentDiffs(res,repo.getResult("average"));
                        System.out.println(percentDiffs);
//                        System.out.println(repo.getResult(res.getName()));
                        lock.notifyAll();

                        markAsFree();
                        System.out.println("last pont in the app");

                    }
                    } catch(Exception e){
                        System.out.println("ERRORE RE PELLE 4");
                        e.printStackTrace();
                        return;
                    }

            }

        }
        public Results reduce(IntermediateResult intResult){
            String name = intResult.getName();
            double totalDistance = Math.round(intResult.getTotalDistances().stream().mapToDouble(Double::doubleValue).sum() * 100.0) / 100.0;
            long totalTime = intResult.getTotalTimes().stream().mapToLong(Long::longValue).sum();
            double totalTimeInMinutes = totalTime / 60.0;
            double totalElevationGain = Math.round(intResult.getTotalElevationGains().stream().mapToDouble(Double::doubleValue).sum() * 100.0) / 100.0;

            double averageSpeed = 0.00;
            if (totalTime > 0) {
                averageSpeed = (totalDistance / (totalTime / 60.0 / 60.0));  // Convert time to hours
                averageSpeed = Math.round(averageSpeed * 100.0) / 100.0;
            }

            double time = convertToSeconds(totalTimeInMinutes);

            return new Results(name, totalDistance, totalElevationGain, time , averageSpeed);
        }
        public Double convertToSeconds(double x){

            int minutes = (int) x;
            double fractionalMinutes = x - minutes;

            double seconds = (fractionalMinutes * 60)/100;
            double time = minutes+seconds;
            return time;

        }

        public void sendTask(Task task) throws IOException {
            oos.writeObject(task);
            oos.flush();
        }
        public void sendTaskSize(int size) throws IOException {
            oos.writeInt(size);
            oos.flush();
        }



//        private void markAsFree() {
//            synchronized (lock) {
//                Socket userName = workerUserMap.remove(this);
//                freeWorkers.add(this);
////                WorkerHandler userName = workerUserMap.remove(user); // Use the Connector as the key
////                freeWorkers.add(this);
//                System.out.println("Worker is now free");
//                lock.notify();
//
//                return;
//            }
//        }

        private void markAsFree() {
            synchronized (lock1) {
                Socket userName = workerUserMap.remove(this);
                freeWorkers.add(this);
                System.out.println("Worker is now free");
                lock1.notifyAll();
            }
        }
        public IntermediateResult receiveIntermediateResult() throws IOException, ClassNotFoundException {
            return (IntermediateResult) ois.readObject();
        }


    }
    private class ClientHandler implements Runnable {
        private final DataInputStream dis ;
        private final Socket clientSocket;
        private String userName;
        private WorkerHandler worker;
        private final PrintWriter oos;
        private final AtomicBoolean running = new AtomicBoolean(false);


        public ClientHandler(Socket clientSocket) throws IOException {

            this.clientSocket = clientSocket;
            this.dis = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            this.oos = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
        }

        @Override
        public void run() {
            try {

                    byte[] data = readData(dis);

                    GPX gpx = byteArrayToGPX(data);
                    GpxObject receivedData = new GpxObject(gpx);

                    System.out.println("Received GPX object from: " + receivedData.getUserName());
                    System.out.println(receivedData);
                    userName = receivedData.getUserName();
//                    usernames.add(userName);

                    worker = assignWorkerToUser();


                    System.out.println(worker);
                    System.out.println("handle client: ");

                    createTasks(userName, receivedData.getWaypoints());
                    System.out.println("tasks size before sending to the worker: " + tasks.size());

                    System.out.println("All tasks have been created for user: " + userName);

                    sendTasksToWorker(worker, userName);

                    System.out.println("tasks size after sending to the worker: " + tasks.size());// Assuming that this method will retrieve the next task for the user.




                synchronized (lock) {

                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            System.out.println("ERRORE RE PELLE 5");
                            throw new RuntimeException(e);
                        }

                }


                System.out.println("Processing completed for user: " + userName);

//                worker.markAsFree();
//                System.out.println(repo.getResult(userName).toString());
                sendData(repo.getResult(userName),repo.getResult("averages"), percentDiffs);

                System.out.println("Free Workers: " + freeWorkers.size());
                clientSocket.close();
//                running.set(false);

                    //stop();



            } catch (Exception e) {

                System.out.println("ERRORE RE PELLE 1");

                e.printStackTrace();
                System.out.println(e);
                System.out.println(e.getMessage());
                return;
            }

        }



        private byte[] readData(DataInputStream dis) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024]; // size depends on the expected data
            int bytesRead;
            while ((bytesRead = dis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }


        private void createTasks(String userName, List<Wpt> waypoints) {
            for (int i = 0; i < waypoints.size() - 1; i++) {
                List<Wpt> wpts = new ArrayList<>();
                wpts.add(waypoints.get(i));
                wpts.add(waypoints.get(i + 1));
                tasks.addTask(userName, wpts);

            }
//            System.out.println(tasks.getTasks(userName).toString());
        }


        public GPX byteArrayToGPX(byte[] data) throws IOException {
            //Create a temporary file
            File tempFile = File.createTempFile("temp_gpx", ".gpx");
            tempFile.deleteOnExit(); // This ensures the file is deleted when the JVM exits

            //Write the byte array data into the temporary file
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(data);
            }

            //Read the GPX data from the temporary file
            GPX gpx = GPX.read(tempFile.toPath());

            return gpx;
        }
        private void sendTasksToWorker(WorkerHandler worker, String userName) throws IOException {
            List<Task> tasksuser = tasks.getAllTasks(userName);
            int size = tasksuser.size();
            worker.sendTaskSize(size);
            if (!tasksuser.isEmpty()) {
                for (Task task : tasksuser) {
                    worker.sendTask(task);
                }
            }
        }


        private WorkerHandler assignWorkerToUser() throws InterruptedException {
            synchronized (lock1) {
                while (freeWorkers.isEmpty()) {
                    System.out.println("Waiting for free worker for user");
                    lock1.wait();

                }
                WorkerHandler worker = freeWorkers.remove(0);
                workerUserMap.put(worker, clientSocket);
                System.out.println("Assigned worker to user: " + clientSocket);
                return worker;
            }
        }
        public void sendData(Results data, Results averages, Map<String,Double> perc) throws IOException {
            // Write the data object to the ObjectOutputStream
            System.out.println("hello from sending the data");

            Gson gson = new Gson();
            String jsonResults = gson.toJson(data);

            Gson gson1 = new Gson();
            String jsonAverages = gson.toJson(averages);

            Gson gson2 = new Gson();
            String jsonPercs = gson.toJson(perc);

            System.out.println(" json data for the user "+jsonResults);
            oos.println(jsonResults);
            oos.println(jsonAverages);
            oos.println(jsonPercs);

            // Flush the stream to ensure the data is sent immediately
            oos.flush();
            oos.close();
//            oos.close();
        }
    }
}