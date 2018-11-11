package com.aidor.secchargemobile.seccharge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;


public class BatteryStatusActivity extends Activity {

    TextView batteryStatusReservationId,batteryStatusSiteId,batteryStatusUsername, batteryStatusVehicle,batteryStatusSoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkNetwork();
        setContentView(R.layout.battery_status_layout);

        batteryStatusReservationId = (TextView) findViewById(R.id.batteryStatusReservationId);
        batteryStatusSiteId =(TextView) findViewById(R.id.batteryStatusSiteId);
        batteryStatusUsername = (TextView) findViewById(R.id.batteryStatusUsername);
        batteryStatusVehicle = (TextView) findViewById(R.id.batteryStatusVechile);
        batteryStatusSoc = (TextView) findViewById(R.id.batteryStatusSoc);
    }


    @Override
    public void onResume(){
        super.onResume();
        checkNetwork();
    }

    private void checkNetwork() {
        if (!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(BatteryStatusActivity.this, NoInternetActivity.class));
            finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
