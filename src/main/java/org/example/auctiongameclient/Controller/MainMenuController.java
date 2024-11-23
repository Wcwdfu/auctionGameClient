package org.example.auctiongameclient.Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import org.example.auctiongameclient.AuctionClientApplication;
import org.example.auctiongameclient.AuctionManager;
import org.example.auctiongameclient.ChatManager;
import org.example.auctiongameclient.MediaPlayerManager;
import org.example.auctiongameclient.domain.User;
import org.example.auctiongameclient.domain.UserFactory;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


public class MainMenuController {

    @FXML
    private Label alertMessage;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField portField;
    @FXML
    private TextField nameField;
    @FXML
    private Button connectButton;

    private Scanner in;
    private PrintWriter out;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private final String serverAddress = "localhost";

    private Stage stage;
    private Scene scene;
    private Parent root;

    String userName;

    @FXML
    private MediaView mediaView; //음악
    MediaPlayer mediaPlayer;
    Media media;

    public void initialize() {
        String path = new File("src/main/resources/bgms/Billiards - Wii Play.mp3").getAbsolutePath();
        media = new Media(new File(path).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        MediaPlayerManager.setMediaPlayer(mediaPlayer);
        mediaView.setMediaPlayer(mediaPlayer);
        MediaPlayerManager.getMediaPlayer().setAutoPlay(true);
        MediaPlayerManager.getMediaPlayer().setCycleCount(MediaPlayer.INDEFINITE);
    }

    @FXML
    private void connectToServer(ActionEvent event) {
        int port = Integer.parseInt(portField.getText());
        userName = nameField.getText().trim();
        if (userName.isEmpty() || portField.getText().isEmpty()) {
            alertMessage.setText("포트번호와 이름을 모두 입력하세요.\n");
            return;
        }

//        try {
//            Socket socket = new Socket(serverAddress, port);
//            if(socket.isConnected()) {
//                switchToGameScreen(event, socket);
//            }
//            connectButton.setDisable(true);
//
//        } catch (IOException e) {
//            alertMessage.setText("서버에 연결할 수 없습니다: " + e.getMessage() + "\n");
//        }

        try {
            Socket socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new Scanner(socket.getInputStream());

            out.println(userName);

            UserFactory.initialize(new User(userName),socket);

            switchToWaitingRoom(in);

        } catch (IOException e) {
            alertMessage.setText(e.getMessage());
        }
    }

    public void switchToWaitingRoom(Scanner scanner) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/auctiongameclient/waiting-room-view.fxml"));
            Parent root = loader.load();
            WaitingRoomController waitingRoomController = loader.getController();

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

//    while (scanner.hasNextLine()) {
//        String input = scanner.nextLine();
//        Platform.runLater(() -> {
//            if(input.startsWith("Matching;")){
//                String remainingText = input.substring("Matching;".length());
//                String[] userNames = remainingText.split(",");
//                waitingRoomController.modifyView(userNames);
//            }
//            else if(input.startsWith("MatchingFinished;")){
//                String remainingText = input.substring("MatchingFinished;".length());
//                int count= Integer.parseInt(remainingText);
//                waitingRoomController.countDownView(count);
//            }
//            System.out.println("User Input: " + input);
//            // 필요한 경우 추가 로직 수행
//        });
//    }

//    public void switchToGameScreen(ActionEvent event, Socket socket) throws IOException {
//        FXMLLoader loader = new FXMLLoader(AuctionClientApplication.class.getResource("auction-client-view.fxml"));
//        try {
//            root = loader.load();
//            AuctionClientController controller = loader.getController();
//            controller.setSocketAndName(socket, userName);
//
//
//            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
//            scene = new Scene(root);
//            stage.setScene(scene);
//            mediaPlayer.stop();
//            stage.show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw e; // 예외를 다시 던져서 상위 메소드에서 처리할 수 있게 함
//        }
//    }


}
