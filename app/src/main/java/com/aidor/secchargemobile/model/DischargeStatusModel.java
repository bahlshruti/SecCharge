package com.aidor.secchargemobile.model;

/**
 * Created by sahajarora1286 on 30-11-2016.
 */

public class DischargeStatusModel {

    private String success;
    private float stateOfCharge, dischargeStateOfCharge;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public float getDischargeStateOfCharge() {
        return dischargeStateOfCharge;
    }

    public void setDischargeStateOfCharge(float dischargeStateOfCharge) {
        this.dischargeStateOfCharge = dischargeStateOfCharge;
    }

    public float getStateOfCharge() {
        return stateOfCharge;
    }

    public void setStateOfCharge(float stateOfCharge) {
        this.stateOfCharge = stateOfCharge;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    private String errorMessage;
}
