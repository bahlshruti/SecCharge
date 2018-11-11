package com.aidor.secchargemobile.seccharge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.custom.SecCharge;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class ReservationEditDeleteActivity extends ActionBarActivity {
    private TextView tvSiteId, tvReservationId, tvStartTimeItem, tvEndTimeItem, tvAddress1, tvPostalCode, tvCountry, tvDate, tvStatus;
    private SecCharge myApp;
    protected boolean reservationCancelled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApp = (SecCharge) this.getApplicationContext();
        checkNetwork();
        setContentView(R.layout.activity_reservation_edit_delete);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_normal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        reservationCancelled = false;
        initComponents();

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
            startActivity(new Intent(ReservationEditDeleteActivity.this, NoInternetActivity.class));
            finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void initComponents() {
        tvReservationId = (TextView) findViewById(R.id.tvReservationId);
        tvSiteId = (TextView) findViewById(R.id.tvSiteId);
        tvStartTimeItem = (TextView) findViewById(R.id.tvStartTimeItem);
        tvEndTimeItem = (TextView) findViewById(R.id.tvEndTimeItem);
        tvDate = (TextView) findViewById(R.id.tvReservationDate);
        tvAddress1 = (TextView) findViewById(R.id.tvAddress);
        tvPostalCode = (TextView) findViewById(R.id.tvPostalCode);
        tvCountry = (TextView) findViewById(R.id.tvCountry);
        tvStatus = (TextView) findViewById(R.id.tvStatus);


        tvReservationId.setText(getIntent().getStringExtra("reservationId"));
        tvSiteId.setText(getIntent().getStringExtra("csId"));
        tvStartTimeItem.setText(getIntent().getStringExtra("startTime"));
        tvEndTimeItem.setText(getIntent().getStringExtra("endTime"));
        tvDate.setText(getIntent().getStringExtra("date"));
        tvAddress1.setText(getIntent().getStringExtra("address1") + ", " + getIntent().getStringExtra("province"));
        tvPostalCode.setText(getIntent().getStringExtra("postalCode"));
        tvCountry.setText(getIntent().getStringExtra("country"));
        tvStatus.setText(getIntent().getStringExtra("status"));



    }

    public void onEditClicked(View view){
        Intent i = new Intent(ReservationEditDeleteActivity.this, ReserveMainActivity.class);
        i.putExtra("SITE_ID", getIntent().getStringExtra("csId"));
        i.putExtra("MODIFICATION_STATUS","edit");
        i.putExtra("RESERVATION_ID", getIntent().getStringExtra("reservationId"));
        startActivity(i);
    }

    public void onCancelClicked(View view){
        final String resId = getIntent().getExtras().getString("reservationId");
        final CancelReservationTask cancelReservationTask = new CancelReservationTask(this, tvReservationId.getText().toString());
        AlertDialog.Builder builder = new AlertDialog.Builder(ReservationEditDeleteActivity.this);
        builder.setTitle("Cancel Reservation");
        builder.setMessage("Are you sure you want to cancel this reservation?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelReservationTask.execute(resId);
            }
        });
        builder.setNegativeButton("No", null);
        AlertDialog ad = builder.create();
        ad.show();


    }

    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReservationEditDeleteActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Yes", null);
        AlertDialog ad = builder.create();
        ad.show();
    }

    public void onBackButtonClicked(View view){
        startActivity(new Intent(ReservationEditDeleteActivity.this, MyReservationsActivity.class));
        finish();
    }

    @Override
    public void onBackPressed(){
        startActivity(new Intent(ReservationEditDeleteActivity.this, MyReservationsActivity.class));
        finish();
    }


    private class CancelReservationTask extends AsyncTask<String, Void, String> {
        ProgressDialog pDialog;


        protected CancelReservationTask(ReservationEditDeleteActivity activity,String reservationId){
            pDialog = new ProgressDialog(activity);
        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Cancelling Reservation");
            pDialog.setCancelable(false);
            pDialog.show();


        }

        @Override
        protected String doInBackground(String... params) {
            String cancelId = params[0];
            MyURL url = new MyURL();
            String URL = url.getUrlR()+"/cancelReserve?CancelId="+cancelId+"&type=beforeCharging";
            System.out.println("URL is:"+URL);
            String return_text;

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(URL);
                HttpResponse response = httpClient.execute(httpGet);
                String res = response.toString();
                System.out.println("Http Post Response : " + res);
                InputStream is = response.getEntity().getContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                String line = "";
                StringBuilder stringBuffer = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
                return_text = stringBuffer.toString();
                System.out.println("DATA EDITEV --> " + return_text);
                return return_text;

            } catch (ClientProtocolException | UnsupportedEncodingException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "false";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equals("success")){
                Toast.makeText(getApplicationContext(), "Reservation successfully cancelled!", Toast.LENGTH_SHORT).show();
                if (pDialog.isShowing()) pDialog.dismiss();
                startActivity(new Intent(ReservationEditDeleteActivity.this, MyReservationsActivity.class));
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Reservation cancellation failed!. Please try again", Toast.LENGTH_SHORT).show();
                if (pDialog.isShowing()) pDialog.dismiss();
            }
        }
    }
}
