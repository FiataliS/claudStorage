package com.cloudStorage.client;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import com.cloudStorage.server.model.CloudMessage;
import com.cloudStorage.server.model.FileMessage;
import com.cloudStorage.server.model.FileRequest;
import com.cloudStorage.server.model.ListMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class MainController implements Initializable {

    public ListView<String> clientView;
    public ListView<String> serverView;
    public TextField clientPath;
    public TextField serverPath;

    private Path clientDir;

    private ObjectEncoderOutputStream oos;
    private ObjectDecoderInputStream ois;

    private boolean delete = false;

    public void download(ActionEvent actionEvent) throws IOException {
        oos.writeObject(new FileRequest(serverView.getSelectionModel().getSelectedItem()));
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        oos.writeObject(new FileMessage(clientDir.resolve(clientView.getSelectionModel().getSelectedItem())));
    }

    public void radioButtonDeleteFile(ActionEvent actionEvent) throws IOException {
        if (!delete) {
            delete = true;
        } else {
            delete = false;
        }
    }

    private void deleteFile(File file, String name) {
        if (delete) {
            if (file.delete()) {
                System.out.println("File: " + name + " delete");
            }
        }
    }


    private void updateClientView() {
        Platform.runLater(() -> {
            clientView.getItems().clear();
            clientView.getItems().add("...");
            clientView.getItems().addAll(clientDir.toFile().list());
        });
    }

    private void read() {
        try {
            while (true) {
                CloudMessage msg = (CloudMessage) ois.readObject();
                switch (msg.getMessageType()) {
                    case FILE:
                        FileMessage fm = (FileMessage) msg;
                        Files.write(clientDir.resolve(fm.getName()), fm.getBytes());
                        updateClientView();
                        break;
                    case LIST:
                        ListMessage lm = (ListMessage) msg;
                        Platform.runLater(() -> {
                            serverView.getItems().clear();
                            serverView.getItems().add("...");
                            serverView.getItems().addAll(lm.getFiles());
                        });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            oos = new ObjectEncoderOutputStream(socket.getOutputStream());
            ois = new ObjectDecoderInputStream(socket.getInputStream());
            clientDir = Paths.get("clientDir");
            updateClientView();
            Thread readThread = new Thread(this::read);
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
