package org._brown_tech._outsourced;

import org._brown_tech.Main;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * @author Muriithi_Mandela
 */

public class PasswordDialog extends Dialog<String> {

    private PasswordField passwordField;

    public PasswordDialog() {
        initOwner(Main.stage);
        setTitle("Edit Security Watchdog");
        setContentText("Your original password is required to continue");
        setHeaderText("Type your current password below");
        ButtonType passwordButtonType = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(passwordButtonType, ButtonType.CANCEL);

        passwordField = new PasswordField();
        passwordField.setPromptText("Original password");

        HBox hBox = new HBox();
        hBox.getChildren().add(passwordField);
        hBox.setPadding(new Insets(20));
        HBox.setHgrow(passwordField, Priority.ALWAYS);

        getDialogPane().setContent(hBox);

        Platform.runLater(()-> passwordField.requestFocus());

        setResultConverter(dialogButton -> {
            if (dialogButton == passwordButtonType) {
                return passwordField.getText();
            }
            return null;
        });
    }

    public PasswordField getPasswordField() {
        return  passwordField;
    }
}
