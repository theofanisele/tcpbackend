package org.example;

import java.net.Socket;

public class Connector {
    private Socket socket;




    public Connector(Socket socket){
        this.socket = socket;

    }

    public Socket getSocket() {
        return this.socket;
    }

}
