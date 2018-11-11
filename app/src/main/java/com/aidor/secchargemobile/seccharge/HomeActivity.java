package com.aidor.secchargemobile.seccharge;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.api.ReservationApi;
import com.aidor.secchargemobile.custom.SecCharge;
import com.aidor.secchargemobile.model.ChargingNotification;
import com.aidor.secchargemobile.model.CsModel;
import com.aidor.secchargemobile.model.DischargeStatusModel;
import com.aidor.secchargemobile.model.MeterValueModel;
import com.aidor.secchargemobile.model.ServerTimeModel;
import com.aidor.secchargemobile.model.StartChargingModel;
import com.aidor.secchargemobile.model.StatusModel;
import com.aidor.secchargemobile.rest.RestClientReservation;
import com.aidor.secchargemobile.rest.SecchargeRestClient;
import com.aidor.secchargemobile.seccharge.Voice.VoiceActivity;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String HomeActivity_TAG = "HomeActivity";
    SecCharge myApp;

    public static boolean chargeMessageShown = false;

    CircleImageView profile_image;
    private static int RESULT_LOAD_IMG = 1;
    String imgDecodableString;
    String userID;
    TextView txtVwProfileName;


    DrawerLayout drawer;
    NavigationView navigationView;

    SharedPreferences pref;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String UserID = "UserID";
    private int id_val = 0;
    private int vehicle_id = 0;

    MapFragmentNew mapFragment;

    ProgressDialog pDialog;

    public static boolean popupShownOnce = false;
    public static boolean showPopup = false;
    public static long timeDifference = 30;

    boolean reserveNowApproved = false;

    boolean notificationSuccess = false;
    String notificationMessage = "";

    private SearchView searchView;

    Timer timer;

    TimerTask doAsynchronousTask;

    Handler timerHandler;
    Runnable timerRunnable;

    boolean paused = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApp =  (SecCharge) this.getApplicationContext();
        checkNetwork();

        setContentView(R.layout.activity_home);
        //expandableListView = (ExpandableListView) findViewById(R.id.expNavList);
        pDialog = new ProgressDialog(this);
        fetchChargingStations();

         callServerTimeTask();

        txtVwProfileName = (TextView) findViewById(R.id.textViewProfileName);
        // setGroupData();
        //setChildGroupData();

        //initDrawer();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        pref = this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userID = pref.getString(UserID, "");

        if (savedInstanceState != null) {
            Bundle b = getIntent().getExtras();
            id_val = b.getInt("ID");

           /* b = getIntent().getExtras();
            vehicle_id = b.getInt("vehicle_ID");*/
        }

        if (id_val != 0) {
            MyEVFragment myEVFragment = new MyEVFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("ID", id_val);
            myEVFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, myEVFragment).commit();
        }

        mapFragment = new MapFragmentNew();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mapFragment).commit();

        GetProfileData getProfileData = new GetProfileData();
        getProfileData.execute();

        profile_image = (CircleImageView) findViewById(R.id.profile_image);

        /*
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HomeActivity.this, "select image", Toast.LENGTH_SHORT).show();
                loadImage();
            }

            private void loadImage() {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
            }
        });

        */

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);

    }

    private void fetchChargingStations() {

        pDialog.setMessage("Fetching charging stations");
        pDialog.setCancelable(false);
        pDialog.show();

        RestClientReservation.get().getChargingSites(new Callback<List<CsModel>>() {
            @Override
            public void success(List<CsModel> csSiteModels, Response response) {

                SplashActivity.csModels = csSiteModels;

                pDialog.dismiss();
            }

            @Override
            public void failure(RetrofitError error) {
                pDialog.dismiss();
                error.printStackTrace();

                Toast.makeText(getApplicationContext(), "Could not get charging sites from server", Toast.LENGTH_LONG).show();
            }
        });
    }



    public void callServerTimeTask() {
        final Handler handler = new Handler();
        timer = new Timer();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {

                        if (!paused) {

                            try {
                                GetServerTimeTask getServerTimeTask = new GetServerTimeTask();

                                getServerTimeTask.execute();

                                getChargingNotification();


                                if (showPopup && !popupShownOnce) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(myApp.getCurrentActivity());
                                    builder.setTitle("Attention");
                                    builder.setMessage("You can now start charging! :)");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            popupShownOnce = true;
                                            RestClientReservation.get().getStartCharging(userID, new Callback<StartChargingModel>() {
                                                @Override
                                                public void success(StartChargingModel startChargingModel, Response response) {
                                                    Intent startCharging = new Intent(HomeActivity.this, ChargingStationActivity.class);
                                                    startCharging.putExtra("RESERVATION_ID", startChargingModel.getReservationid());
                                                    startCharging.putExtra("SITE_ID", startChargingModel.getSiteid());
                                                    startCharging.putExtra("USERNAME", startChargingModel.getUsername());
                                                    startCharging.putExtra("VEHICLE", startChargingModel.getVehicle());
                                                    startActivity(startCharging);
                                                }

                                                @Override
                                                public void failure(RetrofitError error) {
                                                    Toast.makeText(HomeActivity.this, "Failed to start Charging", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    });
                                    AlertDialog ad = builder.create();
                                    ad.show();
                                    popupShownOnce = true;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();

                            }

                        }

                    }
                });
            }
        };

        timer.schedule(doAsynchronousTask, 0, 3000); //execute in every 3 seconds

    }



    private void getChargingNotification() {
        RestClientReservation.get().getChargingNotification(userID, new Callback<ChargingNotification>() {
            @Override
            public void success(ChargingNotification chargingNotification, Response response) {
                if (chargingNotification.getSuccess()) {
                    notificationSuccess = true;
                    notificationMessage = chargingNotification.getMessage();

                    if (!chargeMessageShown) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(myApp.getCurrentActivity());
                        builder.setTitle("Attention");
                        builder.setMessage(notificationMessage);
                        builder.setPositiveButton("OK", null);

                        AlertDialog ad = builder.create();
                        ad.show();

                        chargeMessageShown = true;
                    }


                } else {
                    chargeMessageShown = false;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                Toast.makeText(HomeActivity.this, "Could not fetch chargingNotification from server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {

        paused = true;
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        paused = true;
      timer.cancel();
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
        paused = false;
        super.onResume();
        myApp.setCurrentActivity(this);
        checkNetwork();
        fetchChargingStations();
    }

    private void checkNetwork() {
        if (!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, NoInternetActivity.class).putExtra("activityName", "HomeActivity"));
            finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);


        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            SharedPreferences pref = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
            SharedPreferences.Editor edit = pref.edit();
            edit.clear();
            edit.commit();
            String check = pref.getString(UserID, "");
           // Toast.makeText(HomeActivity.this, "User ID :" + check, Toast.LENGTH_SHORT).show();

            /*
            Intent i = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
            */

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return true;
        }

        // Added by Nikhil
        // Option for starting Driver Mode aka reserving a charging station through voice input
        if (id == R.id.driver_mode) {
            Intent intent = new Intent(this, VoicePermissionCheck.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.voice_mode){
            Intent intent = new Intent(this, VoiceActivity.class);
            startActivity(intent);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_charge) {
            //  MapFragment mapFragment = new MapFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, mapFragment).commit();
            // Intent i = new Intent(HomeActivity.this, HomeActivity.class);
            //  startActivity(i);
        }
        else if ( id == R.id.nav_myAccount){
            startActivity(new Intent(HomeActivity.this, AccountActivity.class));
            overridePendingTransition(R.anim.activity_fade_in, R.anim.empty);
        }

        else if (id == R.id.nav_reserve){
            startActivity(new Intent(HomeActivity.this, MyReservationsActivity.class));
            overridePendingTransition(R.anim.activity_fade_in, R.anim.empty);
        } else if(id == R.id.nav_trip){
            Intent startTrip = new Intent(HomeActivity.this, TripPlanerActivity.class);
            startActivity(startTrip);
            overridePendingTransition(R.anim.activity_fade_in, R.anim.empty);
        } else if (id == R.id.nav_start_charging) {


            RestClientReservation.get().getStartCharging(userID, new Callback<StartChargingModel>() {

                @Override
                public void success(final StartChargingModel startChargingModel, Response response) {


                    String noReservation = "noReservation";
                    if (startChargingModel.getNoReservation().equals(noReservation)) {
                        showAlert("Attention", "You have no reservations. Please make a reservation first.");
                    } else {

                        if (startChargingModel.getProceed().equals("true")) {


                            if (Integer.parseInt(startChargingModel.getReservationid()) > 0) {

                                RestClientReservation.get().getCurrentServerTime(new Callback<ServerTimeModel>() {

                                    @Override
                                    public void success(ServerTimeModel serverTimeModel, Response response) {


                                        //  System.out.println("SOC: Time returned from server: " + serverTimeModel.getCurrentTime());
                                        Calendar currentDateTime = Calendar.getInstance();

                                        //Date date = currentDateTime.getTime();
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                                        Date date = null;
                                        try {
                                            date = dateFormat.parse(serverTimeModel.getCurrentDate());
                                            //  System.out.println("Date formatted to :" + date.toString());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                            Log.d("SOC_DATE_PARSE", "Date couldn't be parsed");
                                        }

                                        currentDateTime.setTime(date);


                                        String currentDate = dateFormat.format(date);

                                        //   System.out.println("currentDate string variable: " + currentDate);
                                        //    System.out.println("Reserve Date returned from server: " + startChargingModel.getReservedate());
                                        // int currentTime = currentDateTime.get(Calendar.HOUR_OF_DAY);
                                        //int startTime = parseStartTimeToInt(startChargingModel.getReservestarttime());

                                        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

                                        Date startTimeDate = null;

                                        try {
                                            startTimeDate = sdfTime.parse(startChargingModel.getReservestarttime());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }


                                        Date currentTimeDate = new Date();
                                        try {
                                            currentTimeDate = sdfTime.parse(serverTimeModel.getCurrentTime());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        //   System.out.println("Reservation Start time returned from server: " + startChargingModel.getReservestarttime());
                                        //  System.out.println("startTime: " + startTimeDate.toString());
                                        //  System.out.println("currentTime: " + currentTimeDate.toString());

                                        long difference = startTimeDate.getTime() - currentTimeDate.getTime();
                                        long diffMinutes = (difference / (60 * 1000) % 60);

                                        //  System.out.println("Start Charging time difference: " + diffMinutes);
                                        if (currentDate.equals(startChargingModel.getReservedate()) && !(startTimeDate.after(currentTimeDate))) {
                                            showPopup = true;


                                            Intent startCharging = new Intent(HomeActivity.this, ChargingStationActivity.class);
                                            startCharging.putExtra("RESERVATION_ID", startChargingModel.getReservationid());
                                            startCharging.putExtra("SITE_ID", startChargingModel.getSiteid());
                                            startCharging.putExtra("USERNAME", startChargingModel.getUsername());
                                            startCharging.putExtra("VEHICLE", startChargingModel.getVehicle());
                                            startActivity(startCharging);

                                            overridePendingTransition(R.anim.activity_fade_in, R.anim.empty);
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                                            builder.setTitle("Attention");
                                            builder.setMessage("You can start charging on " + startChargingModel.getReservedate() +
                                                    " at: " + startChargingModel.getReservestarttime());
                                            builder.setPositiveButton("OK", null);
                                            AlertDialog ad = builder.create();
                                            ad.show();

                                        }


                                    }


                                    @Override
                                    public void failure(RetrofitError error) {
                                        System.out.println("Failed to get current time from server. ");

                                        Toast.makeText(getApplicationContext(), "failed to get current time from server", Toast.LENGTH_SHORT).show();

                                    }
                                });


                            } else {
                                showAlert("Attention", "You don't have any reservation");
                            }
                        }
                    }
                }


                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(HomeActivity.this, "Failed to get reservation data from server.", Toast.LENGTH_LONG).show();
                }
            });



        } else if (id == R.id.nav_start_discharging) {
            startDischarging();


        }   else if (id == R.id.nav_meter_values){
            //startActivity(new Intent(HomeActivity.this, MeterValuesActivity.class).putExtra("variable", "test").putExtra("value", "test"));

            RestClientReservation.get().getMeterValues(userID, new Callback<MeterValueModel>() {
                @Override
                public void success(MeterValueModel meterValueModel, Response response) {
                    String variable = meterValueModel.getVariable();
                    String value = meterValueModel.getValue();
                    Long transactionId = meterValueModel.getTransactionId();
                    if (variable == null || value == null) {
                        Toast.makeText(HomeActivity.this, "No Transaction!", Toast.LENGTH_SHORT).show();
                    } else
                        startActivity(new Intent(HomeActivity.this, MeterValuesActivity.class).putExtra("variable", variable).putExtra("value", value)
                        .putExtra("Transaction_id", transactionId));
                }

                @Override
                public void failure(RetrofitError error) {
                    error.printStackTrace();
                    Toast.makeText(HomeActivity.this, "Could not retrieve Meter Values from server", Toast.LENGTH_SHORT).show();
                }
            });

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    public void startDischarging(){
        RestClientReservation.get().startDischarging(userID, new Callback<DischargeStatusModel>() {
            @Override
            public void success(DischargeStatusModel dischargeStatusModel, Response response) {
                String success = dischargeStatusModel.getSuccess();
                float stateOfCharge, dischargeStateOfCharge;
                if (success.equals("true")){
                    stateOfCharge = dischargeStatusModel.getStateOfCharge();
                    dischargeStateOfCharge = dischargeStatusModel.getDischargeStateOfCharge();

                    Intent intent = new Intent(HomeActivity.this, StartDischargingActivity.class);
                    intent.putExtra("stateOfCharge", stateOfCharge);
                    intent.putExtra("dischargeStateOfCharge", dischargeStateOfCharge);
                    overridePendingTransition(R.anim.activity_fade_in, R.anim.empty);
                } else if (success.equals("false")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setTitle("Attention");
                    builder.setMessage(dischargeStatusModel.getErrorMessage());
                    builder.setPositiveButton("OK", null);
                    AlertDialog ad = builder.create();
                    ad.show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                Toast.makeText(HomeActivity.this, "Could not connect to server. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(android.R.drawable.stat_notify_error);
        builder.setPositiveButton("OK", null);
        AlertDialog ad = builder.create();
        ad.show();
    }

    private int parseStartTimeToInt(String reservestarttime) {
        char i = reservestarttime.charAt(0);
        char j = reservestarttime.charAt(1);
        String total = String.valueOf(i)+String.valueOf(j);
        int toVal = Integer.parseInt(total);
        return toVal;
    }

    private class GetProfileData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String return_text;
            MyURL Url = new MyURL();
            String URL = Url.getUrl();
            try {
                System.out.println("Back User id profile  -->> " + userID);
                //               System.out.println("Back Password -->> " + sPassword);

                HttpClient httpClient = new DefaultHttpClient();
//                HttpPost httpPost = new HttpPost("http://.2.12:8011/seccharge/test/login/mob");
//                List<NameValuePair> para = new ArrayList<>();
//                para.add(new BasicNameValuePair("email", sEmail));
//                para.add(new BasicNameValuePair("password", sPassword));

                String NewURL = URL + "login/basicprofile?userid=" + userID;
                HttpGet httpGet = new HttpGet(NewURL);
                // Encoding POST data
//                httpGet.setEntity(new UrlEncodedFormEntity(para));
                // Making HTTP Request

                HttpResponse response = httpClient.execute(httpGet);
                String res = response.toString();

                //System.out.println("Http Post Response : " + res);
                InputStream is = response.getEntity().getContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                String line = "";
                StringBuilder stringBuffer = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
                return_text = stringBuffer.toString();
               // System.out.println("DATA FROM SERVER --> " + return_text);
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
            try {
                JSONObject jsonObject = new JSONObject(s);
                txtVwProfileName.setText(jsonObject.getString("FIRSTNAME"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class GetServerTimeTask extends AsyncTask<String, Void, String> {


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


        }

        @Override
        protected String doInBackground(String... params) {

            RestClientReservation.get().getStartCharging(userID, new Callback<StartChargingModel>() {


                @Override
                public void success(final StartChargingModel startChargingModel, Response response) {

                    if (startChargingModel.getProceed().equals("true")) {

                        if (Integer.parseInt(startChargingModel.getReservationid()) > 0) {

                            RestClientReservation.get().getCurrentServerTime(new Callback<ServerTimeModel>() {

                                @Override
                                public void success(ServerTimeModel serverTimeModel, Response response) {


                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");


                                    Date dateReservationTime = null, dateCurrentTime = null, reservationDate = null, currentServerDate = null;
                                    try {
                                        dateReservationTime = sdf.parse(startChargingModel.getReservestarttime());
                                        dateCurrentTime = sdf.parse(serverTimeModel.getCurrentTime());
                                        reservationDate = sdfDate.parse(startChargingModel.getReservedate());
                                        currentServerDate = sdfDate.parse(serverTimeModel.getCurrentDate());
                                        System.out.println("Date returned from server: " + serverTimeModel.getCurrentDate());
                                        System.out.println("currentServerDate variable set to: " + currentServerDate.toString());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }


                                    if (reservationDate.equals(currentServerDate)) {
                                        //   System.out.println("TIME TASK DETAILS:");
                                        //  System.out.println("Reservation Time from server: " + dateReservationTime);
                                        //  System.out.println("Current Time from server: " + dateCurrentTime);

                                        long difference = dateReservationTime.getTime() - dateCurrentTime.getTime();
                                        long diffMinutes = (difference / (60 * 1000) % 60);
                                        long diffHours = difference / (60 * 60 * 1000);


                                        // System.out.println("Time difference: " + diffHours + " hours " + diffMinutes + " minutes");
                                        timeDifference = diffMinutes;

                                        if (diffHours > 1 || diffMinutes <= -30 || diffMinutes > 0) {
                                            showPopup = false;
                                            popupShownOnce = false;
                                        } else if (diffHours == 0 && diffMinutes <= 0 && diffMinutes > -30) {

                                            showPopup = true;
                                        }
                                    } else {
                                        popupShownOnce = false;
                                        showPopup = false;
                                    }


                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    System.out.println("Failed to get timer task server time");
                                }
                            });

                        }
                    } else if (startChargingModel.getProceed().equals("false")) {
                        System.out.println("PROCEED STATUS ----->>>>> FALSE");
                    }


                }



                @Override
                public void failure(RetrofitError error) {
                    System.out.println("Failed to get timer task reservation details");
                }
            });



                return null;
        }
    }


}
