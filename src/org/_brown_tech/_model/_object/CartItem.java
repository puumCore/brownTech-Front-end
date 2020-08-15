package org._brown_tech._model._object;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * @author Mandela
 */
public class CartItem implements Serializable {

    public static final long serialVersionUID = 2L;

    private Integer id;
    private String productOrServiceSerial;
    private String name;
    private Integer quantityRequested;
    private Double price;
    private Double buyingPrice;
    private boolean isProduct;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProductOrServiceSerial() {
        return productOrServiceSerial;
    }

    public void setProductOrServiceSerial(String productOrServiceSerial) {
        this.productOrServiceSerial = productOrServiceSerial;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantityRequested() {
        return quantityRequested;
    }

    public void setQuantityRequested(Integer quantityRequested) {
        this.quantityRequested = quantityRequested;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(Double buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public Boolean getProduct() {
        return isProduct;
    }

    public void setProduct(Boolean product) {
        isProduct = product;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this, CartItem.class);
    }
}
