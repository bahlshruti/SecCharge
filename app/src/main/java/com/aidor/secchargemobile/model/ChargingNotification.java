package com.aidor.secchargemobile.model;

/**
 * Created by seccharge on 6/16/2016.
 */
public class ChargingNotification {
    private boolean success;
    private String message;

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
