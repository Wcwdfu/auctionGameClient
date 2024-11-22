package org.example.auctiongameclient;

import javafx.scene.control.TextArea;
import java.io.PrintWriter;

public class AuctionManager {
    private final PrintWriter out;
    private final TextArea messageArea;
    private final TextArea chatArea;

    public AuctionManager(PrintWriter out, TextArea messageArea, TextArea chatArea) {
        this.out = out;
        this.messageArea = messageArea;
        this.chatArea = chatArea;
    }

    public void participateInAuction() {
        out.println("참가");
        chatArea.appendText("당신은 경매에 응찰 했습니다.\n");
    }

    public void declineAuction() {
        out.println("불참여");
        chatArea.appendText("당신은 경매에 불응찰 했습니다.\n");
    }

    public void placeBid(int bidAmount) {
        out.println("호가 " + bidAmount);
    }
}
