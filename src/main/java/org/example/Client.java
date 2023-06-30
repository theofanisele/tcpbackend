//package org.example;
//import io.jenetics.jpx.GPX;
//import java.io.*;
//import java.net.*;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.Scanner;
//
//import io.jenetics.jpx.Latitude;
//import io.jenetics.jpx.WayPoint;
//import org.apache.commons.lang3.SerializationUtils;
//
//
//public class Client {
//    public static void main(String[] args) {
//        try {
//            Socket client = new Socket("localhost", 8000);
//            System.out.println("Client " + client.getInetAddress().getHostName() + " connected to the server.");
//
//            GPX gpx = GPX.read(Path.of("src/main/java/org/example/route1.gpx"));
//
//            GpxObject gpxObject = new GpxObject(gpx);
//
//            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
//            oos.writeObject(gpxObject);
//
//
////
////            OutputStream os = client.getOutputStream();
////            os.write(data);
////            os.flush();
//
//
////            client.close();
//        } catch (IOException e) {
//            System.out.println(e);
//        }
//    }
//}
package org.example;
import io.jenetics.jpx.GPX;
import java.io.*;
import java.net.*;
import java.nio.file.Path;


public class Client {
    private Socket client;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Client(String host, int port) {
        try {
            System.out.println("hello");
            this.client = new Socket(host, port);
            this.out = new ObjectOutputStream(client.getOutputStream());
            this.in = new ObjectInputStream(client.getInputStream());

            System.out.println("Client " + client.getInetAddress().getHostName() + " connected to the server.");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void upload(String filePath) {
        try {
            GPX gpx = GPX.read(Path.of(filePath));
            GpxObject gpxObject = new GpxObject(gpx);
            this.out.writeObject(gpxObject);
            this.out.flush();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public Object receive() {
        try {
            return this.in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e);
        }
        return null;
    }

    public static void main(String[] args) {
        for (int i = 0; i <6 ; i++) {
        }

        Client client = new Client("localhost", 8000);
        System.out.println("hello");

    }
}
//        client.upload("src/main/java/org/example/route1.gpx");
