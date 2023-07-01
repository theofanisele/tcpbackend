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
    private final Map<Connector, WorkerHandler> workerUserMap = new HashMap<>();
    private final ServerSocket ws;
    private final ServerSocket cs;
    private final TaskManager tasks;
    private final Object lock = new Object();
    private final Object lock1 = new Object();
    private List<Connector> connectors = new ArrayList<>();
    private Repository repo = new Repository();

    public Server() throws IOException {
        this.cs = new ServerSocket(8000);
        this.ws = new ServerSocket(8001);
        this.tasks = new TaskManager();
//        int nThreads = Runtime.getRuntime().availableProcessors();

        for (int i = 0; i < 1; i++) {
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

    public void start() throws IOException, ClassNotFoundException, InterruptedException {

        while (true) {
            try {
                System.out.println("Waiting for clients...");

                Socket clientSocket = new Socket();
                clientSocket = cs.accept();
                Connector connector = new Connector(clientSocket);
                connectors.add(connector);
                System.out.println(clientSocket.getInetAddress() + " connected.");
                Thread thread = new Thread(new ClientHandler(clientSocket,connector));
                thread.start();
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

    private class WorkerHandler extends Thread{
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

                try {
                    synchronized (lock1) {
                        IntermediateResult intResult = receiveIntermediateResult();
                        System.out.println(intResult.getTotalTimes());
                        System.out.println(intResult.getName());
                        Results res = reduce(intResult);
                        System.out.println(res.toString());
                        repo.addResults(res);
                        System.out.println(repo.getResult(res.getName()));
                        lock1.notifyAll();


                    }

                } catch (Exception e) {
                    System.out.println("ERRORE RE PELLE 4");

                    e.printStackTrace();
                    return;
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



        private void markAsFree() {
            synchronized (lock) {
                WorkerHandler userName = workerUserMap.remove(this);
                freeWorkers.add(this);
                System.out.println("Worker is now free");
                lock.notifyAll();
            }
        }
        public IntermediateResult receiveIntermediateResult() throws IOException, ClassNotFoundException {
            return (IntermediateResult) ois.readObject();
        }


    }
    private class ClientHandler extends Thread {
        private final DataInputStream dis ;
        private final Socket clientSocket;
        private String userName;
        private Connector connector;
        private WorkerHandler worker;
        private final PrintWriter oos;
        private final AtomicBoolean running = new AtomicBoolean(false);


        public ClientHandler(Socket clientSocket, Connector connector) throws IOException {
            this.connector = connector;
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


                    worker = assignWorkerToUser(connector);


                    System.out.println(worker);
                    System.out.println("handle client: ");

                    createTasks(userName, receivedData.getWaypoints());
                    System.out.println("tasks size before sending to the worker: " + tasks.size());

                    System.out.println("All tasks have been created for user: " + userName);

                    sendTasksToWorker(worker, userName);

                    System.out.println("tasks size after sending to the worker: " + tasks.size());// Assuming that this method will retrieve the next task for the user.

                    System.out.println("Processing completed for user: " + userName);

                    System.out.println("Free Workers: " + freeWorkers.size());
                    synchronized (lock1) {
                        while (repo.getResult(userName) == null) {
                            try {
                                lock1.wait();
                            } catch (InterruptedException e) {
                                System.out.println("ERRORE RE PELLE 5");
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    System.out.println(repo.getResult(userName).toString());
                    sendData(repo.getResult(userName));
                worker.markAsFree();
                    running.set(false);
                    //stop();



            } catch (Exception e) {

                System.out.println("ERRORE RE PELLE 1");
                stop();
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


//private byte[] readData(DataInputStream dis) throws IOException {
//    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//    byte[] buffer = new byte[1024]; // size depends on the expected data
//    int bytesRead;
//
//    // read the first 4 bytes for the file length
//    byte[] lengthBytes = new byte[4];
//    dis.readFully(lengthBytes);
//    int length = ByteBuffer.wrap(lengthBytes).getInt();
//
//    while (length > 0 && (bytesRead = dis.read(buffer, 0, Math.min(buffer.length, length))) != -1) {
//        baos.write(buffer, 0, bytesRead);
//        length -= bytesRead; // subtract the number of bytes read from the length
//    }
//
//    return baos.toByteArray();
//}

        private void createTasks(String userName, List<Wpt> waypoints) {
            for (int i = 0; i < waypoints.size() - 1; i++) {
                List<Wpt> wpts = new ArrayList<>();
                wpts.add(waypoints.get(i));
                wpts.add(waypoints.get(i + 1));
                tasks.addTask(userName, wpts);

            }
            System.out.println(tasks.getTasks(userName).toString());
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

        private WorkerHandler assignWorkerToUser(Connector connector) throws InterruptedException {
            synchronized (lock) {
                while (freeWorkers.isEmpty()) {
                        System.out.println("Waiting for free worker for user: " + connector);
                        lock.wait();
                    System.out.println("hello");
                }
//                lock.notifyAll();
                WorkerHandler worker = freeWorkers.remove(0);
                workerUserMap.put(connector, worker);
                System.out.println("Assigned worker to user: " + connector);

                return worker;
            }
        }
        public void sendData(Results data) throws IOException {
            // Write the data object to the ObjectOutputStream
            Gson gson = new Gson();
            String jsonString = gson.toJson(data);
            oos.println(jsonString);

            // Flush the stream to ensure the data is sent immediately
            oos.flush();
//            oos.close();
        }
    }
}