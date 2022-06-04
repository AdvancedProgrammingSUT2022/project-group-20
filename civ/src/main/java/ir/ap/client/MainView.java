package ir.ap.client;

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

    public void newGame() {

    }

    public void enterScoreboard() {
        try {
            App.setRoot("fxml/scoreboard-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
