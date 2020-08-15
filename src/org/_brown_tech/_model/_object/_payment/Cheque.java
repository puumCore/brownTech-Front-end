package org._brown_tech._model._object._payment;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * @author Mandela aka puumInc
 * @version 1.0.0
 */

public class Cheque implements Serializable {

    public static final long serialVersionUID = 9L;

    private String dateTime;
    private Integer receiptNumber;
    private Integer chequeNumber;
    private String drawerName;
    private String maturityDate;
    private String bank;
    private Double amount;
    private Boolean hasMatured;

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

    public Integer getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(Integer chequeNumber) {
        this.chequeNumber = chequeNumber;
    }

    public String getDrawerName() {
        return drawerName;
    }

    public void setDrawerName(String drawerName) {
        this.drawerName = drawerName;
    }

    public String getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(String maturityDate) {
        this.maturityDate = maturityDate;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public boolean isHasMatured() {
        return hasMatured;
    }

    public void setHasMatured(boolean hasMatured) {
        this.hasMatured = hasMatured;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this, Cheque.class);
    }
}
