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
import com.aidor.secchargemobile.custom.CustomListForViewReserve;
import com.aidor.secchargemobile.custom.SecCharge;
import com.aidor.secchargemobile.model.Example;
import com.aidor.secchargemobile.model.Reservationdetail;
import com.aidor.secchargemobile.rest.RestClientReservation;
import com.google.maps.android.MarkerManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MyReservationsActivity extends ActionBarActivity {
    private ListView listMyReservations;
    List<Reservationdetail> reserveDetails;
    String userId;
    SecCharge myApp;
    private TextView tvReservations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApp = (SecCharge) this.getApplicationContext();
        checkNetwork();
        setContentView(R.layout.activity_my_reservations);
        tvReservations = (TextView) findViewById(R.id.tvReservations);
        listMyReservations = (ListView) findViewById(R.id.listMyReservations);

        /*
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_normal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        */
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("UserID", "");
        fetchData();

    }

    public void onMakeNewReservationClicked(View view){
        Toast.makeText(getApplicationContext(), "Please choose a charging station", Toast.LENGTH_LONG).show();
        startActivity(new Intent(MyReservationsActivity.this, HomeActivity.class));
        finish();
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
            startActivity(new Intent(MyReservationsActivity.this, NoInternetActivity.class).putExtra("activityName", "HomeActivity"));
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
        RestClientReservation.get().getMyReservation(userId, new Callback<Example>() {

            @Override
            public void success(Example details, Response response) {
                reserveDetails = new ArrayList<Reservationdetail>();


                reserveDetails = details.getDetails().getReservationdetails();

                if (reserveDetails.isEmpty()) tvReservations.setText("No Reservations");
                Collections.sort(reserveDetails, new Comparator<Reservationdetail>() {
                    @Override
                    public int compare(Reservationdetail lhs, Reservationdetail rhs) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        SimpleDateFormat stf = new SimpleDateFormat("HH:mm");
                        Date rhsDate = null, lhsDate = null;
                        try {
                            rhsDate = sdf.parse(rhs.getDate() + " " + rhs.getStarttime());
                            lhsDate = sdf.parse(lhs.getDate() + " " + lhs.getStarttime());
                        } catch (Exception e){
                            e.printStackTrace();
                        }
/*
                        Date rhsTime = null, lhsTime = null;
                        try{
                            rhsTime = stf.parse(rhs.getStarttime());
                            lhsTime = stf.parse(lhs.getStarttime());
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        lhsDate.setTime(lhsTime.getTime());
                        rhsDate.setTime(rhsTime.getTime());
            */
                        if (lhsDate.before(rhsDate)) return -1;
                        else if (rhsDate.before(lhsDate)) return 1;
                        else return 0;
                        // return lhsDate.compareTo(rhsDate);
                    }
                });
                listMyReservations.setAdapter(new CustomListForViewReserve(MyReservationsActivity.this, reserveDetails));
                listMyReservations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Reservationdetail reservationdetail = reserveDetails.get(position);
                        Intent i = new Intent(MyReservationsActivity.this, ReservationEditDeleteActivity.class);
                        i.putExtra("date", reservationdetail.getDate());
                        i.putExtra("country", reservationdetail.getCountry());
                        i.putExtra("city", reservationdetail.getCity());
                        i.putExtra("csId", String.valueOf(reservationdetail.getCsid()));
                       // i.putExtra("plateNo", reservationdetail.getPlateno());
                        i.putExtra("address1", reservationdetail.getAddress1());
                        i.putExtra("endTime", reservationdetail.getEndtime());
                        i.putExtra("startTime", reservationdetail.getStarttime());
                        i.putExtra("province", reservationdetail.getProvince());
                        i.putExtra("postalCode", reservationdetail.getPostalcode());
                     //   i.putExtra("price", String.valueOf(reservationdetail.getPrice()));
                       // i.putExtra("vehicleMake", reservationdetail.getVehiclemake());
                       // i.putExtra("vehicleModel", reservationdetail.getVehiclemodel());
                        i.putExtra("status", reservationdetail.getStatus());
                        i.putExtra("reservationId", String.valueOf(reservationdetail.getReservationid()));
                        startActivity(i);
                        finish();
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(MyReservationsActivity.this, "failed", Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        });
    }

    public void reservationHistoryClicked(View view){
        startActivity(new Intent(MyReservationsActivity.this, ReservationHistoryActivity.class));
        finish();
    }


}
