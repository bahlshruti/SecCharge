package com.aidor.secchargemobile.seccharge;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.custom.SecCharge;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReserveSummaryActivity extends ActionBarActivity {

    TextView tvCarName, tvSiteId, tvSiteOwner, tvAddress1, tvAddress2, tvStartTime, tvEndTime,
                tvDate, tvPortType;
    String userId, reservationId, reservationType;
    private SecCharge myApp;
    ProgressDialog pDialog;
    TextView tvTotalCharges;

    private static PayPalConfiguration config = new PayPalConfiguration()

            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)

            .clientId("ATHublRqN6mYhoBuT-NBW1ql6OzHk4TlaSpO0YjboNGkA08R3TwgZ_hbiEFzsJGMx2_FO1bmn5s9ihfT");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApp = (SecCharge) this.getApplicationContext();
        checkNetwork();
        setContentView(R.layout.activity_reserve_summary);


        Intent intent = new Intent(this, PayPalService.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        startService(intent);

        tvTotalCharges = (TextView) findViewById(R.id.tvtotalCharges);

        initComponent();
        appendDataToView();



    }

    @Override
    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
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
            startActivity(new Intent(ReserveSummaryActivity.this, NoInternetActivity.class));
            finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void appendDataToView() {
        tvCarName.setText(getIntent().getExtras().getString("VEHICLE_MAKE")+" "+getIntent().getExtras().getString("VEHICLE_MODEL"));
        tvSiteId.setText(getIntent().getExtras().getString("SITE_ID"));
        tvSiteOwner.setText(getIntent().getExtras().getString("SITE_OWNER"));
        tvAddress1.setText(getIntent().getExtras().getString("ADDRESS1"));
        tvAddress2.setText(getIntent().getExtras().getString("ADDRESS2"));
        tvStartTime.setText(getIntent().getExtras().getString("RESERVATION_START_TIME"));
        tvEndTime.setText(getIntent().getExtras().getString("RESERVATION_END_TIME"));
        tvDate.setText(getIntent().getExtras().getString("RESERVATION_DATE"));
        tvPortType.setText(getIntent().getExtras().getString("PORT_TYPE"));
        userId = getIntent().getExtras().getString("USER_ID");
        reservationId = getIntent().getExtras().getString("RESERVATION_ID");
    }

    private void initComponent() {
        tvCarName = (TextView)findViewById(R.id.summary_tv_carName);
        tvSiteId = (TextView)findViewById(R.id.summary_tv_siteID);
        tvSiteOwner = (TextView)findViewById(R.id.summary_tv_owner);
        tvAddress1 = (TextView)findViewById(R.id.summary_tv_address1);
        tvAddress2 = (TextView)findViewById(R.id.summary_tv_address2);
        tvStartTime = (TextView)findViewById(R.id.summary_tvStartTime);
        tvEndTime = (TextView)findViewById(R.id.summary_tvEndTime);
        tvDate = (TextView)findViewById(R.id.summary_tvDate);
        tvPortType = (TextView)findViewById(R.id.summary_tvPort);
    }

    public void goBack(View view){
        finish();
    }

    @Override
    public void onBackPressed(){
       // startActivity(new Intent(ReserveSummaryActivity.this, ReserveMainActivity.class).putExtra("SITE_ID", getIntent().getStringExtra("SITE_ID")));
        finish();
    }

    public void onConfirmClicked(View view){
        reservationType = getIntent().getStringExtra("RESERVATION_TYPE");
        if (reservationType.equals("edit")) {
            ConfirmReservationWebService confirmReservation = new ConfirmReservationWebService(ReserveSummaryActivity.this, "Modify_Reservation");
            confirmReservation.execute();
        } else {
            ConfirmReservationWebService confirmReservation = new ConfirmReservationWebService(ReserveSummaryActivity.this, "Confirm");
            confirmReservation.execute();
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                try {
                    Log.i("paymentExample", confirm.toJSONObject().toString(4));

                    ReservationPaymentWebService paymentWebService = new ReservationPaymentWebService(ReserveSummaryActivity.this);
                    paymentWebService.execute();



                    // TODO: send 'confirm' to your server for verification.
                    // see https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                    // for more details.

                } catch (JSONException e) {
                    Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            Log.i("paymentExample", "The user canceled.");
        }
        else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
        }
    }




    private class ReservationPaymentWebService extends AsyncTask<String, String, String> {
        ReserveSummaryActivity activity;

        protected ReservationPaymentWebService(ReserveSummaryActivity activity){
            this.activity = activity;
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
            String URL = Url.getUrlR()+"/paypalPayment";

            try {
                HttpClient httpClient = new DefaultHttpClient();
                List<NameValuePair> user = new ArrayList<>();

                user.add(new BasicNameValuePair("reservationId", reservationId));

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


            if (s.equals("success")) {
                finish();

                startActivity(new Intent(ReserveSummaryActivity.this, HomeActivity.class));
                overridePendingTransition(R.anim.activity_fade_in, R.anim.empty);
                Toast.makeText(getApplicationContext(), "Successfully Reserved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Reservation failed!", Toast.LENGTH_SHORT).show();
            }

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }





    public void initiatePaypal(){
        PayPalPayment payment = new PayPalPayment(new BigDecimal(tvTotalCharges.getText().toString()), "CAD", "Reservation Charge",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(ReserveSummaryActivity.this, PaymentActivity.class);

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        startActivityForResult(intent, 0);
    }

    private class ConfirmReservationWebService extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;
        String buttonValue;
        ReserveSummaryActivity activity;

        protected ConfirmReservationWebService(ReserveSummaryActivity activity, String buttonType){
            this.activity = activity;
            pDialog = new ProgressDialog(activity);
            this.buttonValue = buttonType;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Confirming");
            pDialog.setCancelable(false);
            pDialog.show();
        }


        @Override
        protected String doInBackground(String... userms) {
            String return_text;

            MyURL Url = new MyURL();
            String URL = Url.getUrlR()+"/reservationConfirm";

            try {
                HttpClient httpClient = new DefaultHttpClient();
                List<NameValuePair> user = new ArrayList<>();
                user.add(new BasicNameValuePair("BtnConfirm", buttonValue));
                user.add(new BasicNameValuePair("registrationid", reservationId));
                user.add(new BasicNameValuePair("userid", userId));
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
            if (pDialog.isShowing()){
                pDialog.dismiss();
            }

            if (s.equals("success") || s.equals("cofirmedByUser")) {
                // Toast.makeText(getApplicationContext(), "proceeding to next Step..", Toast.LENGTH_SHORT).show();

                initiatePaypal();



                /*
                Intent intent = new Intent(ReserveSummaryActivity.this, PaymentInfoActivity.class);
                intent.putExtra("RESERVATION_ID", reservationId);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_fade_in, R.anim.empty);
                */


            } else if (s.equals("paymentPreAuthorized")) {
                Intent intent = new Intent(ReserveSummaryActivity.this, MyReservationsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_fade_in, R.anim.empty);
                finish();
            }
            else {
                Toast.makeText(getApplicationContext(), "Reservation failed!", Toast.LENGTH_SHORT).show();
            }

           // finish();
        }
    }
}
