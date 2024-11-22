package org.example.auctiongameclient.Controller;

import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
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
import org.example.auctiongameclient.domain.User;
import org.example.auctiongameclient.domain.UserFactory;

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
        System.out.println("port = " + port);
        System.out.println("name = " + name);
        
        // 포트 번호와 이름이 입력되지 않았을 경우 경고
        if (port.isEmpty() || name.isEmpty()) {
            showAlert("포트 번호와 이름을 모두 입력하세요.");
            return;
        }

        try {

            socket = new Socket("localhost", Integer.parseInt(port));
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new Scanner(socket.getInputStream());

            // 서버에 이름 전송
            //MatchingRequest matchingRequest = new MatchingRequest(name);
            out.println(name);

            //유저 등록
            UserFactory.initialize(new User(name),socket);

            // 연결 성공 시 대기방 화면으로 전환
            openWaitingRoom(in);
        } catch (IOException e) {
            showAlert("서버에 연결할 수 없습니다: " + e.getMessage());
        }
    }

    private void openWaitingRoom(Scanner scanner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/auctiongameclient/waiting-room-view.fxml"));
            Parent root = loader.load();

            WaitingRoomController waitingRoomController = loader.getController();

            //waitingRoomController.initializeUserName(userName);

            Stage stage = (Stage) portField.getScene().getWindow();
            stage.setScene(new Scene(root));

            new Thread(() -> {
                AtomicBoolean bool= new AtomicBoolean(true);
                while (bool.get()) {
                    boolean b = scanner.hasNextLine();
                    String input = scanner.nextLine();
                    Platform.runLater(() -> {
                        if(input.startsWith("Matching;")){
                            String remainingText = input.substring("Matching;".length());
                            String[] userNames = remainingText.split(",");
                            waitingRoomController.modifyView(userNames);
                        }
                        else if(input.startsWith("MatchingFinished;")){
                            String remainingText = input.substring("MatchingFinished;".length());
                            int count= Integer.parseInt(remainingText);

                            waitingRoomController.countDownView(count);
                            if(count==0)
                                bool.set(false);

                        }
                        System.out.println("User Input: " + input);
                        // 필요한 경우 추가 로직 수행
                    });
                }
                System.out.println("종료");
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
