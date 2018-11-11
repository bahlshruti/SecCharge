package com.aidor.secchargemobile.seccharge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.custom.CustomListForReserveHistory;
import com.aidor.secchargemobile.custom.CustomListForViewReserve;
import com.aidor.secchargemobile.custom.SecCharge;
import com.aidor.secchargemobile.model.Example;
import com.aidor.secchargemobile.model.Reservationdetail;
import com.aidor.secchargemobile.rest.RestClientReservation;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ReservationHistoryActivity extends ActionBarActivity{
    private ListView listMyReservations;
    List<Reservationdetail> reserveDetails;
    String userId;
    SecCharge myApp;
    private TextView tvReservationHistory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApp = (SecCharge) this.getApplicationContext();
        checkNetwork();
        setContentView(R.layout.activity_reservation_history);
        listMyReservations = (ListView) findViewById(R.id.listMyReservations);
        tvReservationHistory = (TextView) findViewById(R.id.tvReservationHistory);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_normal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("UserID", "");
        fetchData();

    }

    @Override
    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences(){
        Activity currActivity = myApp.getCurrentActivity();
        if (this.equals(currActivity))
            myApp.setCurrentActivity(null);
    }
    @Override
    public void onResume(){
        super.onResume();
        myApp.setCurrentActivity(this);
        checkNetwork();
    }

    private void checkNetwork() {
        if (!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ReservationHistoryActivity.this, NoInternetActivity.class).putExtra("activityName", "HomeActivity"));
            finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void fetchData() {
        RestClientReservation.get().getMyReservationHistory(userId, new Callback<Example>() {

            @Override
            public void success(Example details, Response response) {
                reserveDetails = new ArrayList<Reservationdetail>();

                reserveDetails = details.getDetails().getReservationdetails();
                if (reserveDetails.isEmpty()) tvReservationHistory.setText("No Previous Reservations");
                listMyReservations.setAdapter(new CustomListForReserveHistory(ReservationHistoryActivity.this, reserveDetails));
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(ReservationHistoryActivity.this, "failed", Toast.LENGTH_LONG).show();
            }
        });
    }

}

