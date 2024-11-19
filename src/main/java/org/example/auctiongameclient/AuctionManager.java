package org.example.auctiongameclient;

import javafx.scene.control.TextArea;
import java.io.PrintWriter;

public class AuctionManager {
    private final PrintWriter out;
    private final TextArea messageArea;

    public AuctionManager(PrintWriter out, TextArea messageArea) {
        this.out = out;
        this.messageArea = messageArea;
    }

    public void participateInAuction() {
        out.println("참가");
        messageArea.appendText("당신은 경매에 응찰 했습니다.\n");
    }

    public void declineAuction() {
        out.println("불참여");
        messageArea.appendText("당신은 경매에 불응찰 했습니다.\n");
    }

    public void placeBid(int bidAmount) {
        out.println("호가 " + bidAmount);
//        messageArea.appendText("입찰 요청을 보냈습니다: " + bidAmount + "원\n");
    }
}
