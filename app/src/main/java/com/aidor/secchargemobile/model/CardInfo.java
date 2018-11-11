package com.aidor.secchargemobile.model;

/**
 * Created by Nikhil on 02/03/2017.
 * Model for mapping response JSON data from web service call to this class.
 */
public class CardInfo {

    private String cardId;
    private String last4Digits;
    private String stripteUserID;
    private String cardBrand;
    private int exp_month;
    private int exp_year;
    private String cvc;

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getLast4Digits() {
        return last4Digits;
    }

    public void setLast4Digits(String last4Digits) {
        this.last4Digits = last4Digits;
    }

    public String getStripteUserID() {
        return stripteUserID;
    }

    public void setStripteUserID(String stripteUserID) {
        this.stripteUserID = stripteUserID;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }

    public int getExp_month() {
        return exp_month;
    }

    public void setExp_month(int exp_month) {
        this.exp_month = exp_month;
    }

    public int getExp_year() {
        return exp_year;
    }

    public void setExp_year(int exp_year) {
        this.exp_year = exp_year;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }
}
