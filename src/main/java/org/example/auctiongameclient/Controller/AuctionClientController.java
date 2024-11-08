package org.example.auctiongameclient.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import org.example.auctiongameclient.AuctionManager;
import org.example.auctiongameclient.ChatManager;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionClientController {
    @FXML
    private TextArea messageArea;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField portField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField chatInputField;
    @FXML
    private Button connectButton;
    @FXML
    private Button participateButton;
    @FXML
    private Button notParticipateButton;
    @FXML
    private Button bid1Button;
    @FXML
    private Button bid5Button;
    @FXML
    private ImageView goodsImageView; //이미지

    private BufferedReader in;
    private PrintWriter out;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private final String serverAddress = "localhost";
    private String userName;

    private AuctionManager auctionManager;
    private ChatManager chatManager;

    // 굿즈 이름과 이미지 파일 경로 매핑
    private Map<String, String> goodsImages;
    private Map<String, String> itemImages;

    public void initialize() {
        participateButton.setDisable(true);
        notParticipateButton.setDisable(true);
        bid1Button.setDisable(true);
        bid5Button.setDisable(true);

        // 굿즈 이미지 경로 매핑
        goodsImages = new HashMap<>();
        goodsImages.put("쿠", "/images/쿠.png");
        goodsImages.put("건구스", "/images/건구스.png");
        goodsImages.put("건덕이", "/images/건덕이.png");
        goodsImages.put("건붕이", "/images/건붕이.png");

        // 아이템 이미지 경로 매핑
        itemImages = new HashMap<>();
        itemImages.put("건구스의 지원금", "/images/건구스의지원금.png");
        itemImages.put("황소의 분노", "/images/황소의분노.png");
        itemImages.put("일감호의 기적", "/images/일감호의기적.png");
        itemImages.put("스턴건", "/images/스턴건.png");

        // 채팅 입력창에서 Enter 키를 누르면 메시지 전송
        chatInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendChatMessage();
            }
        });
    }

    @FXML
    private void connectToServer() {
        int port = Integer.parseInt(portField.getText());
        userName = nameField.getText().trim();

        if (userName.isEmpty()) {
            messageArea.appendText("이름을 입력하세요.\n");
            return;
        }

        try {
            Socket socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            messageArea.appendText("서버에 연결되었습니다.\n");
            out.println(userName);  // 서버로 이름 전송

            auctionManager = new AuctionManager(out, messageArea);
            chatManager = new ChatManager(out, chatArea, chatInputField);

            executor.submit(this::receiveMessages);

            connectButton.setDisable(true);
            participateButton.setDisable(false);
            notParticipateButton.setDisable(false);
            bid1Button.setDisable(false);
            bid5Button.setDisable(false);
        } catch (IOException e) {
            messageArea.appendText("서버에 연결할 수 없습니다: " + e.getMessage() + "\n");
        }
    }

    @FXML
    private void sendParticipateRequest() {
        auctionManager.participateInAuction();
    }

    @FXML
    private void sendNotParticipateRequest() {
        auctionManager.declineAuction();
    }

    @FXML
    private void sendBid1() {
        auctionManager.placeBid(1);
    }

    @FXML
    private void sendBid5() {
        auctionManager.placeBid(5);
    }

    @FXML
    private void sendChatMessage() {
        String message = chatInputField.getText().trim();
        if (!message.isEmpty()) {
            out.println("채팅 " + message);
            chatInputField.clear();
        }
    }

    // 경매 품목에 따른 이미지 업데이트
    private void updateAuctionItemImage(String itemName) {
        String imagePath;

        if (goodsImages.containsKey(itemName)) {
            imagePath = goodsImages.get(itemName);
        } else {
            imagePath = itemImages.get(itemName);
        }

        if (imagePath != null) {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            goodsImageView.setImage(image);
        } else {
            goodsImageView.setImage(null); // 이미지가 없는 경우 초기화
        }
    }

    //서버로부터 수신된 메세지에 따라 올바른 처리를 함
    //TODO 서버 - 클라이언트간 메세지로 주고받을지, json으로 해야할지 결정해야할듯 함
    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                final String msg = message;
                Platform.runLater(() -> {
                    if (msg.startsWith("채팅 ")) {
                        chatArea.appendText(msg.substring(3) + "\n");
                    } else {
                        messageArea.appendText(msg + "\n");
                        if (msg.startsWith("경매를 시작합니다. 경매품목: ")) {
                            String itemName = msg.substring(msg.lastIndexOf(":") + 2).trim();
                            updateAuctionItemImage(itemName); // 경매 품목 이미지 업데이트
                        }
                    }
                });
            }
        } catch (IOException e) {
            Platform.runLater(() -> messageArea.appendText("서버와의 연결이 끊어졌습니다.\n"));
        }
    }
}
