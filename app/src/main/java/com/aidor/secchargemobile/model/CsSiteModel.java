package com.aidor.secchargemobile.model;

public class CsSiteModel {
    private String id;
    private String accesstypetime;
    private String address1;
    private String address2;
    private String city;
    private String country;
    private String dcfastprice;
    private String level2price;
    private String postalcode;
    private String province;
    private String sitenumber;
    private String siteowner;
    private String sitephone;
    private String sitetype;
    private String usagetype;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccesstypetime() {
        return accesstypetime;
    }

    public void setAccesstypetime(String accesstypetime) {
        this.accesstypetime = accesstypetime;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDcfastprice() {
        return dcfastprice;
    }

    public void setDcfastprice(String dcfastprice) {
        this.dcfastprice = dcfastprice;
    }

    public String getLevel2price() {
        return level2price;
    }

    public void setLevel2price(String level2price) {
        this.level2price = level2price;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getSitenumber() {
        return sitenumber;
    }

    public void setSitenumber(String sitenumber) {
        this.sitenumber = sitenumber;
    }

    public String getSiteowner() {
        return siteowner;
    }

    public void setSiteowner(String siteowner) {
        this.siteowner = siteowner;
    }

    public String getSitephone() {
        return sitephone;
    }

    public void setSitephone(String sitephone) {
        this.sitephone = sitephone;
    }

    public String getSitetype() {
        return sitetype;
    }

    public void setSitetype(String sitetype) {
        this.sitetype = sitetype;
    }

    public String getUsagetype() {
        return usagetype;
    }

    public void setUsagetype(String usagetype) {
        this.usagetype = usagetype;
    }
     public CsSiteModel(String id, String accesstypetime, String address1, String address2, String city,
                        String country, String dcfastprice, String level2price, String postalcode,
                        String province, String sitenumber, String siteowner, String sitephone,
                        String sitetype, String usagetype){
         this.id = id;
         this.accesstypetime = accesstypetime;
         this.address1 = address1;
         this.address2 = address2;
         this.city = city;
         this.country = country;
         this.dcfastprice =dcfastprice;
         this.level2price = level2price;
         this.postalcode = postalcode;
         this.province = province;
         this.sitenumber =sitenumber;
         this.siteowner =siteowner;
         this.sitephone =sitephone;
         this.sitetype = sitetype;
         this.usagetype = usagetype;
     }
    public CsSiteModel() {

    }
}
