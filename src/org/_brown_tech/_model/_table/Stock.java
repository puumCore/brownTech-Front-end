package org._brown_tech._model._table;

import com.google.gson.Gson;
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

    public StringProperty serial;
    public StringProperty name;
    public StringProperty description;
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

}
