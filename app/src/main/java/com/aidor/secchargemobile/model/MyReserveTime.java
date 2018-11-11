package com.aidor.secchargemobile.model;

/**
 * Created by Nikhil on 01/03/2017.
 * Model for mapping response JSON data from web service call to this class.
 */
public class MyReserveTime {

    private String slotTime;
    private Boolean disabled;

    public String getSlotTime() {
        return slotTime;
    }

    public void setSlotTime(String slotTime) {
        this.slotTime = slotTime;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }
}
