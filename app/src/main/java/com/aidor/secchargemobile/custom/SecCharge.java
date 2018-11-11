package com.aidor.secchargemobile.custom;

import android.app.Activity;
import android.app.Application;
import android.support.multidex.MultiDexApplication;

/**
 * Created by sahajarora1286 on 2016-03-11.
 */
public class SecCharge extends MultiDexApplication {
    public void onCreate() {
        super.onCreate();
    }

    private Activity mCurrentActivity = null;
    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }
}
