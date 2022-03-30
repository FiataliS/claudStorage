package com.cloudStorage.client;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;


import com.cloudStorage.module.model.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
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
        if (chekSelectedView(serverView)) {
            oos.writeObject(new FileRequest(serverView.getSelectionModel().getSelectedItem(), delete));
            updateClientView();
            selectView(clientView, serverView.getSelectionModel().getSelectedItem());
        } else {
            alert("Файл не выбран", "Выбери файл для загрузки!");
        }
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        if (chekSelectedView(clientView)) {
            String item = clientView.getSelectionModel().getSelectedItem();
            File selected = clientDir.resolve(item).toFile();
            oos.writeObject(new FileMessage(clientDir.resolve(clientView.getSelectionModel().getSelectedItem())));
            deleteFile(selected, item);
            updateClientView();
            selectView(serverView, clientView.getSelectionModel().getSelectedItem());
        } else {
            alert("Файл не выбран", "Выбери файл для отправки!");
        }
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


    private void updateClientView() { //обновление списка клиента
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
                        break;
                    case FILE_DIR:
                        FileDir fd = (FileDir) msg;
                        serverPath.setText(fd.getDir());
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


            clientPath.setText(clientDir.toFile().getPath());

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        /*clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = clientView.getSelectionModel().getSelectedItem();
                if (item.equals("...")) {
                    currentDirectory = clientDir.toFile().getParentFile();
                    updateClientView();
                } else {
                    File selected = clientDir.toFile().toPath().resolve(item).toFile();
                    if (selected.isDirectory()) {
                        currentDirectory = selected;
                        updateClientView();
                    }
                }
            }
        });*/
    }

    private Boolean chekSelectedView(ListView<String> view) { // если файл не выделен возвращает false
        return !view.getSelectionModel().isEmpty();
    }

    private void selectView(ListView<String> view, String name) {
        Platform.runLater(() -> {
            view.getSelectionModel().select(name);
        });
    }

    private void alert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void updateView(ActionEvent actionEvent) {
        updateClientView();
        try {
            oos.writeObject(new ListMessage(clientDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
        viewColor(clientView);
        viewColor(serverView);
    }


    public void viewColor (ListView<String> view){
        Platform.runLater(()->{
            view.styleProperty().set("-fx-background-color: red;");
        });

    }
}
