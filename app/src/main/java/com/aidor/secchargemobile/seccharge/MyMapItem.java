package com.aidor.secchargemobile.seccharge;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyMapItem implements ClusterItem {

    private LatLng mPosition;
    private String mSnippet;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;

    public MyMapItem(double lat, double lng, String id) {
        mPosition = new LatLng(lat,lng);
       // this.mSnippet = id;
        this.id = id;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getSnippet(){
        return mSnippet;
    }

    public void setPosition(LatLng position) {
        this.mPosition = position;
    }
}
