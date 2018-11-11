package com.aidor.secchargemobile.model;


public class BatteryStatusModel {

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    private Long transactionId;

    private String myReservation, reservationId, stateOfCharge, success, usageType;

    public String getMyReservation() {
        return myReservation;
    }

    public void setMyReservation(String myReservation) {
        this.myReservation = myReservation;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getStateOfCharge() {
        return stateOfCharge;
    }

    public void setStateOfCharge(String stateOfCharge) {
        this.stateOfCharge = stateOfCharge;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }
}
