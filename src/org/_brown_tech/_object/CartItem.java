package org._brown_tech._object;

import java.io.Serializable;

/**
 * @author Mandela
 */
public class CartItem implements Serializable {

    public static final long serialVersionUID = 52L;

    public Integer id;
    public String productOrServiceSerial, name;
    public Integer quantityRequested;
    public Double price;
    public Boolean isProduct;

    public CartItem() {
    }

    public CartItem(Integer id, String productOrServiceSerial, String name, Integer quantityRequested, Double price, Boolean isProduct) {
        this.id = id;
        this.productOrServiceSerial = productOrServiceSerial;
        this.name = name;
        this.quantityRequested = quantityRequested;
        this.price = price;
        this.isProduct = isProduct;
    }

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

    public Boolean getProduct() {
        return isProduct;
    }

    public void setProduct(Boolean product) {
        isProduct = product;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", productOrServiceSerial='" + productOrServiceSerial + '\'' +
                ", name='" + name + '\'' +
                ", quantityRequested=" + quantityRequested +
                ", price=" + price +
                ", isProduct=" + isProduct +
                '}';
    }
}
