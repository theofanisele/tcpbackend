package org.example;

import io.jenetics.jpx.GPX;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Path;

public class Main {

        public static void main(String[] args) {
            try {
                Socket client = new Socket("localhost", 8000);
                System.out.println("Client " + client.getInetAddress().getHostName() + " connected to the server.");

//                GPX gpx = GPX.read(Path.of("src/main/java/org/example/route2.gpx"));
//
//                GpxObject gpxObject = new GpxObject(gpx);
//
//                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
//                oos.writeObject(gpxObject);


//
//            OutputStream os = client.getOutputStream();
//            os.write(data);
//            os.flush();


//            client.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

