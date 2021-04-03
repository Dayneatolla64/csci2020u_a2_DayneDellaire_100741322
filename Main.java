package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.*;
import java.util.*;
import javafx.geometry.Insets;
import java.net.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.*;


public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        displayMenu(primaryStage);
    }


/*
Displays the menu
 */
    private void displayMenu(Stage primaryStage){
        primaryStage.setTitle("Assignment 2 By: Dayne Dellaire");
        loadLocalDir(primaryStage);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        Button btApp1 = new Button("Connect to server");
        btApp1.setPrefWidth(200);
        Button btApp2 = new Button("Change Local Directory");
        btApp2.setPrefWidth(200);

        Label ipLabel = new Label("ip:     ");
        TextField ipField = new TextField ();
        Label portLabel = new Label("port: ");
        TextField portField = new TextField ();

        HBox ipBox = new HBox();
        HBox portBox = new HBox();

        ipBox.getChildren().addAll(ipLabel, ipField);
        portBox.getChildren().addAll(portLabel, portField);

        grid.add(btApp1, 0,3);
        grid.add(ipBox, 0,1);
        grid.add(portBox, 0,2);
        grid.add(btApp2, 0,4);

        Scene mainScene = new Scene(grid, 300, 275);
        primaryStage.setScene(mainScene);
        primaryStage.show();

        btApp1.setOnAction(new EventHandler<ActionEvent>() { //Displays the file transfer scree
            @Override
            public void handle(ActionEvent actionEvent) {
                ipField.getText();

                displayScene(primaryStage, ipField.getText(), Integer.parseInt(portField.getText()));
            }
        });
        btApp2.setOnAction(new EventHandler<ActionEvent>() { //Allows the user to change the file directory
            @Override
            public void handle(ActionEvent actionEvent) {
                createConfig(primaryStage);
            }
        });
    }

    /*
    This method will display the file transfer screen
    */

    private void displayScene(Stage primaryStage, String ip, int port){
        primaryStage.setTitle("Shared File v1.0");

        String dir = getLocalDirectory();
        ListView localList = getLocalList(dir);
        ListView sharedList = getSharedList(ip, port);

        Button btApp1 = new Button("Download");
        btApp1.setPrefWidth(80);
        Button btApp2 = new Button("Upload");
        btApp2.setPrefWidth(80);
        Button btApp3 = new Button("Refresh");
        btApp3.setPrefWidth(80);

        HBox hBoxButtons = new HBox();
        hBoxButtons.setSpacing(5);
        hBoxButtons.getChildren().addAll(btApp1,btApp2,btApp3);

        HBox hBoxLists = new HBox();
        hBoxLists.setSpacing(4);
        hBoxLists.setPadding(new Insets(3, 3, 3, 3));
        hBoxLists.getChildren().addAll(localList, sharedList);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(hBoxButtons,hBoxLists);

        Scene mainScene = new Scene(vBox, 550, 700);
        primaryStage.setScene(mainScene);
        primaryStage.show();

        btApp1.setOnAction(new EventHandler<ActionEvent>() { //New
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("Download Pressed!");
                downloadFiles(dir, (String)sharedList.getSelectionModel().getSelectedItem(), ip, port);
                displayScene(primaryStage, ip, port);
            }
        });
        btApp2.setOnAction(new EventHandler<ActionEvent>() { //New
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("Upload Pressed!");
                uploadFiles(getLocalDirectory() + "/" + localList.getSelectionModel().getSelectedItem(), ip, port);
                displayScene(primaryStage, ip, port);
            }
        });
        btApp3.setOnAction(new EventHandler<ActionEvent>() { //New
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("Refresh Pressed!");
                displayScene(primaryStage, ip, port);
            }
        });
    }

    /*
    This method will download the files from the server
    */

    private void downloadFiles(String dir, String fileName, String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            byte[] commandNameBytes = "DOWNLOAD".getBytes();
            dataOutputStream.writeInt(commandNameBytes.length);
            dataOutputStream.write(commandNameBytes);
            System.out.println("HERE!");
            byte[] fileNameBytes = fileName.getBytes();
            dataOutputStream.writeInt(fileNameBytes.length);
            dataOutputStream.write(fileNameBytes);
            System.out.println("HERE2");
            int fileContentLength = dataInputStream.readInt();
            if (fileContentLength > 0) {
                byte[] fileContentBytes = new byte[fileContentLength];
                dataInputStream.readFully(fileContentBytes, 0, fileContentLength);
                writeToFile(fileName, fileContentBytes, dir);
                socket.close();
                return;
            } else {
                socket.close();
                writeToFile(fileName, dir);
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /*
    This method will upload the files from the local machine
    */

    private void uploadFiles(String dir, String ip, int port) {
        File uploadFile = new File(dir);

        try {
            FileInputStream fileInputStream = new FileInputStream(uploadFile.getAbsolutePath());
            Socket socket = new Socket(ip, port);

            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            byte[] commandNameBytes = "UPLOAD".getBytes();

            String fileName = uploadFile.getName();
            byte[] fileNameBytes = fileName.getBytes();

            byte[] fileContentBytes = new byte[(int) uploadFile.length()];
            fileInputStream.read(fileContentBytes);

            dataOutputStream.writeInt(commandNameBytes.length);
            dataOutputStream.write(commandNameBytes);

            dataOutputStream.writeInt(fileNameBytes.length);
            dataOutputStream.write(fileNameBytes);

            dataOutputStream.writeInt(fileContentBytes.length);
            dataOutputStream.write(fileContentBytes);
            socket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /*
    This method will get a list of file names from the server then return them
    to be displayed in the file transfer menu
    */

    private ListView getSharedList(String ip, int port){
        ListView listView = new ListView();
        String fileNames = null;

        listView.setPrefWidth(270);
        listView.setPrefHeight(675);

        try {
            Socket socket = new Socket(ip, port);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            byte[] commandNameBytes = "DIR".getBytes();

            dataOutputStream.writeInt(commandNameBytes.length);
            dataOutputStream.write(commandNameBytes);

            int fileNameLength = dataInputStream.readInt();
            byte[] fileNameBytes = new byte[fileNameLength];
            dataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);
            fileNames = new String(fileNameBytes);
            socket.close();
        } catch(IOException e) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Server is offline");
            alert.setHeaderText(null);
            alert.setContentText("The server is offline, try again later");
            alert.showAndWait();
        }

        String fileList[] = fileNames.split(" ");

        for(int i = 0; i < fileList.length; i++){
            listView.getItems().add(fileList[i]);
        }

        return listView;
    }

    /*
    This method will read the config file and return the local directory
    */

    private String getLocalDirectory() {
        String dir = "";

        try {
            Scanner scanner = new Scanner(new File("config.txt"));
            dir = scanner.nextLine();
        } catch (IOException e) {System.out.println("Error: Could not read config");}

        return dir;
    }

    /*
    This method will return a list of all the file names in the local directory
    */

    private ListView getLocalList(String dir){
        File localDirectory = new File(dir);
        String[] localList = localDirectory.list();
        ListView listView = new ListView();
        listView.setPrefWidth(270);
        listView.setPrefHeight(675);

        for(int i = 0; i < localList.length; i++){
            listView.getItems().add(localList[i]);
        }

        return listView;
    }

    /*
    This method will check if a config exsists and if it doesnt it will prompt the user
    to select a local directory then it will make a config containing the absolute path to that directory
    */

    private void loadLocalDir(Stage stage){
        if(!getConfig()){
            createConfig(stage);
        }
    }

    /*
    creates a config with the user selected directory
    */

    private void createConfig(Stage stage){
        try {
            File config = new File("config.txt");
            config.createNewFile();
            FileWriter writer = new FileWriter("config.txt");
            writer.write(ChooseDirectory(stage));
            writer.close();
        } catch (IOException e) {System.out.println("Error: Could not create config");}
    }

    /*
    checks if the config exsists
    */

    private boolean getConfig(){
        File config = new File("config.txt");
        return config.exists();
    }

    /*
    Allows the user to select a directory then returns the absolute path
    */

    private String ChooseDirectory(Stage stage){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Choose Local Directory");
        alert.setHeaderText(null);
        alert.setContentText("Choose the directory in which you would like to view files from");
        alert.showAndWait();

        File selectedDirectory = directoryChooser.showDialog(stage);

        return selectedDirectory.getAbsolutePath();
    }

    /*
    create a file at a certain directory with the filename
    (no contents)
    */

    public void writeToFile(String fileName, String dir) {
        File file = new File(dir + "/" + fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    create a file at a certain directory with the filename
    (with contents)
    */

    public void writeToFile(String fileName, byte[] fileContentBytes, String dir) {
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

    public static void main(String[] args) {
        launch(args);
    }
}
