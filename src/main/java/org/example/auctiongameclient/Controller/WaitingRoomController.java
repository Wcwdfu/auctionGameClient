package org.example.auctiongameclient.Controller;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.auctiongameclient.AuctionClientApplication;

import org.example.auctiongameclient.MediaPlayerManager;

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

    public void modifyView(String[] userNames) {
        players.clear();
        for (String username : userNames) {
            addPlayer(username);
        }
    }

    public void countDownView(int count){
//        System.out.println("count = " + count);

        if (count == 0) {
            try {
                // 다른 화면을 로드하기 위한 FXMLLoader
                FXMLLoader loader = new FXMLLoader(AuctionClientApplication.class.getResource("main-game-view.fxml"));
                Parent root = loader.load();

                // MainGameController 인스턴스 가져오기
                MainGameController mainGameController = loader.getController();
                // 유저 이름 데이터를 MainGameController로 전달
                mainGameController.setUserNames(players.toArray(new String[0]));

                // 새 Scene과 Stage로 화면을 전환
                Stage stage = (Stage) player1Label.getScene().getWindow();  // 현재 화면에서 Stage를 가져옴
                stage.setScene(new Scene(root));  // 새 Scene을 설정
                MediaPlayerManager.stopMediaPlayer();

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
}
