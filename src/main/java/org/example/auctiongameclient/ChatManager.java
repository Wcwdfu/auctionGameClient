package org.example.auctiongameclient;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.io.BufferedReader;
import java.io.PrintWriter;

public class ChatManager {
    private final PrintWriter out;
    private final TextArea chatArea;
    private final TextField chatInputField;

    public ChatManager(PrintWriter out, TextArea chatArea, TextField chatInputField) {
        this.out = out;
        this.chatArea = chatArea;
        this.chatInputField = chatInputField;

        chatInputField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                sendChatMessage();
            }
        });
    }

    public void sendChatMessage() {
        String message = chatInputField.getText().trim();
        if (!message.isEmpty()) {
            out.println("채팅 " + message);
            chatInputField.clear();
        }
    }

    public void receiveChatMessage(String message) {
        Platform.runLater(() -> chatArea.appendText(message + "\n"));
    }
}

