
package com.aidor.secchargemobile.model;

import java.util.ArrayList;
import java.util.List;

public class Details {

    private List<Reservationdetail> reservationdetails = new ArrayList<Reservationdetail>();
    private String username;
    private String vehicle;

    /**
     * 
     * @return
     *     The reservationdetails
     */
    public List<Reservationdetail> getReservationdetails() {
        return reservationdetails;
    }

    /**
     * 
     * @param reservationdetails
     *     The reservationdetails
     */
    public void setReservationdetails(List<Reservationdetail> reservationdetails) {
        this.reservationdetails = reservationdetails;
    }

    /**
     * 
     * @return
     *     The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * 
     * @param username
     *     The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 
     * @return
     *     The vehicle
     */
    public String getVehicle() {
        return vehicle;
    }

    /**
     * 
     * @param vehicle
     *     The vehicle
     */
    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

}
