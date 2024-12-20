package org.example.auctiongameclient.Controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.example.auctiongameclient.AuctionManager;
import org.example.auctiongameclient.ChatManager;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//배경음악기능 위한 라이브러리
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.example.auctiongameclient.domain.UserFactory;
import org.example.auctiongameclient.utils.UIUtils;
//

public class MainGameController {
    @FXML
    private TextArea mainMessageArea;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField chatInputField;
    @FXML
    private TextArea countArea;

    @FXML
    private ImageView goodsImageView; //이미지

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
    private HBox musicBox;
    @FXML
    private Slider volumeSlider;


    @FXML
    private MediaView mediaView; //음악
    MediaPlayer mediaPlayer;
    Media media;

    @FXML
    private Label userMoneyLabel;
    @FXML
    private Label user1Label, user2Label, user3Label, user4Label;
    private final Label[] userLabels = {user1Label, user2Label, user3Label, user4Label};
    private ArrayList<Label> participatingUserNames = new ArrayList<>();

    private String[] userNames;
    String pattern = "참여명단(.*?) 님";
    Pattern regex = Pattern.compile(pattern);
    Matcher matcher;

    @FXML
    private Pane itemSlot1, itemSlot2, itemSlot3, itemSlot4, itemSlot5, itemSlot6, itemSlot7;
    @FXML
    private Label itemSlot1Label, itemSlot2Label, itemSlot3Label, itemSlot4Label, itemSlot5Label, itemSlot6Label, itemSlot7Label;

    @FXML
    private Label winLabel, loseLabel;

    private Map<Integer, Pane> itemSlots;

    private Boolean musicFlag = true;
    private ReadOnlyObjectProperty<Duration> currentMusicTime;

    private BufferedReader in;
    private PrintWriter out;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private String userName;
    private Socket socket;

//    private boolean itemUsed = false;
    private boolean isAngerActive=false;
    private boolean isMiracleActive=false;


    private AuctionManager auctionManager;
    private ChatManager chatManager;

    // 굿즈 이름과 이미지 파일 경로 매핑
    private Map<String, String> goodsImages;
    private Map<String, String> itemImages;


    public void setUserNames(String[] userNames) {
        this.userNames = userNames;
        updateLabels();
    }

    // 각 라벨에 유저 이름 업데이트
    private void updateLabels() {
        if (userNames != null) {
            if (userNames.length > 0) {
                user1Label.setText(userNames[0]);
            }
            if (userNames.length > 1) {
                user2Label.setText(userNames[1]);
            }
            if (userNames.length > 2) {
                user3Label.setText(userNames[2]);
            }
            if (userNames.length > 3) {
                user4Label.setText(userNames[3]);
            }
        }
    }

    // 슬롯 초기 배경 이미지 설정
    private void setSlotBackground(int slotNumber, String itemName) {
        Pane slot = itemSlots.get(slotNumber);
        if (slot != null) {
            String imagePath = goodsImages.getOrDefault(itemName, itemImages.get(itemName));
            if (imagePath != null) {
                slot.setStyle(
                        "-fx-background-image: url('" + getClass().getResource(imagePath).toExternalForm() + "'); " +
                                "-fx-background-size: cover; -fx-border-color: black;");
            } else {
                slot.setStyle("-fx-background-color: lightgray; -fx-border-color: black;");
            }
        }
    }

    public void initialize() { //FXML파일이 로드된 후 JavaFX가 자동으로 호출하는 메서드
        participateButton.setDisable(true);
        notParticipateButton.setDisable(true);
        bid1Button.setDisable(true);
        bid5Button.setDisable(true);

        userMoneyLabel.setText("소지 금액: 100");

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

        // 아이템 슬롯 초기화
        itemSlots = new HashMap<>();
        itemSlots.put(1, itemSlot1);
        itemSlots.put(2, itemSlot2);
        itemSlots.put(3, itemSlot3);
        itemSlots.put(4, itemSlot4);
        itemSlots.put(5, itemSlot5);
        itemSlots.put(6, itemSlot6);
        itemSlots.put(7, itemSlot7);

        //호버 이벤트
        setupTooltip(itemSlot1, "/images/쿠.png", "쿠: 건국대를 대표하는 황소 마스코트이다.");
        setupTooltip(itemSlot2, "/images/건구스.png", "건구스: 건국대에 상주하는 거위이다. 청심대에서 볼 수 있다");
        setupTooltip(itemSlot3, "/images/건덕이.png", "건덕이: 일감호에 있는 청둥오리이다.");
        setupTooltip(itemSlot4, "/images/건붕이.png", "건붕이: 건국대를 다니는 컴공생이다. 네트워크 프로그래밍을 수강하고 있다.");
        setupTooltip(itemSlot5, "/images/황소의분노.png", "황소의 분노: 해당 라운드 응찰자가 공개되기 전에 해당 경매품을 무조건 유찰시킨다.");
        setupTooltip(itemSlot6, "/images/일감호의기적.png", "일감호의 기적: 경매중 자신이 응찰한 상태이면, 해당 경매품을 0원에 자신이 무조건 낙찰받는다.");
        setupTooltip(itemSlot7, "/images/스턴건.png", "스턴건: 경매중에 대상을 선택하여, 대상의 응찰 상태를 강제로 불응으로 변경시킨다.");

        setSlotBackground(1, "쿠");
        setSlotBackground(2, "건구스");
        setSlotBackground(3, "건덕이");
        setSlotBackground(4, "건붕이");
        setSlotBackground(5, "황소의 분노");
        setSlotBackground(6, "일감호의 기적");
        setSlotBackground(7, "스턴건");

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


        Platform.runLater(() -> { //fxml파일이 load되고나서 실행되도록할수있음
            getServerMessage(); // 초기화 이후 실행될 메서드
        });

        // 채팅 입력창에서 Enter 키를 누르면 메시지 전송
        chatInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendChatMessage();
            }
        });

    }


    @FXML
    private void getServerMessage() {

        try {
            socket = UserFactory.MY_SOCKET;
            userName = UserFactory.MY_INSTANCE.getName();
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            mainMessageArea.appendText("경매 게임에 참여하였습니다!\n");

            out.println(userName);  // 서버로 이름 전송

            auctionManager = new AuctionManager(out, mainMessageArea, countArea);
            chatManager = new ChatManager(out, chatArea, chatInputField);

            executor.submit(this::receiveMessages);

            participateButton.setDisable(false);
            notParticipateButton.setDisable(false);
            bid1Button.setDisable(false);
            bid5Button.setDisable(false);
        } catch (IOException e) {
            mainMessageArea.appendText("서버에 연결할 수 없습니다: " + e.getMessage() + "\n");
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
        chatManager.sendChatMessage();
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
    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                final String msg = message;
//                if (msg.endsWith("성공")) {
//                    itemUsed = true;
//                }
                UIUtils.runOnUIThread(() -> processMessage(msg));
            }
        } catch (IOException e) {
            UIUtils.runOnUIThread(() -> mainMessageArea.appendText("서버와의 연결이 끊어졌습니다.\n"));
        }
    }

    private void processMessage(String msg) {
        if(msg.startsWith("낙찰받았습니다. 축하합니다!")){
            addTextArea(msg);
        }
        else if(msg.startsWith("익명의 유저에게 낙찰되었습니다. 축하드립니다!")){
            addTextArea(msg);
        }



        if (msg.startsWith("채팅")) {
            chatManager.receiveChatMessage(msg.substring(2));

        } else if (msg.equals("호가효과음")) {
            playEffectSound("src/main/resources/effectSounds/betsound.mp3");

        } else if (msg.startsWith("경매를 시작합니다. 경매품목: ")) {
            for (int i = 1; i <= 7; i++) {
                Label countLabel = getLabelForSlot(i);
                countLabel.setDisable(false);
            }

            String itemName = msg.substring(msg.lastIndexOf(":") + 2).trim();
            updateAuctionItemImage(itemName);
            mainMessageArea.appendText(msg + "\n");

            for(Label label:participatingUserNames) {
                label.setStyle(label.getStyle() + "-fx-background-color: lightgray;");
            }
            participatingUserNames.clear();

        } else if (msg.startsWith("메인")) {
            String mainMsg = msg.substring(2);

            if (mainMsg.startsWith("승리자는")) {
                int last = mainMsg.indexOf("님");
                String winner = mainMsg.substring(5, last);
                if (winner.equals(userName)) {
                    mediaPlayer.stop();
                    String path = new File("src/main/resources/bgms/win-soundeffect.mp3").getAbsolutePath();
                    media = new Media(new File(path).toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    mediaView.setMediaPlayer(mediaPlayer);
                    mediaPlayer.setAutoPlay(true);
                    winLabel.setVisible(true);
                    FadeTransition fade = new FadeTransition();
                    fade.setNode(winLabel);
                    fade.setDuration(Duration.millis(3000));
//                fade.setCycleCount(TranslateTransition.INDEFINITE);
                    fade.setInterpolator(Interpolator.LINEAR);
                    fade.setFromValue(0);
                    fade.setToValue(1);
                    fade.play();
                    ScaleTransition scale = new ScaleTransition();
                    scale.setNode(winLabel);
                    scale.setDuration(Duration.millis(3000));
                    scale.setCycleCount(TranslateTransition.INDEFINITE);
                    scale.setInterpolator(Interpolator.LINEAR);
                    scale.setByX(1.4f);
                    scale.setByY(1.4f);
                    scale.setToX(1.5f);
                    scale.setToY(1.5f);
                    scale.setAutoReverse(true);
                    scale.play();

                } else {
                    mediaPlayer.stop();
                    String path = new File("src/main/resources/bgms/lose-soundeffect.mp3").getAbsolutePath();
                    media = new Media(new File(path).toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    mediaView.setMediaPlayer(mediaPlayer);
                    mediaPlayer.setAutoPlay(true);
                    loseLabel.setVisible(true);
                    FadeTransition fade = new FadeTransition();
                    fade.setNode(loseLabel);
                    fade.setDuration(Duration.millis(3000));
                    fade.setCycleCount(TranslateTransition.INDEFINITE);
                    fade.setInterpolator(Interpolator.LINEAR);
                    fade.setFromValue(0);
                    fade.setToValue(1);
                    fade.play();
                    ScaleTransition scale = new ScaleTransition();
                    scale.setNode(loseLabel);
                    scale.setDuration(Duration.millis(1000));
//                scale.setCycleCount(TranslateTransition.INDEFINITE);
                    scale.setInterpolator(Interpolator.LINEAR);
                    scale.setByX(0.5);
                    scale.setByY(0.5);
                    scale.play();
                }


            }
            mainMessageArea.appendText(mainMsg + "\n");



        } else if (msg.startsWith("참여명단")) {
            String list = msg.substring(4);
            mainMessageArea.appendText(list + "\n");

            matcher = regex.matcher(msg);
            if(matcher.find()) {
                String result = matcher.group(1);

                if(result.equals(user1Label.getText())) {
                    user1Label.setStyle(user1Label.getStyle() + "-fx-background-color: lightblue;");
                    participatingUserNames.add(user1Label);

                } else if(result.equals(user2Label.getText())) {
                    user2Label.setStyle(user2Label.getStyle() + "-fx-background-color: lightblue;");
                    participatingUserNames.add(user2Label);

                } else if(result.equals(user3Label.getText())) {
                    user3Label.setStyle(user3Label.getStyle() + "-fx-background-color: lightblue;");
                    participatingUserNames.add(user3Label);

                } else if(result.equals(user4Label.getText())) {
                    user4Label.setStyle(user4Label.getStyle() + "-fx-background-color: lightblue;");
                    participatingUserNames.add(user4Label);
                }
            }
        } else if (msg.startsWith("소지금")) {
            String moneyString = msg.substring(3);
            int money = Integer.parseInt(moneyString.trim());
            Platform.runLater(() -> userMoneyLabel.setText("소지 금액: " + money));

        } else if (msg.startsWith("소유 물품")) {
            String itemsString = msg.substring(6); // "소유 물품: " 이후의 내용
            String[] items = itemsString.split(", "); // 아이템별로 나누기

            for (String item : items) {
                String[] parts = item.split(" x"); // "아이템 이름 x개수" 분리
                String itemName = parts[0].trim();
                int itemCount = Integer.parseInt(parts[1].trim());

                // 아이템 이름에 맞는 슬롯 번호 가져오기
                Integer slotNumber = getSlotNumberForItem(itemName);
                if (slotNumber != null) {
                    updateItemSlot(slotNumber, itemCount); // 슬롯 개수만 업데이트
                } else {
                    System.out.println("Invalid item name: " + itemName); // 디버깅 로그
                }
            }
            isAngerActive=false;
            isMiracleActive=false;

        } else if(msg.startsWith("스턴건")){
            countArea.appendText(msg+"\n");
            bid1Button.setDisable(false);
            bid5Button.setDisable(false);

        } else if(msg.startsWith("participatingListUpdate")) {
            String targetUser = msg.split(" ")[1];
            for(Label label:participatingUserNames) {
                if(label.getText().equals(targetUser)){
                    label.setStyle(label.getStyle() + "-fx-background-color: lightgray;");
                    break;
                }
            }
        } else{
            countArea.appendText(msg+"\n");// 작은 영역에 텍스트 표시
        }
    }


    // 아이템 이름에 해당하는 슬롯 번호 매핑
    private Integer getSlotNumberForItem(String itemName) {
        switch (itemName) {
            case "쿠":
                return 1;
            case "건구스":
                return 2;
            case "건덕이":
                return 3;
            case "건붕이":
                return 4;
            case "황소의 분노":
                return 5;
            case "일감호의 기적":
                return 6;
            case "스턴건":
                return 7;
            default:
                return null;
        }
    }


    // 아이템 슬롯에 아이템 개수 업데이트만 수행
    public void updateItemSlot(int slotNumber, int itemCount) {
        Label countLabel = getLabelForSlot(slotNumber); // 슬롯 번호에 해당하는 레이블 가져오기
        if (countLabel != null) {
            Platform.runLater(() -> countLabel.setText("x" + itemCount)); // 레이블에 개수 설정
        }
    }


    // 슬롯 번호에 해당하는 레이블 가져오는 헬퍼 메서드
    private Label getLabelForSlot(int slotNumber) {
        switch (slotNumber) {
            case 1:
                return itemSlot1Label;
            case 2:
                return itemSlot2Label;
            case 3:
                return itemSlot3Label;
            case 4:
                return itemSlot4Label;
            case 5:
                return itemSlot5Label;
            case 6:
                return itemSlot6Label;
            case 7:
                return itemSlot7Label;
            default:
                return null;
        }
    }

    private void setupTooltip(Pane pane, String imagePath, String description) {

        Tooltip tooltip = new Tooltip();
        tooltip.setText(description);
        tooltip.setStyle("-fx-font-size: 20px;");
        Image image = new Image(getClass().getResource(imagePath).toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        tooltip.setGraphic(imageView);
        Tooltip.install(pane, tooltip);
    }

    //황소의 분노 현재 수 가져오기
    private int getCurrentItemCount(String itemName) {
        Label countLabel = getLabelForSlot(getSlotNumberForItem(itemName));
        if (countLabel != null) {
            String text = countLabel.getText();
            return Integer.parseInt(text.replace("x", "").trim());
        }
        return 0; // 기본값: 0
    }



    //아이템 클릭 이벤트
    @FXML
    private void useItem(MouseEvent event) {
        Pane clickedPane = (Pane) event.getSource();
        System.out.println("Clicked Pane: " + clickedPane.getId());

        //미소유 아이템 경우 return
        if (!checkoutItemExist(clickedPane)) {
            return;
        }

        switch (clickedPane.getId()) {
            case "itemSlot5":
                //TODO 황소의 분노 아이템 호출
                itemAnger();
                System.out.println("황소의 분노 아이템 사용");
                break;
            case "itemSlot6":
                //TODO 일감호의 기적 아이템 호출
                itemMiracle();
                System.out.println("일감호의 기적 아이템 사용");
                break;
            case "itemSlot7":
                //TODO 스턴건 아이템 호출
                itemStunGun();
                System.out.println("스턴건 아이템 사용");
                break;
            default:
                System.out.println("확인되지 않은 아이템 클릭 이벤트");
        }
    }

    private boolean checkoutItemExist(Pane clickedPane) {
        String labelText = clickedPane.getChildren()
                .stream()
                .filter(node -> node instanceof Label)
                .map(node -> ((Label) node).getText())
                .findFirst()
                .get();

        if (labelText.equals("x0")) {
            Alert alert = new Alert(Alert.AlertType.WARNING); // 경고창(AlertType.WARNING)
            alert.setTitle("아이템 경고");
            alert.setHeaderText("소유하지 않은 아이템입니다.");
            alert.showAndWait(); // 창을 띄우고 사용자의 응답을 기다림
            return false;
        }
        return true;
    }

    public void itemMiracle() {

        if (isMiracleActive) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("이미 일감호의 기적을 사용하였습니다.");
            alert.showAndWait();
            return;
        }

        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("일감호의 기적 사용");
            for (int i = 1; i <= 7; i++) {
                Label countLabel = getLabelForSlot(i);
                countLabel.setDisable(true);
            }
            isMiracleActive=true;
            countArea.appendText("아이템 [일감호의 기적]을 사용하였습니다.\n이번 경매픔을 강제로 낙찰받습니다.\n(단, 황소의 분노가 발동된 경우 유찰됨)\n");
            int currentCount = getCurrentItemCount("일감호의 기적");
            updateItemSlot(4, currentCount - 1);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void itemAnger() {
        if (isAngerActive) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("이미 황소의분노를 사용하였습니다.");
            alert.showAndWait();
            return;
        }

        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("ItemUse;황소의 분노;"+userName);
            System.out.println("ItemUse;황소의 분노;");
            countArea.appendText("아이템 [황소의 분노]를 사용하였습니다.\n이번 경매는 무조건 유찰됩니다.\n");

            // 아이템 갯수 감소 처리
            int currentCount = getCurrentItemCount("황소의 분노");
            updateItemSlot(5, currentCount - 1);
            isAngerActive=true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void itemStunGun() {
        for(Label label:participatingUserNames) {
            System.out.println("스턴건 - 참여자: " + label.getText());
            if(label.getText().equals(userName))
                continue;

            label.setOnMouseClicked(event -> clickTargetUser(event));
            label.setStyle(label.getStyle() + "-fx-background-color: blue;");
            countArea.appendText("아이템 [스턴건]을 사용하였습니다.\n강제불응찰 시킬 유저[파란색]를 클릭해주세요.\n");
            // 아이템 갯수 감소 처리
            int currentCount = getCurrentItemCount("스턴건");
            updateItemSlot(6, currentCount - 1);
        }
    }

    @FXML
    private void clickTargetUser(MouseEvent event) {
        Label clickedLabel = (Label) event.getSource();
        System.out.println("Clicked Lable: " + clickedLabel.getText());

        out.println("스턴건 " + clickedLabel.getText());

        participatingUserNames.remove(clickedLabel);
        clickedLabel.setStyle(clickedLabel.getStyle()+ "-fx-background-color: lightgray;");
        for(Label label:participatingUserNames) {
            label.setOnMouseClicked(null);
            label.setStyle(label.getStyle() + "-fx-background-color: lightblue;");
        }

    }


    // 효과음 재생하는 함수
    private void playEffectSound(String soundFilePath) {
        try {
            Media sound = new Media(new File(soundFilePath).toURI().toString());
            MediaPlayer effectMediaPlayer = new MediaPlayer(sound);
            effectMediaPlayer.play(); // 효과음 재생
        } catch (Exception e) {
            System.err.println("효과음 재생 중 오류 발생: " + e.getMessage());
        }
    }


    //게임 진행 메세지 표시 영역에 메세지를 추가
    private void addTextArea(String msg){
        mainMessageArea.appendText(msg + "\n");
    }


}
