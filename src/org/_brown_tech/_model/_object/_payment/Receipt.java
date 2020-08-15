package org._brown_tech._model._object._payment;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * @author Mandela aka puumInc
 * @version 1.0.0
 */

public class Receipt implements Serializable {

    public static final long serialVersionUID = 6L;

    private String dateAndTime;
    private Integer receiptNumber;
    private String serial;
    private Integer quantitySold;
    private Double sellingPrice;
    private Double buyingPrice;
    private Boolean isProduct;


    public String getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public Integer getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(Integer receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public Integer getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(Integer quantitySold) {
        this.quantitySold = quantitySold;
    }

    public Double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Double getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(Double buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public boolean isProduct() {
        return isProduct;
    }

    public void setProduct(boolean product) {
        isProduct = product;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this, Receipt.class);
    }
}
