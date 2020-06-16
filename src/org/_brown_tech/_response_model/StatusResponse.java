package org._brown_tech._response_model;

/**
 * @author Mandela
 */
public enum StatusResponse {

    SUCCESS ("Success"), ERROR ("Error");

    public String status;

    StatusResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
