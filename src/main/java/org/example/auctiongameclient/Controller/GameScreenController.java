package org.example.auctiongameclient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class GameScreenController {

    @FXML
    private TextArea messageArea;  // 경매 상태 메시지 출력 영역
    @FXML
    private Button participateButton;
    @FXML
    private Button bid1Button;
    @FXML
    private Button bid5Button;
    @FXML
    private Label highestBidLabel;  // 최고 입찰 정보 표시
    @FXML
    private Label currentPlayerLabel;  // 현재 플레이어 잔액 표시

    private String userName;
    private int playerBalance = 100;  // 각 플레이어의 시작 금액
    private int currentBid = 0;  // 현재 최고 입찰 금액
    private String highestBidder = "";  // 현재 최고 입찰자

    // 게임을 시작하며 사용자 이름을 초기화합니다.
    public void initializeGame(String userName) {
        this.userName = userName;
        messageArea.appendText("게임이 시작되었습니다! " + userName + "님, 입찰해 주세요.\n");
        updateBalanceDisplay();
    }

    // 경매에 참여하는 메서드
    @FXML
    private void participateInAuction() {
        participateButton.setDisable(true);  // 한 번 참가하면 버튼 비활성화
    }

    // 1원 단위로 입찰하는 메서드
    @FXML
    private void placeBid1() {
        placeBid(1);
    }

    // 5원 단위로 입찰하는 메서드
    @FXML
    private void placeBid5() {
        placeBid(5);
    }

    // 실제 입찰을 수행하는 메서드
    private void placeBid(int bidAmount) {
        if (playerBalance < bidAmount) {
            messageArea.appendText("잔액이 부족하여 입찰할 수 없습니다.\n");
            return;
        }

        if (bidAmount <= currentBid) {
            messageArea.appendText("현재 최고 입찰가보다 높은 금액을 제시해야 합니다.\n");
            return;
        }

        // 입찰 성공 시 잔액을 차감하고 최고 입찰 정보 갱신
        playerBalance -= bidAmount;
        currentBid = bidAmount;
        highestBidder = userName;

        messageArea.appendText(userName + "님이 " + bidAmount + "원에 입찰하였습니다.\n");
        updateHighestBidDisplay();
        updateBalanceDisplay();
    }

    // 최고 입찰 정보를 업데이트하는 메서드
    private void updateHighestBidDisplay() {
        highestBidLabel.setText("최고 입찰: " + highestBidder + " - " + currentBid + "원");
    }

    // 플레이어의 잔액 정보를 업데이트하는 메서드
    private void updateBalanceDisplay() {
        currentPlayerLabel.setText("잔액: " + playerBalance + "원");
    }

    // 서버로부터 메시지를 수신하고 UI에 반영하는 메서드 (예시)
    public void receiveServerMessage(String message) {
        Platform.runLater(() -> {
            if (message.startsWith("입찰 ")) {
                String[] parts = message.split(" ");
                highestBidder = parts[1];
                currentBid = Integer.parseInt(parts[2]);
                messageArea.appendText("최고 입찰자: " + highestBidder + " - " + currentBid + "원\n");
                updateHighestBidDisplay();
            } else {
                messageArea.appendText(message + "\n");
            }
        });
    }
}
