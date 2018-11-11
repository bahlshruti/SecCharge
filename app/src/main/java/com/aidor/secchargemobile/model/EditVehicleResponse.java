package com.aidor.secchargemobile.model;


public class EditVehicleResponse {

    private Integer ID;
    private String MAKE;
    private String MODEL;
    private String PLATENUMBER;
    private String VEHICLEVIN;
    private String YEAR;

    public Integer getID() {
        return ID;
    }
    public void setID(Integer ID) {
        this.ID = ID;
    }
    public String getMAKE() {
        return MAKE;
    }
    public void setMAKE(String MAKE) {
        this.MAKE = MAKE;
    }
    public String getMODEL() {
        return MODEL;
    }
    public void setMODEL(String MODEL) {
        this.MODEL = MODEL;
    }
    public String getPLATENUMBER() {
        return PLATENUMBER;
    }
    public void setPLATENUMBER(String PLATENUMBER) {
        this.PLATENUMBER = PLATENUMBER;
    }
    public String getVEHICLEVIN() {
        return VEHICLEVIN;
    }
    public void setVEHICLEVIN(String VEHICLEVIN) {
        this.VEHICLEVIN = VEHICLEVIN;
    }
    public String getYEAR() {
        return YEAR;
    }
    public void setYEAR(String YEAR) {
        this.YEAR = YEAR;
    }

}
