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
import javafx.scene.control.*;
import javafx.scene.shape.Line;

public class MainController implements Initializable {

    public ListView<String> clientView, serverView;
    public TextField clientPath, serverPath, login;
    public Button download, upload, updateView, authButton;
    public Line line1, line2;
    public Label label1, label2, label3, label4;
    public RadioButton radioButtonDeleteFile;
    public PasswordField pass;


    private Path clientDir;

    private ObjectEncoderOutputStream oos;
    private ObjectDecoderInputStream ois;

    private boolean delete = false;
    private boolean isAuthorized = false;

    String nickName;

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
        if (!fileExtensionCheck(clientView.getSelectionModel().getSelectedItem()) && chekSelectedView(clientView)) {
            alert("Ошибка", "Папку передать не могу!");
        } else if (chekSelectedView(clientView)) {
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

    private void updateClientPatth() {
        clientPath.setText(clientDir.toFile().getPath());
    }

    private void read() {
        updateClientView();
        clientPath.setText(clientDir.toFile().getPath());
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
                            //serverView.getItems().add("...");
                            serverView.getItems().addAll(lm.getFiles());
                        });
                        break;
                    case FILE_DIR:
                        FileDir fd = (FileDir) msg;
                        serverPath.setText(fd.getDir());
                        System.out.println(fd.getDir());
                        break;
                    case AUTH_SERV:
                        AuthServ authServ = (AuthServ) msg;
                        isAuthorized = authServ.getAuth();
                        nickName = authServ.getNick();
                        if (isAuthorized) {
                            oos.writeObject(new ListMessage(clientDir));
                            showHide();
                        } else {

                            alert("Неудача", "Неверный логин и/или пороль");
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        try {
            Socket socket = new Socket("localhost", 8189);
            oos = new ObjectEncoderOutputStream(socket.getOutputStream());
            ois = new ObjectDecoderInputStream(socket.getInputStream());
            clientDir = Paths.get("clientDir");
            Thread readThread = new Thread(this::read);
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connect();

        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = clientView.getSelectionModel().getSelectedItem();
                if (item.equals("...")) {
                    clientDir = clientDir.resolve("..").normalize();
                    updateClientView();
                    updateClientPatth();
                } else {
                    File selected = clientDir.resolve(item).toFile();
                    if (selected.isDirectory()) {
                        clientDir = clientDir.resolve(item).normalize();
                        updateClientView();
                        updateClientPatth();
                    }
                }
            }
        });
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
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    public void updateView(ActionEvent actionEvent) {
        updateClientView();
        try {
            oos.writeObject(new ListMessage(clientDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        viewColor(clientView);
//        viewColor(serverView);
    }


    public void viewColor(ListView<String> view) {
        Platform.runLater(() -> {
            view.styleProperty().set("-fx-background-color: red;");
        });

    }

    private void showHide() {
        Platform.runLater(() -> {
            if (isAuthorized == true) {
                clientView.setVisible(true);
                serverView.setVisible(true);
                clientPath.setVisible(true);
                serverPath.setVisible(true);
                download.setVisible(true);
                upload.setVisible(true);
                updateView.setVisible(true);
                line1.setVisible(true);
                line2.setVisible(true);
                label1.setVisible(true);
                label2.setVisible(true);
                label3.setVisible(false);
                label4.setVisible(false);
                pass.setVisible(false);
                login.setVisible(false);
                radioButtonDeleteFile.setVisible(true);
                authButton.setLayoutX(130);
                authButton.setLayoutY(10);
                authButton.setText("Отключится");

            } else {
                clientView.setVisible(false);
                serverView.setVisible(false);
                clientPath.setVisible(false);
                serverPath.setVisible(false);
                download.setVisible(false);
                upload.setVisible(false);
                updateView.setVisible(false);
                line1.setVisible(false);
                line2.setVisible(false);
                label1.setVisible(false);
                label2.setVisible(false);
                label3.setVisible(true);
                label4.setVisible(true);
                pass.setVisible(true);
                login.setVisible(true);
                radioButtonDeleteFile.setVisible(false);
                authButton.setLayoutX(223);
                authButton.setLayoutY(220);
                authButton.setText("Подключится");
                login.clear();
                pass.clear();
            }
        });
    }

    public void authButton(ActionEvent actionEvent) {
        if (!isAuthorized) {
            if (pass.getLength() > 0 && login.getLength() > 0) {
                try {
                    oos.writeObject(new AuthServ(login.getText(), pass.getText(), isAuthorized));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                alert("Пустые строки", "Заполни логин и/или пороль");
            }
        } else {
            isAuthorized = false;
            showHide();
        }
    }

    public static boolean fileExtensionCheck(String name) { // true если расширение есть
        StringBuilder sb = new StringBuilder(name);
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '.' && sb.charAt(sb.length() - 1) != '.') {
                return true;
            }
        }
        return false;
    }
}
