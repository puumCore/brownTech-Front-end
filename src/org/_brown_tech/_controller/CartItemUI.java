package org._brown_tech._controller;

import animatefx.animation.SlideOutLeft;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org._brown_tech._custom.Brain;
import org._brown_tech._model._object.CartItem;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Mandela
 */
public class CartItemUI extends Brain implements Initializable {

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
            if (i_am_sure_of_it("delete ".concat(nameLbl.getText()).concat(" from cart"))) {
                if (delete_cart_item(KEY)) {
                    Controller.cartItems.remove(KEY);
                    final Node currentNode = nameLbl.getParent().getParent();
                    final VBox motherBox = (VBox) currentNode.getParent();
                    new SlideOutLeft(currentNode).play();
                    Platform.runLater(() -> motherBox.getChildren().remove(currentNode));
                    Controller.anItemHasBeenRemovedFromCart = true;
                    success_notification("Item has been removed from cart!").show();
                } else {
                    error_message("Failed!",
                            "The item was not successfully removed from cart").show();
                }
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        KEY = row_id;
        final CartItem cartItem = Controller.cartItems.get(KEY);
        nameLbl.setText(cartItem.getName());
        quantityLbl.setText(String.format("%d", cartItem.getQuantityRequested()));
        costLbl.setText("Ksh ".concat(String.format("%,.1f", cartItem.getPrice())));
    }
}
