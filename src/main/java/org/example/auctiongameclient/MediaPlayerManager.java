package org.example.auctiongameclient;

import javafx.application.Platform;
import javafx.scene.media.MediaPlayer;

public class MediaPlayerManager {
    private static MediaPlayer mediaPlayer;

    public static void setMediaPlayer(MediaPlayer player) {
        if (mediaPlayer != null) {
            mediaPlayer.stop(); // 기존 MediaPlayer 중지
        }
        mediaPlayer = player;
    }

    // MediaPlayer 가져오기
    public static MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    // MediaPlayer 정지
    public static void stopMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}
