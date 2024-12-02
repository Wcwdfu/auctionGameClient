package org.example.auctiongameclient;

import javafx.scene.control.TextArea;
import java.io.PrintWriter;

public class AuctionManager {
    private final PrintWriter out;
    private final TextArea messageArea;
    private final TextArea countArea;

    public AuctionManager(PrintWriter out, TextArea messageArea, TextArea countArea) {
        this.out = out;
        this.messageArea = messageArea;
        this.countArea=countArea;
    }

    public void participateInAuction() {
        out.println("응찰");
    }

    public void declineAuction() {
        out.println("불응찰");
    }

    public void placeBid(int bidAmount) {
        out.println("호가 " + bidAmount);
    }
}
