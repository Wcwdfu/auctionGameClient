package org.example.auctiongameclient.Controller;

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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import org.example.auctiongameclient.AuctionClientApplication;
import org.example.auctiongameclient.AuctionManager;
import org.example.auctiongameclient.ChatManager;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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

    private BufferedReader in;
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
        String path = new File("src/main/resources/bgms/클로버의 근면.mp3").getAbsolutePath();
        media = new Media(new File(path).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
    }

    @FXML
    private void connectToServer(ActionEvent event) {
        int port = Integer.parseInt(portField.getText());
        userName = nameField.getText().trim();
        alertMessage.setText("안녕하세요");
        if (userName.isEmpty()) {
            alertMessage.setText("이름을 입력하세요.\n");
            return;
        }

        try {
            Socket socket = new Socket(serverAddress, port);
            if(socket.isConnected()) {
                switchToGameScreen(event, socket);
            }
            connectButton.setDisable(true);

        } catch (IOException e) {
            alertMessage.setText("서버에 연결할 수 없습니다: " + e.getMessage() + "\n");
        }
    }

    public void switchToGameScreen(ActionEvent event, Socket socket) throws IOException {
        FXMLLoader loader = new FXMLLoader(AuctionClientApplication.class.getResource("auction-client-view.fxml"));
        try {
            root = loader.load();
            AuctionClientController controller = loader.getController();
            controller.setSocketAndName(socket, userName);


            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            mediaPlayer.stop();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            throw e; // 예외를 다시 던져서 상위 메소드에서 처리할 수 있게 함
        }
    }


}
