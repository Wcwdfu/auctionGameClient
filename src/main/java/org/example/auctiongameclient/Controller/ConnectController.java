package org.example.auctiongameclient.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ConnectController {
    @FXML
    private TextField portField;
    @FXML
    private TextField nameField;

    private Socket socket;
    private PrintWriter out;
    private Scanner in;

    @FXML
    private void connectToServer() {
        String port = portField.getText();
        String name = nameField.getText();

        // 포트 번호와 이름이 입력되지 않았을 경우 경고
        if (port.isEmpty() || name.isEmpty()) {
            showAlert("포트 번호와 이름을 모두 입력하세요.");
            return;
        }

        try {
            // 서버에 연결
            socket = new Socket("localhost", Integer.parseInt(port)); // 예시로 localhost 사용
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new Scanner(socket.getInputStream());

            // 서버에 이름 전송
            out.println(name);

            // 연결 성공 시 대기방 화면으로 전환
            openWaitingRoom(name);
        } catch (IOException e) {
            showAlert("서버에 연결할 수 없습니다: " + e.getMessage());
        }
    }

    private void openWaitingRoom(String userName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/auctiongameclient/waiting-room-view.fxml"));
            Parent root = loader.load();

            WaitingRoomController waitingRoomController = loader.getController();
            waitingRoomController.initializeUserName(userName);

            Stage stage = (Stage) portField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("연결 오류");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
