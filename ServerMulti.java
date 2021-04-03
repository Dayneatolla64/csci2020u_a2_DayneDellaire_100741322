package sample;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.*;
import java.net.*;

public class ServerMulti {
    protected Socket clientSocket           = null;
    protected ServerSocket serverSocket     = null;
    protected ClientConnectionHandler[] threads    = null;
    protected int numClients                = 0;
    protected String dir = "C:/Users/rdell/Desktop/Daynes_Server/Shared";
    public static int SERVER_PORT = 25565;
    public static int MAX_CLIENTS = 100;

    public ServerMulti() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("---------------------------");
            System.out.println("Chat Server Application is running");
            System.out.println("---------------------------");
            System.out.println("Listening to port: "+SERVER_PORT);
            threads = new ClientConnectionHandler[MAX_CLIENTS];
            while(true) {
                clientSocket = serverSocket.accept();
                System.out.println("Client #"+(numClients+1)+" connected.");
                threads[numClients] = new ClientConnectionHandler(clientSocket, dir);
                threads[numClients].start();
                numClients++;
            }
        } catch (IOException e) {
            System.err.println("IOException while creating server connection");
        }
    }

    public static void main(String[] args) {
        ServerMulti app = new ServerMulti();
    }
}
