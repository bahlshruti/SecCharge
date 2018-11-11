package com.aidor.secchargemobile.model;

public class StartChargingModel {
    private String reservationid;
    private String siteid;
    private String username;
    private String vehicle;
    private String reservedate;
    private String authentication;
    private String proceed;

    public String getProceed(){
        return this.proceed;
    }

    public String getNoReservation() {
        return noReservation;
    }

    public void setNoReservation(String noReservation) {
        this.noReservation = noReservation;
    }

    private String noReservation;

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    private String contract;

    public String getVehiclevin() {
        return vehiclevin;
    }

    public void setVehiclevin(String vehiclevin) {
        this.vehiclevin = vehiclevin;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    private String vehiclevin;

    public String getReservestarttime() {
        return reservestarttime;
    }

    public void setReservestarttime(String reservestarttime) {
        this.reservestarttime = reservestarttime;
    }

    public String getReservedate() {
        return reservedate;
    }

    public void setReservedate(String reservedate) {
        this.reservedate = reservedate;
    }

    private String reservestarttime;

    public String getReservationid() {
        return reservationid;
    }

    public void setReservationid(String reservationid) {
        this.reservationid = reservationid;
    }

    public String getSiteid() {
        return siteid;
    }

    public void setSiteid(String siteid) {
        this.siteid = siteid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }
}
