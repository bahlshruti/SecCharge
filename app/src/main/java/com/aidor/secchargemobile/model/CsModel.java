package com.aidor.secchargemobile.model;

/**
 * Created by sahajarora1286 on 16-05-2016.
 */
public class CsModel {
    private String id;
    private String siteType;
    private String siteOwner;
    private String address1;
    private String address2;
    private String city;
    private String province;
    private String postalCode;
    private String country;
    //private String siteImage;
    private String longLocation;
    private String latLocation;
    private String level2Price;
    private String fastDCPrice;
    private String priceDate;
    private String sitePhone;
    private String accessTypeTime;
    private String siteNumber;

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSiteType() {
        return siteType;
    }

    public void setSiteType(String siteType) {
        this.siteType = siteType;
    }

    public String getSiteOwner() {
        return siteOwner;
    }

    public void setSiteOwner(String siteOwner) {
        this.siteOwner = siteOwner;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLongLocation() {
        return longLocation;
    }

    public void setLongLocation(String longLocation) {
        this.longLocation = longLocation;
    }

    public String getLatLocation() {
        return latLocation;
    }

    public void setLatLocation(String latLocation) {
        this.latLocation = latLocation;
    }

    public String getLevel2Price() {
        return level2Price;
    }

    public void setLevel2Price(String level2Price) {
        this.level2Price = level2Price;
    }

    public String getFastDCPrice() {
        return fastDCPrice;
    }

    public void setFastDCPrice(String fastDCPrice) {
        this.fastDCPrice = fastDCPrice;
    }

    public String getPriceDate() {
        return priceDate;
    }

    public void setPriceDate(String priceDate) {
        this.priceDate = priceDate;
    }

    public String getSitePhone() {
        return sitePhone;
    }

    public void setSitePhone(String sitePhone) {
        this.sitePhone = sitePhone;
    }

    public String getAccessTypeTime() {
        return accessTypeTime;
    }

    public void setAccessTypeTime(String accessTypeTime) {
        this.accessTypeTime = accessTypeTime;
    }

    public String getSiteNumber() {
        return siteNumber;
    }

    public void setSiteNumber(String siteNumber) {
        this.siteNumber = siteNumber;
    }

    private String usageType;
}
