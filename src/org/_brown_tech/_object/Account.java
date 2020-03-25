package org._brown_tech._object;

import java.io.Serializable;

/**
 * @author Mandela
 */
public class Account implements Serializable {

    public static final long serialVersionUID = 82L;

    public String username = "", firstname = "", surname = "", siri = "", email = "";
    public boolean isAdmin;

    public Account() {
    }

    public Account(String username, String firstname, String surname, String siri, String email, boolean isAdmin) {
        this.username = username;
        this.firstname = firstname;
        this.surname = surname;
        this.siri = siri;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getSiri() {
        return siri;
    }

    public void setSiri(String siri) {
        this.siri = siri;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Override
    public String toString() {
        return "Account{" +
                "username='" + username + '\'' +
                ", firstname='" + firstname + '\'' +
                ", surname='" + surname + '\'' +
                ", siri='" + siri + '\'' +
                ", email='" + email + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}
