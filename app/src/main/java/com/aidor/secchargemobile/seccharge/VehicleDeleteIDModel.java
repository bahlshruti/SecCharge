package com.aidor.secchargemobile.seccharge;

/**
 * Created by sahilsiwach on 11/5/2015.
 */
public class VehicleDeleteIDModel {

    public static VehicleDeleteIDModel vm = new VehicleDeleteIDModel();

    public static VehicleDeleteIDModel getInstance(){
        return vm;
    }

    public String getVID() {
        return VID;
    }

    public void setVID(String VID) {
        this.VID = VID;
    }
    String VID;

    public String getListRowId() {
        return listRowId;
    }

    public void setListRowId(String listRowId) {
        this.listRowId = listRowId;
    }

    String listRowId;

}
