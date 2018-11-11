package com.aidor.secchargemobile.model;

public class ProfileResponse {

    private String CITY;
    private String EMAIL;
    private String FIRSTNAME;
    private String LASTNAME;
    private String PROVINCE;
    private String ADDRESS1;
    private String ADDRESS2;
    private String POSTALCODE;
    private String CELLPHONE;
    private String COUNTRY;

    public String getCOUNTRY() {
        return COUNTRY;
    }

    public void setCOUNTRY(String COUNTRY) {
        this.COUNTRY = COUNTRY;
    }

    public String getCELLPHONE() {
        return CELLPHONE;
    }

    public void setCELLPHONE(String CELLPHONE) {
        this.CELLPHONE = CELLPHONE;
    }

    public String getPOSTALCODE() {
        return POSTALCODE;
    }

    public void setPOSTALCODE(String POSTALCODE) {
        this.POSTALCODE = POSTALCODE;
    }

    public String getADDRESS1() {
        return ADDRESS1;
    }

    public void setADDRESS1(String ADDRESS1) {
        this.ADDRESS1 = ADDRESS1;
    }

    public String getADDRESS2() {
        return ADDRESS2;
    }

    public void setADDRESS2(String ADDRESS2) {
        this.ADDRESS2 = ADDRESS2;
    }
    public String getCITY() {
        return CITY;
    }
    public void setCITY(String CITY) {
        this.CITY = CITY;
    }
    public String getEMAIL() {
        return EMAIL;
    }
    public void setEMAIL(String EMAIL) {
        this.EMAIL = EMAIL;
    }
    public String getFIRSTNAME() {
        return FIRSTNAME;
    }
    public void setFIRSTNAME(String FIRSTNAME) {
        this.FIRSTNAME = FIRSTNAME;
    }
    public String getLASTNAME() {
        return LASTNAME;
    }
    public void setLASTNAME(String LASTNAME) {
        this.LASTNAME = LASTNAME;
    }
    public String getPROVINCE() {
        return PROVINCE;
    }
    public void setPROVINCE(String PROVINCE) {
        this.PROVINCE = PROVINCE;
    }
}

