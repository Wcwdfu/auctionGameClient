package org.example.auctiongameclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.text.Font;

public class AuctionClientApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Font.loadFont(Class.forName("org.example.auctiongameclient.AuctionClientApplication").getResource("/fonts/Hakgyoansim-Byeoljari.ttf").toString(), 16).getFamily();
        System.setProperty("prism.lcdtext", "false");
        FXMLLoader fxmlLoader = new FXMLLoader(AuctionClientApplication.class.getResource("connect-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 880, 600);
        stage.setTitle("경매 게임");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
