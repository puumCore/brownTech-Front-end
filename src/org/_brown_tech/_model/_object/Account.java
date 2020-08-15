package org._brown_tech._model._object;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * @author Mandela
 */
public class Account implements Serializable {

    public static final long serialVersionUID = 1L;

    private int rowid;
    private String username;
    private String fname;
    private String surname;
    private String password;
    private String email;
    private boolean isActive;

    public int getRowid() {
        return rowid;
    }

    public void setRowid(int rowid) {
        this.rowid = rowid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this, Account.class);
    }
}
