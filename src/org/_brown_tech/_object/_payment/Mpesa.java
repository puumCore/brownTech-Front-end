package org._brown_tech._object._payment;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * @author Mandela
 */
public class Mpesa implements Serializable {

    public static final long serialVersionUID = 3L;

    private String transactionCode;
    private String customerName;
    private String phone;
    private Double amount;

    public Mpesa() {
    }

    public Mpesa(String transactionCode, String customerName, String phone, Double amount) {
        this.transactionCode = transactionCode;
        this.customerName = customerName;
        this.phone = phone;
        this.amount = amount;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this, Mpesa.class);
    }
}
