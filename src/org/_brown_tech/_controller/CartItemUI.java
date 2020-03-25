package org._brown_tech._controller;

import animatefx.animation.SlideOutLeft;
import org._brown_tech._custom.Issues;
import org._brown_tech._object.CartItem;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Mandela
 */
public class CartItemUI extends Issues implements Initializable {

    public static int row_id;
    public int KEY;

    @FXML
    private Label nameLbl;

    @FXML
    private Label quantityLbl;

    @FXML
    private Label costLbl;

    @FXML
    private void delete_item(ActionEvent event) {
        if (event != null) {
            if (i_am_sure_of_it(" delete the ".concat(nameLbl.getText()).concat(" from cart "))) {
                if (delete_cart_item(KEY)) {
                    MainUI.cartItems.remove(KEY);
                    final Node currentNode = nameLbl.getParent().getParent();
                    final VBox motherBox = (VBox) currentNode.getParent();
                    new SlideOutLeft(currentNode).play();
                    Platform.runLater(() -> motherBox.getChildren().remove(currentNode));
                    MainUI.cartItems.remove(KEY);
                    success_notification("Item has been removed from cart!").show();
                } else {
                    error_message("Failed!", "The item was not successfully removed from cart").show();
                }
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        KEY = row_id;
        final CartItem cartItem = MainUI.cartItems.get(KEY);
        nameLbl.setText(cartItem.getName());
        quantityLbl.setText(String.format("%d", cartItem.getQuantityRequested()));
        costLbl.setText("Ksh ".concat(String.format("%,.1f", cartItem.getPrice())));
    }
}