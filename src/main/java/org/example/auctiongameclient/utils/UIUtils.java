package org.example.auctiongameclient.utils;

import javafx.application.Platform;

public class UIUtils {
    public static void runOnUIThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run(); // UI 스레드에서 바로 실행
        } else {
            Platform.runLater(action); // UI 스레드가 아닌 경우 runLater로 실행
        }
    }
}
