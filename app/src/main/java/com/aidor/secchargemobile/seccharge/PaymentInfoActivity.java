package com.aidor.secchargemobile.seccharge;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.custom.SecCharge;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class PaymentInfoActivity extends ActionBarActivity {
    private android.widget.Spinner spinnerExpMonth, spinnerExpYear;
    EditText etCardNumber;
    String buttonPay, cardNumber, expiryMonth, expiryYear, reservationId, stripeToken;
    private TextView tvReservationId;
    Button payButton;
    ProgressDialog pDialog;

    private SecCharge myApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApp = (SecCharge) this.getApplicationContext();
        checkNetwork();
        setContentView(R.layout.activity_payment_info);

        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_normal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
            */
        initComponents();


    }

    public void onCancelClicked(View view){
        startActivity(new Intent(PaymentInfoActivity.this, HomeActivity.class));
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
            startActivity(new Intent(PaymentInfoActivity.this, NoInternetActivity.class));
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
        cardNumber = etCardNumber.getText().toString();
        expiryMonth = spinnerExpMonth.getSelectedItem().toString();
        expiryYear = spinnerExpYear.getSelectedItem().toString();
        reservationId = getIntent().getExtras().getString("RESERVATION_ID");
        buttonPay = "Pay";
        stripeToken = "tok_17dZwqECUL4KEpyCn1Lum7ew";
    }

    private void initComponents() {
       // tvReservationId = (TextView) findViewById(R.id.tvReservationId);
        //tvReservationId.setText(getIntent().getExtras().getString("RESERVATION_ID"));
        ArrayList<String> listMonths = new ArrayList<>();

        for (int i=1; i<13; i++){
            if (i<10) {
                String t = "0" + i;
                listMonths.add(t);
            } else listMonths.add(i+"");
        }

        ArrayList<String> listYears = new ArrayList<>();

        for (int i=2016; i<=2080; i++){
            listYears.add(i+"");
        }

        spinnerExpMonth = (android.widget.Spinner) findViewById(R.id.spinnerExpMonth);
        spinnerExpYear = (android.widget.Spinner) findViewById(R.id.spinnerExpYear);
        etCardNumber = (EditText)findViewById(R.id.etCardNumber);
        payButton = (Button)findViewById(R.id.btnPay);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_spinner_exp_month, listMonths);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinnerExpMonth.setAdapter(adapter);

        ArrayAdapter<String> adapterYears = new ArrayAdapter<String>(this, R.layout.item_spinner_exp_month, listYears);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinnerExpYear.setAdapter(adapterYears);
    }

    public void payClicked(View v) {
        fetchData();
        ReservationPaymentWebService paymentWebService = new ReservationPaymentWebService(PaymentInfoActivity.this);
        paymentWebService.execute();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(PaymentInfoActivity.this, HomeActivity.class));
        finish();
    }

    private class ReservationPaymentWebService extends AsyncTask<String, String, String> {

        protected ReservationPaymentWebService(PaymentInfoActivity activity){
            pDialog = new ProgressDialog(activity);
        }
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pDialog.setMessage("Please Wait");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... userms) {
            String return_text;

            MyURL Url = new MyURL();
            String URL = Url.getUrlR()+"/payment";

            try {
                HttpClient httpClient = new DefaultHttpClient();
                List<NameValuePair> user = new ArrayList<>();
                user.add(new BasicNameValuePair("BtnPay", buttonPay));
                user.add(new BasicNameValuePair("Card_Number", cardNumber));
                user.add(new BasicNameValuePair("Expiration_Month", expiryMonth));
                user.add(new BasicNameValuePair("Year", expiryYear));
                user.add(new BasicNameValuePair("myReservationIdInPayForm", reservationId));
                user.add(new BasicNameValuePair("stripeToken", stripeToken));
                HttpPost httpPost = new HttpPost(URL);
                httpPost.setEntity(new UrlEncodedFormEntity(user));

                HttpResponse response = httpClient.execute(httpPost);

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
                System.out.println("DATA REG. --> " + return_text);
                return return_text;
            } catch (ClientProtocolException | UnsupportedEncodingException clientEx) {
                clientEx.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "false";
        }



        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("REG RES:", s);

            finish();

            startActivity(new Intent(PaymentInfoActivity.this, HomeActivity.class));

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            overridePendingTransition(R.anim.activity_fade_in, R.anim.empty);
            if (s.equals("success")) {

                Toast.makeText(getApplicationContext(), "Successfully Booked", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Reservation failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
