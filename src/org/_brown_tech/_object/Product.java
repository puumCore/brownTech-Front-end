package org._brown_tech._object;

import com.google.gson.Gson;
import javafx.scene.image.Image;

import java.io.Serializable;

/**
 * @author Mandela
 */
public class Product implements Serializable {

    public static final long serialVersionUID = 42L;

    public String serial, name, description;
    public Integer starCount, stockQuantity;
    public Double markedPrice, buyingPrice;
    public Image itemImage;
    public Boolean isAvailable;

    public Product() {
    }

    public Product(String serial, String name, String description, Integer starCount, Integer stockQuantity, Double markedPrice, Double buyingPrice, Image itemImage, Boolean isAvailable) {
        this.serial = serial;
        this.name = name;
        this.description = description;
        this.starCount = starCount;
        this.stockQuantity = stockQuantity;
        this.markedPrice = markedPrice;
        this.buyingPrice = buyingPrice;
        this.itemImage = itemImage;
        this.isAvailable = isAvailable;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStarCount() {
        return starCount;
    }

    public void setStarCount(Integer starCount) {
        this.starCount = starCount;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Double getMarkedPrice() {
        return markedPrice;
    }

    public void setMarkedPrice(Double markedPrice) {
        this.markedPrice = markedPrice;
    }

    public Double getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(Double buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public Image getItemImage() {
        return itemImage;
    }

    public void setItemImage(Image itemImage) {
        this.itemImage = itemImage;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this, Product.class);
    }
}
