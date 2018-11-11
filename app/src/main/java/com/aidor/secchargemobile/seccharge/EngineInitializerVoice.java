package com.aidor.secchargemobile.seccharge;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.custom.SecCharge;

/**
 * Created by Nikhil.
 * Class for initializing TTS Engine and getting the current location of user.
 */
public class EngineInitializerVoice extends AppCompatActivity {

    TTSManager myTTSManager = null; // TTS Manager instance

    private SecCharge myApp;

    private String EngineInitializer_TAG = "EngineInitializer"; // Tag for logging

    GPSTracker currentLocationTracker; // For getting current location
    double latitude; // Current latitude of user
    double longitude; // Current longitude of user

    ProgressDialog pDialog; // Progress bar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engine_initializer_voice);

        // Initializing TTS Engine
        myTTSManager = new TTSManager();
        myTTSManager.initializeTTSEngine(this);

        myApp = (SecCharge) this.getApplicationContext();
        checkNetwork();

        // For keeping the lights on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Async Task for getting current location
        new CurrentLocationAsync(EngineInitializerVoice.this).execute();
    }

    /**
     * Method for going to address selector activity with current location data.
     */
    public void goToAddressSelector() {
        Intent intent = new Intent(this, AddressSelectorVoice.class);
        Bundle bundle = new Bundle();
        bundle.putDouble("Latitude", latitude);
        bundle.putDouble("Longitude", longitude);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * Async Task for getting current location of user.
     */
    private class CurrentLocationAsync extends AsyncTask<Void, Void, Void> {

        protected CurrentLocationAsync(EngineInitializerVoice activity){
            pDialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Please Wait, Initializing necessary engines.");
            pDialog.show();
        }


        @Override
        protected Void doInBackground(Void... params) {
            Log.i(EngineInitializer_TAG, "Thread name outside async task is "
                    + Thread.currentThread().getName());
            EngineInitializerVoice.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(EngineInitializer_TAG, "Thread name inside async task is "
                            + Thread.currentThread().getName());

                    // Getting current location of user
                    currentLocationTracker = new GPSTracker(EngineInitializerVoice.this);
                    if (currentLocationTracker.canGetLoaction()) {
                        latitude = currentLocationTracker.getLattitude();
                        longitude = currentLocationTracker.getLongitide();
                        Log.i(EngineInitializer_TAG, "Control coming here to get the values of " +
                                "latitude and longitude.");
                    }

                    Log.i(EngineInitializer_TAG, "Received value of Latitude is - " +latitude);
                    Log.i(EngineInitializer_TAG, "Received value of Longitude is - " +longitude);
                }
            });

            try {
                Log.i(EngineInitializer_TAG, "Thread which I am trying here is "
                        + Thread.currentThread().getName());
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.i(EngineInitializer_TAG, "Thread interrupted.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i(EngineInitializer_TAG, "Now executing post execute one.");
            Log.i(EngineInitializer_TAG, "Thread inside post execute is - "
                    + Thread.currentThread().getName());

            if (pDialog.isShowing()) pDialog.dismiss();
            goToAddressSelector(); // On success
        }
    }

    /**
     * Method for checking the netowrk.
     */
    private void checkNetwork() {
        if (!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), "No Internet Connection!",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(EngineInitializerVoice.this, NoInternetActivity.class));
            finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Method for handling backpress button by user. This will shutdown the TTS Engine and control
     * will go back to the previous activity.
     */
    @Override
    public void onBackPressed() {
        myTTSManager.shutdown();
        Log.i(EngineInitializer_TAG, "Back pressed. TTS object destroyed.");
        super.onBackPressed();
    }
}
