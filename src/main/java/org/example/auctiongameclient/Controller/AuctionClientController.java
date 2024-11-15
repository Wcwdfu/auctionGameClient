package org.example.auctiongameclient.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import org.example.auctiongameclient.AuctionManager;
import org.example.auctiongameclient.ChatManager;
import org.example.auctiongameclient.utils.UIUtils;

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
    private ImageView goodsImageView;

    private BufferedReader in;
    private PrintWriter out;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private final String serverAddress = "localhost";
    private String userName;

    private AuctionManager auctionManager;
    private ChatManager chatManager;

    private Map<String, String> goodsImages;
    private Map<String, String> itemImages;

    public void initialize() {
        initializeButtons();
        initializeImages();
        initializeChatInput();
    }

    private void initializeButtons() {
        participateButton.setDisable(true);
        notParticipateButton.setDisable(true);
        bid1Button.setDisable(true);
        bid5Button.setDisable(true);
    }

    private void initializeImages() {
        goodsImages = new HashMap<>();
        goodsImages.put("쿠", "/images/쿠.png");
        goodsImages.put("건구스", "/images/건구스.png");
        goodsImages.put("건덕이", "/images/건덕이.png");
        goodsImages.put("건붕이", "/images/건붕이.png");

        itemImages = new HashMap<>();
        itemImages.put("건구스의 지원금", "/images/건구스의지원금.png");
        itemImages.put("황소의 분노", "/images/황소의분노.png");
        itemImages.put("일감호의 기적", "/images/일감호의기적.png");
        itemImages.put("스턴건", "/images/스턴건.png");
    }

    private void initializeChatInput() {
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
            out.println(userName);

            auctionManager = new AuctionManager(out, messageArea);
            chatManager = new ChatManager(out, chatArea, chatInputField);

            executor.submit(this::receiveMessages);

            updateUIAfterConnect();
        } catch (IOException e) {
            messageArea.appendText("서버에 연결할 수 없습니다: " + e.getMessage() + "\n");
        }
    }

    private void updateUIAfterConnect() {
        connectButton.setDisable(true);
        participateButton.setDisable(false);
        notParticipateButton.setDisable(false);
        bid1Button.setDisable(false);
        bid5Button.setDisable(false);
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
        chatManager.sendChatMessage();
    }


    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                final String msg = message;
                UIUtils.runOnUIThread(() -> processMessage(msg));
            }
        } catch (IOException e) {
            UIUtils.runOnUIThread(() -> messageArea.appendText("서버와의 연결이 끊어졌습니다.\n"));
        }
    }


    private void processMessage(String msg) {
        if (msg.startsWith("채팅 ")) {
            chatManager.receiveChatMessage(msg.substring(3));
        } else if (msg.startsWith("경매를 시작합니다. 경매품목: ")) {
            handleAuctionStartMessage(msg);
        } else {
            handleGeneralMessage(msg);
        }
    }


    private void handleAuctionStartMessage(String msg) {
        String itemName = msg.substring(msg.lastIndexOf(":") + 2).trim();
        updateAuctionItemImage(itemName);
        messageArea.appendText(msg + "\n");
    }


    private void handleGeneralMessage(String msg) {
        messageArea.appendText(msg + "\n");
    }


    private void updateAuctionItemImage(String itemName) {
        String imagePath = goodsImages.getOrDefault(itemName, itemImages.get(itemName));

        if (imagePath != null) {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            goodsImageView.setImage(image);
        } else {
            goodsImageView.setImage(null);
        }
    }
}
