package org._brown_tech._model._object._payment;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * @author Mandela aka puumInc
 * @version 1.0.0
 */

public class Cash implements Serializable {

    public static final long serialVersionUID = 8L;

    private String dateTime;
    private Integer receiptNumber;
    private Double amount;

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Integer getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(Integer receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this, Cash.class);
    }

}
