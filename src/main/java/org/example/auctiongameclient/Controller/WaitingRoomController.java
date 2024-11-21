package org.example.auctiongameclient.Controller;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.auctiongameclient.AuctionClientApplication;
import org.example.auctiongameclient.GameScreenController;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WaitingRoomController {

    @FXML
    private Label player1Label, player2Label, player3Label, player4Label;
    @FXML
    private TextArea chatArea;

    private final List<String> players = new ArrayList<>();  // 입장한 플레이어들의 이름을 저장하는 리스트
    private String userName;
    private Socket socket;
    private PrintWriter out;
    private Scanner in;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> messageTask;

    public void modifyView(String[] userNames) {
        players.clear();
        for (String username : userNames) {
            addPlayer(username);
        }
    }
    public void countDownView(int count){
        System.out.println("count = " + count);
        if (count == 0) {
            try {
                // 다른 화면을 로드하기 위한 FXMLLoader
                FXMLLoader loader = new FXMLLoader(AuctionClientApplication.class.getResource("auction-client-view.fxml"));
                Parent root = loader.load();


                // 새 Scene과 Stage로 화면을 전환
                Stage stage = (Stage) player1Label.getScene().getWindow();  // 현재 화면에서 Stage를 가져옴
                stage.setScene(new Scene(root));  // 새 Scene을 설정

                // 새로운 화면을 표시
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            chatArea.setText(count+"초 뒤에 시작합니다!");
        }
    }
    public void addPlayer(String userName) {
        if (players.size() == 0)
            player1Label.setText(userName);
        else if (players.size() == 1)
            player2Label.setText(userName);
        else if (players.size() == 2)
            player3Label.setText(userName);
        else if (players.size() == 3)
            player4Label.setText(userName);
        players.add(userName);
    }
/*    public void initializeConnection(Socket socket, PrintWriter out, Scanner in) {
        this.socket = socket;
        this.out = out;
        this.in = in;

        new Thread(() -> {
            while (in.hasNextLine()) {
                String message = in.nextLine();
                Platform.runLater(() -> processServerMessage(message));
            }
        }).start();
    }*/

/*
    private void processServerMessage(String message) {
        if (message.startsWith("JOIN ")) {
            String playerName = message.substring(5);
            modifyView(playerName);  // 새로운 플레이어를 리스트에 추가
        } else if (message.startsWith("COUNTDOWN ")) {
            int seconds = Integer.parseInt(message.substring(10));
            chatArea.appendText(seconds + "초 후 게임이 시작됩니다.\n");
            if (seconds == 0) {
                startGame();
            }
        } else if (message.startsWith("PLAYER_LIST ")) {
            String[] playerNames = message.substring(12).split(" ");
            players.clear();
            for (String name : playerNames) {
                modifyView(name);
            }
        } else {
            chatArea.appendText(message + "\n");
        }
    }
*/

    // 플레이어 리스트에 따라 각 Label을 업데이트하는 메서드
    private void updatePlayerLabels() {
        if (players.size() > 0) player1Label.setText(players.get(0));
        if (players.size() > 1) player2Label.setText(players.get(1));
        if (players.size() > 2) player3Label.setText(players.get(2));
        if (players.size() > 3) {
            player4Label.setText(players.get(3));
            out.println("START_COUNTDOWN");  // 네 번째 플레이어가 입장했을 때 카운트다운 시작 요청
        }
    }

    private void startGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("game-screen-view.fxml"));
            Parent gameScreenRoot = loader.load();

            GameScreenController gameScreenController = loader.getController();
            gameScreenController.initializeGame(userName);

            Stage stage = (Stage) player1Label.getScene().getWindow();
            stage.setScene(new Scene(gameScreenRoot));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
