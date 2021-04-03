package sample;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientConnectionHandler extends Thread{
    protected Socket socket = null;
    protected DataInputStream dataInputStream = null;
    protected DataOutputStream dataOutputStream = null;
    protected String dir = null;

    public ClientConnectionHandler(Socket socket, String dir){
        super();
        this.socket = socket;
        this.dir = dir;

        try{
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        // initialize interaction
        processCommand();

        try {
            socket.close();
            interrupt();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void processCommand(){
        String command = null;

        try{
            int fileNameLength = dataInputStream.readInt();
            byte[] fileNameBytes = new byte[fileNameLength];
            dataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);
            command = new String(fileNameBytes);
        } catch(IOException e) {
            e.printStackTrace();
            System.out.println("Error reading command");
        }

        if(command.equalsIgnoreCase("UPLOAD")) {
            recieveFile();
        } else if(command.equalsIgnoreCase("DOWNLOAD")) {
            sendFile();
        } else if(command.equalsIgnoreCase("DIR")) {
            refreshUser();
        }
    }

    private void sendFile() {
        try {
            int fileNameLength = dataInputStream.readInt();
            if (fileNameLength > 0) {
                byte[] fileNameBytes = new byte[fileNameLength];
                dataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);
                String fileName = new String(fileNameBytes);

                File file = new File(dir + "/" + fileName);

                if(file.exists()) {
                    FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
                    byte[] fileContentBytes = new byte[(int) file.length()];
                    fileInputStream.read(fileContentBytes);

                    dataOutputStream.writeInt(fileContentBytes.length);
                    dataOutputStream.write(fileContentBytes);
                }

                return;
            }
        } catch (IOException e) {
            System.out.println("Error Reading File Content");
            e.printStackTrace();
        }
    }

    public void refreshUser() {
        File directory = new File(dir);

        if(directory.isDirectory()) {
            String[] list = directory.list();
            String fileNames = "";

            for(int i = 0; i < list.length; i++){
                fileNames += list[i];

                if(i < list.length - 1){
                    fileNames += " ";
                }
            }

            try{
                byte[] fileNameBytes = fileNames.getBytes();
                dataOutputStream.writeInt(fileNameBytes.length);
                dataOutputStream.write(fileNameBytes);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void recieveFile() {
        try {
            int fileNameLength = dataInputStream.readInt();
            if (fileNameLength > 0) {
                byte[] fileNameBytes = new byte[fileNameLength];
                dataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);
                String fileName = new String(fileNameBytes);

                int fileContentLength = dataInputStream.readInt();

                if (fileContentLength > 0) {
                    byte[] fileContentBytes = new byte[fileContentLength];
                    dataInputStream.readFully(fileContentBytes, 0, fileContentLength);
                    writeToFile(fileName, fileContentBytes);
                    return;
                }

                System.out.println(fileName);
                writeToFile(fileName);
                return;
            }
        } catch (IOException e) {
            System.out.println("Error Reading File Content");
            e.printStackTrace();
        }
    }

    public void writeToFile(String fileName) {
        File file = new File(dir + "/" + fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(String fileName, byte[] fileContentBytes) {
        File file = new File(dir + "/" + fileName);
        FileOutputStream fos = null;
        try {
            file.createNewFile();
            fos = new FileOutputStream(file);
            fos.write(fileContentBytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
