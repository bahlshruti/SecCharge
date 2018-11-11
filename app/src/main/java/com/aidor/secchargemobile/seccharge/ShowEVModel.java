package com.aidor.secchargemobile.seccharge;

/**
 * Created by sahilsiwach on 10/29/2015.
 */
public class ShowEVModel {

    public static ShowEVModel sm = new ShowEVModel();

    public static ShowEVModel getInstance() {
        return sm;
    }

    public String getVehicle_ID() {
        return vehicle_ID;
    }

    public void setVehicle_ID(String vehicle_ID) {
        this.vehicle_ID = vehicle_ID;
    }

    String vehicle_ID;

    String car_Make;
    String car_Year;
    String car_Model;


    public String getCar_Model() {
        return car_Model;
    }

    public void setCar_Model(String car_Model) {
        this.car_Model = car_Model;
    }

    public String getCar_Year() {
        return car_Year;
    }

    public void setCar_Year(String car_Year) {
        this.car_Year = car_Year;
    }

    public String getCar_Make() {
        return car_Make;
    }

    public void setCar_Make(String car_Make) {
        this.car_Make = car_Make;
    }

}
