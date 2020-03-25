package org._brown_tech._table_object_model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.ImageView;

import java.io.Serializable;

/**
 * @author Mandela
 */
public class Stock extends RecursiveTreeObject<Stock> implements Serializable {

    public static final long serialVersionUID = 82L;

    public StringProperty serial, name, description;
    public SimpleObjectProperty<ImageView> imageview;
    public StringProperty quantity;
    public StringProperty markedPrice;
    public StringProperty status;


    public Stock(String serial, String name, String description, ImageView imageview, String quantity, String markedPrice, String status) {
        this.serial = new SimpleStringProperty(serial);
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.imageview = new SimpleObjectProperty<>(imageview);
        this.quantity = new SimpleStringProperty(quantity);
        this.markedPrice = new SimpleStringProperty(markedPrice);
        this.status = new SimpleStringProperty(status);
    }

    public String getSerial() {
        return serial.get();
    }

    public StringProperty serialProperty() {
        return serial;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public ImageView getImageview() {
        return imageview.get();
    }

    public SimpleObjectProperty<ImageView> imageviewProperty() {
        return imageview;
    }

    public String getQuantity() {
        return quantity.get();
    }

    public StringProperty quantityProperty() {
        return quantity;
    }

    public String getMarkedPrice() {
        return markedPrice.get();
    }

    public StringProperty markedPriceProperty() {
        return markedPrice;
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "serial=" + serial +
                ", name=" + name +
                ", description=" + description +
                ", imageview=" + imageview +
                ", quantity=" + quantity +
                ", markedPrice=" + markedPrice +
                ", status=" + status +
                '}';
    }
}
