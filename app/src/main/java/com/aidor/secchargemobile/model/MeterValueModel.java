package com.aidor.secchargemobile.model;

/**
 * Created by sahajarora1286 on 17-05-2016.
 */
public class MeterValueModel {
    private String variable;

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private String value;

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    private Long transactionId;

}
