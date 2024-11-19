module org.example.auctiongameclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires org.kordamp.bootstrapfx.core;

    opens org.example.auctiongameclient to javafx.fxml;
    exports org.example.auctiongameclient;
    exports org.example.auctiongameclient.Controller;
    opens org.example.auctiongameclient.Controller to javafx.fxml;
}