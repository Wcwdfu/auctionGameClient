package org.example.auctiongameclient.Controller;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.example.auctiongameclient.AuctionManager;
import org.example.auctiongameclient.ChatManager;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//배경음악기능 위한 라이브러리
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
//

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
    private Button musicButton;
    @FXML
    private ImageView goodsImageView; //이미지
    @FXML
    private HBox musicBox;
    @FXML
    private Slider volumeSlider;

    @FXML
    private ImageView gifImageView;
    private Image gifImage;

    @FXML
    private MediaView mediaView; //음악
    MediaPlayer mediaPlayer;
    Media media;

    private Boolean musicFlag = true;
    private ReadOnlyObjectProperty<Duration> currentMusicTime;

    private BufferedReader in;
    private PrintWriter out;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private final String serverAddress = "localhost";
    private String userName;
    private Socket socket;

    private AuctionManager auctionManager;
    private ChatManager chatManager;

    // 굿즈 이름과 이미지 파일 경로 매핑
    private Map<String, String> goodsImages;
    private Map<String, String> itemImages;

    public void initialize() { //FXML파일이 로드된 후 JavaFX가 자동으로 호출하는 메서드
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
        
        //배경음악기능
        String path = new File("src/main/resources/bgms/BOX 15.mp3").getAbsolutePath();
        media = new Media(new File(path).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

        volumeSlider.setValue(mediaPlayer.getVolume() * 100);
        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                mediaPlayer.setVolume(volumeSlider.getValue() / 100);
            }
        });


//        gifImage = new Image(getClass().getResource("/images/sound.gif").toExternalForm(), true);
//        gifImageView.setImage(gifImage);


        Platform.runLater(() -> {
            getServerMessage(); // 초기화 이후 실행될 메서드
        });

        // 채팅 입력창에서 Enter 키를 누르면 메시지 전송
        chatInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendChatMessage();
            }
        });
    }



    public void setSocketAndName(Socket socket, String userName) {
        this.socket = socket;
        this.userName = userName;
    }

    @FXML
    private void getServerMessage() {

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            messageArea.appendText("서버에 연결되었습니다.\n");
            out.println(userName);  // 서버로 이름 전송

            auctionManager = new AuctionManager(out, messageArea);
            chatManager = new ChatManager(out, chatArea, chatInputField);

            executor.submit(this::receiveMessages);

            participateButton.setDisable(false);
            notParticipateButton.setDisable(false);
            bid1Button.setDisable(false);
            bid5Button.setDisable(false);
        } catch (IOException e) {
            messageArea.appendText("서버에 연결할 수 없습니다: " + e.getMessage() + "\n");
        }
    }

    @FXML
    private void musicPlayPause() {
        ImageView imageView;

        if (musicFlag) {
            musicFlag = false;
            musicButton.getStyleClass().removeAll("playButton");
            musicButton.getStyleClass().add("pauseButton");
            musicBox.getStyleClass().removeAll("music-box-playing");
            musicBox.getStyleClass().add("music-box-paused");

            mediaPlayer.pause();
        } else {
            musicFlag = true;
            musicButton.getStyleClass().removeAll("pauseButton");
            musicButton.getStyleClass().add("playButton");

            musicBox.getStyleClass().removeAll("music-box-paused");
            musicBox.getStyleClass().add("music-box-playing");

            mediaPlayer.play();
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
