package com.aidor.secchargemobile.seccharge;

import android.app.Application;

public class PassValueActivity extends Application {

    int id = 0;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }



}
