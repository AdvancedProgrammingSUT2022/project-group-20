package ir.ap.client.components.menu;

import com.google.gson.JsonObject;
import ir.ap.client.View;
import ir.ap.client.components.UserSerializer;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.util.*;

public class InvitePlayersView extends View {
    @FXML
    private TextField invitePlayer;
    @FXML
    private Label messageLabel;
    @FXML
    private TableView<UserSerializer> invitedPlayersTable;

    private ArrayList<UserSerializer> invitedUsers;

    {
        invitedUsers = new ArrayList<>();
    }

    public void initialize() {
        initializeTable();
        success(messageLabel, "You can invite up to 7 players to play with you!");
    }

    private void initializeTable() {
        invitedPlayersTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        invitedPlayersTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("username"));
        invitedPlayersTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("accepted"));
    }

    public ArrayList<UserSerializer> getInvitedUsers() {
        return invitedUsers;
    }

    public void invite() {
        String username = invitePlayer.getText();
        JsonObject response = USER_CONTROLLER.inviteUser(currentUsername, username);
        if (responseOk(response)) {
            success(messageLabel, getField(response, "msg", String.class));
            addInvitedUser(username);
        } else {
            error(messageLabel, getField(response, "msg", String.class));
        }
    }

    public void addInvitedUser(String username, boolean toMsg) {
        if (hasInvitedUser(username)) {
            error(messageLabel, "Chand bar invite mikoni?");
            return;
        }
        UserSerializer userSerializer = new UserSerializer(username, true);
        invitedUsers.add(userSerializer);
        invitedPlayersTable.getItems().add(userSerializer);
        if (toMsg)
            success(messageLabel, "User invitation sent successfully");
    }

    public void addInvitedUser(String username) {
        addInvitedUser(username, true);
    }

    private boolean hasInvitedUser(UserSerializer userSerializer) {
        return invitedUsers.contains(userSerializer);
    }

    private boolean hasInvitedUser(String username) {
        for (UserSerializer userSerializer : invitedUsers) {
            if (userSerializer.getUsername().equals(username))
                return true;
        }
        return false;
    }

    private void removeInvitedUser(UserSerializer userSerializer) {
        invitedUsers.remove(userSerializer);
        invitedPlayersTable.getItems().remove(userSerializer);
    }

    public void removeSelectedPlayers() {
        ArrayList<UserSerializer> selectedUsers = new ArrayList<>(invitedPlayersTable.getSelectionModel().getSelectedItems());
        invitedPlayersTable.getSelectionModel().clearSelection();
        if (selectedUsers.isEmpty()) {
            error(messageLabel, "No users selected");
            return;
        }
        for (UserSerializer userSerializer : selectedUsers) {
            if (userSerializer.getUsername().equals(currentUsername)) {
                error(messageLabel, "You should be in game!");
                return;
            }
            removeInvitedUser(userSerializer);
        }
        success(messageLabel, "Selected Users removed successfully");
    }

    private void error(Label messageLabel, String msg) {
        messageLabel.setText(msg);
        messageLabel.setTextFill(Color.RED);
    }

    private void success(Label messageLabel, String msg) {
        messageLabel.setText(msg);
        messageLabel.setTextFill(Color.GREEN);
    }
}