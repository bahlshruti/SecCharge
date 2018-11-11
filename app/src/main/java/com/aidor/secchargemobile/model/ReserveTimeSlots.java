package com.aidor.secchargemobile.model;

/**
 * Created by Nikhil on 01/03/2017.
 * Model for mapping response JSON data from web service call to this class.
 */
public class ReserveTimeSlots {

    private String country;
    private String level2price;
    private String province;
    private String siteowner;
    private String city;
    private String address1;
    private String postalcode;
    private String vehiclemake;
    private String vehiclemodel;
    private Integer id;
    private String reservedate;
    private String portlevel;
    private ReserveView reserveView;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLevel2price() {
        return level2price;
    }

    public void setLevel2price(String level2price) {
        this.level2price = level2price;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getSiteowner() {
        return siteowner;
    }

    public void setSiteowner(String siteowner) {
        this.siteowner = siteowner;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public String getVehiclemake() {
        return vehiclemake;
    }

    public void setVehiclemake(String vehiclemake) {
        this.vehiclemake = vehiclemake;
    }

    public String getVehiclemodel() {
        return vehiclemodel;
    }

    public void setVehiclemodel(String vehiclemodel) {
        this.vehiclemodel = vehiclemodel;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReservedate() {
        return reservedate;
    }

    public void setReservedate(String reservedate) {
        this.reservedate = reservedate;
    }

    public String getPortlevel() {
        return portlevel;
    }

    public void setPortlevel(String portlevel) {
        this.portlevel = portlevel;
    }

    public ReserveView getReserveView() {
        return reserveView;
    }

    public void setReserveView(ReserveView reserveView) {
        this.reserveView = reserveView;
    }
}
