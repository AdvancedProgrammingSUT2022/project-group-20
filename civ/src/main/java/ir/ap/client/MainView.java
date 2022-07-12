package ir.ap.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

public class MainView extends View {

    @FXML
    private Label usernameLabel;

    public void initialize() {
        if (currentUsername != null) {
            usernameLabel.setText(currentUsername);
        }
    }

    public void enterGame() {
        try {
            App.setRoot("fxml/launch-game-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enterScoreboard() {
        try {
            App.setRoot("fxml/scoreboard-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enterChat() {
        try {
            App.setRoot("fxml/chat-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}