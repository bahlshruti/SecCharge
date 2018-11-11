package com.aidor.secchargemobile.model;

import java.util.List;

/**
 * Created by Nikhil on 01/03/2017.
 * Model for mapping response JSON data from web service call to this class.
 */
public class ReserveView {

    List<String> reserveTimeList;

    List<MyReserveTime> myTimeList;

    Long myReservationId;
    Long myEditingReservationId;
    String teststr;

    public List<String> getReserveTimeList() {
        return reserveTimeList;
    }

    public void setReserveTimeList(List<String> reserveTimeList) {
        this.reserveTimeList = reserveTimeList;
    }

    public List<MyReserveTime> getMyTimeList() {
        return myTimeList;
    }

    public void setMyTimeList(List<MyReserveTime> myTimeList) {
        this.myTimeList = myTimeList;
    }

    public Long getMyReservationId() {
        return myReservationId;
    }

    public void setMyReservationId(Long myReservationId) {
        this.myReservationId = myReservationId;
    }

    public Long getMyEditingReservationId() {
        return myEditingReservationId;
    }

    public void setMyEditingReservationId(Long myEditingReservationId) {
        this.myEditingReservationId = myEditingReservationId;
    }

    public String getTeststr() {
        return teststr;
    }

    public void setTeststr(String teststr) {
        this.teststr = teststr;
    }
}
