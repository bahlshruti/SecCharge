package com.aidor.secchargemobile.seccharge;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.custom.SecCharge;

import com.aidor.secchargemobile.model.DatePickerFragment;
import com.aidor.secchargemobile.model.EditReservationModel;
import com.aidor.secchargemobile.model.ServerTimeModel;
import com.aidor.secchargemobile.model.ViewReserveModel;
import com.aidor.secchargemobile.rest.RestClientReservation;

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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ReserveMainActivity extends ActionBarActivity implements DatePickerDialog.OnDateSetListener{

    private DialogFragment timeFragment, dateFragment;
    private LinearLayout tabTime;

    private EditText etSelectDate; //stores the selected date as dd/mm/yy
    private TextView tvTimeSelected, tvCarName, tvSiteId, tvSiteOwner, tvAddress1, tvAddress2, tvTemporary;
    private Button btnNext;
    private ImageView btnSelectDate;
    private ListView listTime;
    private LinearLayout layout_dateSelection;
    public static String serverTime, serverDate;
    private NumberPicker npTime;
    private TimeAdapter timeAdapter;
    TextView txt_chargingPort, txt_reservationDetails;

    private boolean dateSelectedOnce;
    private String dateSelected;

    private ArrayList<Integer> positionsToDisable;

    private String[] timeValues;
    private List<String> listTimeValues;
    SharedPreferences sharedPreferences;
    String userId, siteId, siteOwner, address1, address2,level2price,
            portLevel,reservationDate,reservationStartTime,reservationEndTime, reservationId, vehicleMake,
            vehicleModel;

    private SecCharge myApp;
    String reservationType = "normal";

    String selectedDateForEdit = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApp = (SecCharge) this.getApplicationContext();
        checkNetwork();
        setContentView(R.layout.activity_reserve_main);
        txt_chargingPort = (TextView) findViewById(R.id.txt_chargingPort);
        txt_reservationDetails = (TextView) findViewById(R.id.txt_reservationDetails);

        RadioGroup rgPorts = (RadioGroup) findViewById(R.id.rgPorts);
        Animation leftSlide = AnimationUtils.loadAnimation(ReserveMainActivity.this, R.anim.fast_slide_left);

        rgPorts.startAnimation(leftSlide);

        layout_dateSelection = (LinearLayout) findViewById(R.id.layout_dateSelection);
        etSelectDate = (EditText) findViewById(R.id.etSelectDate); //will store the date as dd/mm/yy

        Animation rightSlide = AnimationUtils.loadAnimation(ReserveMainActivity.this, R.anim.fast_slide_right);
        // rightSlide.reset();

        layout_dateSelection.startAnimation(rightSlide);

        Animation fadeIn = AnimationUtils.loadAnimation(ReserveMainActivity.this, R.anim.fade_in);
        // fadeIn.reset();
        //txt_chargingPort.clearAnimation();
        // txt_reservationDetails.clearAnimation();
        txt_chargingPort.startAnimation(fadeIn);
        txt_reservationDetails.startAnimation(fadeIn);
        tabTime = (LinearLayout) findViewById(R.id.tabTime);
        Animation fadeInTime = AnimationUtils.loadAnimation(ReserveMainActivity.this, R.anim.fade_in);
        tabTime.startAnimation(fadeInTime);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_normal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // tabTime.setVisibility(View.INVISIBLE);
        npTime = (NumberPicker) findViewById(R.id.npTime);
        npTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        npTime.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        serverTime = "Time not set";
        serverDate = "Date from server not set";

        dateSelected = "";
        positionsToDisable = new ArrayList<>();
        fetchCurrentServerTime();






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
            startActivity(new Intent(ReserveMainActivity.this, NoInternetActivity.class));
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
        finish();

    }

    public void showDatePickerDialog(){


        if (dateFragment == null) {
            dateFragment = new DatePickerFragment();
        }

        dateFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        dateSelectedOnce = true;

        int month = monthOfYear+1;
        String monthString = "";
        if (month<10){
            monthString = "0" + month;
        } else {
            monthString = "" + month;
        }

        String dayString = "";
        if (dayOfMonth < 10){
            dayString = "0" + dayOfMonth;
        } else {
            dayString = "" + dayOfMonth;
        }
        dateSelected = year + "-" + monthString + "-" + dayString;
        etSelectDate.setText(dateSelected);
        positionsToDisable.clear();
        updateListTimes();
        Animation fadeIn = AnimationUtils.loadAnimation(ReserveMainActivity.this, R.anim.fade_in);

        tabTime.setVisibility(View.VISIBLE);
        tabTime.startAnimation(fadeIn);


        // Toast.makeText(getApplicationContext(), dayOfMonth + "/" + monthOfYear + "/" + year, Toast.LENGTH_LONG).show();
        //Log.d("DATE:", dayOfMonth + "/" + monthOfYear + "/" + year);
    }

    private void updateListTimes() {

        int firstAvailableTimePosition;
        for (int i = 0; i< timeValues.length; i++){
            DateFormat sdf = new SimpleDateFormat("HH:mm");

            Date datelistTime = null, dateServerTime = null, currentServerDate = null, dateSelectedDate = null;
            try {
                datelistTime = sdf.parse(timeValues[i]);
                dateServerTime = sdf.parse(serverTime);


            } catch (ParseException e) {
                Toast.makeText(getApplicationContext(), "Unable to parse time to Date object", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            dateSelected = etSelectDate.getText().toString();

            // System.out.println("Server Date in Time List Activity: " + serverDate);
            // System.out.println("Selected Date in Time List Activity: " + dateSelected);
            if (!dateSelected.equals("")) {
                if (dateSelected.equals(serverDate) && datelistTime.before(dateServerTime)) {


                    positionsToDisable.add(i);

                    if (positionsToDisable.size() == timeValues.length) {
                        positionsToDisable.clear();
                    }

                } else if (!dateSelected.equals(serverDate)){
                    positionsToDisable.clear();
                }
            } else {
                if (datelistTime.before(dateServerTime)) {

                    positionsToDisable.add(i);

                    if (positionsToDisable.size() == timeValues.length) {
                        positionsToDisable.clear();
                    }

                }
            }

        }

        updateNumberPicker(positionsToDisable, timeValues, dateSelected);


        timeAdapter = new TimeAdapter(this, timeValues, positionsToDisable);

        listTime.setAdapter(timeAdapter);


        if (positionsToDisable.size()< timeValues.length){
            firstAvailableTimePosition = positionsToDisable.size();
            listTime.setSelection(firstAvailableTimePosition);
            listTime.requestFocus();
        }

        listTime.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //reservationStartTime = (String) listTime.getItemAtPosition(position);

                // tvTimeSelected.setText(reservationStartTime);
                //listTime.setEnabled(false);
            }
        });
    }

    private void updateNumberPicker(ArrayList<Integer> positionsToDisable, final String[] timeValues, String dateSelected) {
        reservationStartTime = tvTimeSelected.getText().toString();
        npTime.refreshDrawableState();
        npTime.setDisplayedValues(null);
        if (dateSelected.equals(serverDate)) {

            if (positionsToDisable.isEmpty()){
                tvTimeSelected.setText("");
                npTime.setVisibility(View.INVISIBLE);
                tabTime.setVisibility(View.INVISIBLE);
                AlertDialog.Builder builder = new AlertDialog.Builder(ReserveMainActivity.this);
                builder.setTitle("Attention");
                builder.setMessage("No time slot available for the selected date. Please choose another date.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tabTime.setVisibility(View.INVISIBLE);
                        npTime.setVisibility(View.INVISIBLE);
                    }
                });
                AlertDialog ad = builder.create();
                ad.show();

            }
            else {
                npTime.setVisibility(View.VISIBLE);
                final ArrayList<String> timeList = new ArrayList<>();

                for (int i = 0; i < timeValues.length; i++) {
                    if (!positionsToDisable.contains(i)) {
                        timeList.add(timeValues[i]);
                    } else {
                        timeList.remove((timeValues[1]));
                    }
                }

                final String[] timeArray = new String[timeList.size()];
                for (int i = 0; i < timeList.size(); i++) {
                    timeArray[i] = timeList.get(i);
                }

                npTime.setMinValue(0);
                npTime.setMaxValue(timeList.size() - 1);
                npTime.setValue(0);
                npTime.setDisplayedValues(timeArray);
                npTime.requestFocus();
                npTime.setWrapSelectorWheel(true);
                tvTimeSelected.setText(timeArray[npTime.getValue()]);

                npTime.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        // System.out.println("TIME SELECTED: " + timeArray[newVal]);
                        tvTimeSelected.setText(timeArray[newVal]);
                    }
                });

                reservationStartTime = tvTimeSelected.getText().toString();
            }

        } else {
            npTime.setVisibility(View.VISIBLE);
            npTime.setMinValue(0);
            npTime.setMaxValue(timeValues.length - 1);

            npTime.setValue(0);
            npTime.setWrapSelectorWheel(true);
            npTime.requestFocus();
            npTime.setDisplayedValues(timeValues);

            tvTimeSelected.setText(timeValues[npTime.getValue()]);

            npTime.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    //  System.out.println("TIME SELECTED: " + timeValues[newVal]);
                    tvTimeSelected.setText(timeValues[newVal]);
                }
            });
            reservationStartTime = tvTimeSelected.getText().toString();
        }
    }

    private void initComponents() {
        /*
        tvCarName = (TextView)findViewById(R.id.tv_carName);
        tvSiteId = (TextView)findViewById(R.id.tv_siteID);
        tvSiteOwner = (TextView)findViewById(R.id.tv_owner);
        tvAddress1 = (TextView)findViewById(R.id.tv_address1);
        tvAddress2 = (TextView)findViewById(R.id.tv_address2);
        */

        dateSelectedOnce = false;
        etSelectDate.setText(serverDate);
        etSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        btnSelectDate = (ImageView) findViewById(R.id.btnSelectDate);
        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        tvTimeSelected = (TextView) findViewById(R.id.tvTimeSelected);
        listTimeValues = new ArrayList<String>();

        setListTime();

    }

    private void setListTime() {
        timeValues = new String[] {"00:00", "00:30", "01:00","01:30", "02:00","02:30", "03:00", "03:30", "04:00", "04:30", "05:00", "05:30", "06:00", "06:30",
                "07:00","07:30", "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30",
                "15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30", "21:00", "21:30", "22:00", "22:30",
                "23:00", "23:30"};

        listTime = (ListView) findViewById(R.id.listTime);
        updateListTimes();



        btnNext = (Button) findViewById(R.id.btnNext1);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (etSelectDate.getText().toString().equals("--/--/--")){
                    Toast.makeText(getApplicationContext(), "Please select the reservation date!", Toast.LENGTH_SHORT).show();
                } else if (tvTimeSelected.getText().equals("")){
                    Toast.makeText(getApplicationContext(), "Please select the reservation start time!", Toast.LENGTH_SHORT).show();
                } else {
                    reservationStartTime = tvTimeSelected.getText().toString();
                    reservationEndTime = calculateReservationEndTime(reservationStartTime);
                    reservationDate = etSelectDate.getText().toString();
                    dateSelected = etSelectDate.getText().toString();
                    reservationType = getIntent().getStringExtra("MODIFICATION_STATUS");
                    if (reservationType.equals("edit")){
                        String reservationId = getIntent().getStringExtra("RESERVATION_ID");
                        startEditReservationWebService(reservationId);
                    } else {
                        ReservationWebService reservationWebService = new ReservationWebService(ReserveMainActivity.this);
                        reservationWebService.execute();
                    }
                }
                /*
                if (host.getCurrentTab()==0) {
                    host.getTabWidget().getChildTabViewAt(1).setEnabled(true);

                    host.setCurrentTab(1);
                } else if (host.getCurrentTab() == 1){
                    if (dateSelectedOnce) {
                        host.getTabWidget().getChildTabViewAt(2).setEnabled(true);
                        host.setCurrentTab(2);
                    } else {
                        Toast.makeText(getApplicationContext(), "Please select a date.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                    if (tvTimeSelected.getText().equals("")){
                        Toast.makeText(getApplicationContext(), "Please select the start time.", Toast.LENGTH_SHORT).show();
                    } else {
                        reservationEndTime = calculateReservationEndTime(reservationStartTime);
                        reservationDate = etSelectDate.getText().toString();
                        reservationType = getIntent().getStringExtra("MODIFICATION_STATUS");
                        if (reservationType.equals("edit")){
                            String reservationId = getIntent().getStringExtra("RESERVATION_ID");
                            startEditReservationWebService(reservationId);
                        } else {
                            ReservationWebService reservationWebService = new ReservationWebService(ReserveMainActivity.this);
                            reservationWebService.execute();
                        }
                    }

                }
                */
            }
        });
    }

    private void startEditReservationWebService(String reservationId) {
        dateSelected = etSelectDate.getText().toString();
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfDateServer = new SimpleDateFormat("dd-MM-yyyy");

        try {
            Date selectedDate = sdfDate.parse(dateSelected);
            selectedDateForEdit = sdfDateServer.format(selectedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //TODO edit reservation webservice
        RestClientReservation.get().getEditReservation(reservationId, selectedDateForEdit, reservationStartTime,
                reservationEndTime, new Callback<EditReservationModel>() {
                    @Override
                    public void success(EditReservationModel editReservationModel, Response response) {
                        String reserveId = editReservationModel.getReservationId();
                        int editedReserveId = Integer.parseInt(reserveId);
                        if (editedReserveId > 0) {
                            Intent intent = new Intent(ReserveMainActivity.this, ReserveSummaryActivity.class);
                            intent.putExtra("SITE_ID", siteId);
                            intent.putExtra("RESERVATION_ID", reserveId);
                            intent.putExtra("SITE_OWNER", siteOwner);
                            intent.putExtra("ADDRESS1", address1);
                            intent.putExtra("ADDRESS2", address2);
                            intent.putExtra("RESERVATION_START_TIME", tvTimeSelected.getText().toString());
                            intent.putExtra("RESERVATION_END_TIME", reservationEndTime);
                            intent.putExtra("RESERVATION_DATE", selectedDateForEdit);
                            intent.putExtra("USER_ID", userId);
                            intent.putExtra("PORT_TYPE", portLevel);
                            intent.putExtra("VEHICLE_MAKE", vehicleMake);
                            intent.putExtra("VEHICLE_MODEL", vehicleModel);
                            intent.putExtra("RESERVATION_TYPE", reservationType);

                            startActivity(intent);
                            overridePendingTransition(R.anim.activity_fade_in, R.anim.empty);
                            // finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Reservation failed!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(ReserveMainActivity.this, "EditReservation Error", Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void fetchCurrentServerTime() {

        RestClientReservation.get().getCurrentServerTime(new Callback<ServerTimeModel>() {

            @Override
            public void success(ServerTimeModel serverTimeModel, Response response) {

                serverTime = serverTimeModel.getCurrentTime();
                serverDate = serverTimeModel.getCurrentDate();
                //System.out.println("DATE returned from server: " + serverTimeModel.getCurrentDate());
                // System.out.println("Time returned from server: " + serverTimeModel.getCurrentTime());

                initComponents();
                fetchReservationDetails();

            }


            @Override
            public void failure(RetrofitError error) {
                System.out.println("Failed to get current time from server. ");

                Toast.makeText(getApplicationContext(), "failed to get current time from server", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


    }

    private void fetchReservationDetails() {
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("UserID", "");
        siteId = getIntent().getExtras().getString("SITE_ID");


        RestClientReservation.get().getReserveView(siteId, userId, new Callback<ViewReserveModel>() {
            @Override
            public void success(ViewReserveModel viewReserveModel, Response response) {
                // tvCarName.setText(viewReserveModel.getVehiclemake() + "  " + viewReserveModel.getVehiclemodel());
                //  tvSiteId.setText(Integer.toString(viewReserveModel.getId()));
                // tvSiteOwner.setText(viewReserveModel.getSiteowner());
                // tvAddress1.setText(viewReserveModel.getAddress1());
                //  tvAddress2.setText(viewReserveModel.getCity() + "," + viewReserveModel.getProvince() +
                //         "," + viewReserveModel.getCountry() + "," + viewReserveModel.getPostalcode());

                reservationId = createReservationId();

                siteOwner = viewReserveModel.getSiteowner();
                address1 = viewReserveModel.getAddress1();
                address2 = viewReserveModel.getCity() + "," + viewReserveModel.getProvince() +
                        "," + viewReserveModel.getCountry() + "," + viewReserveModel.getPostalcode();
                level2price = viewReserveModel.getLevel2price();
                portLevel = viewReserveModel.getPortlevel();
                reservationDate = viewReserveModel.getReservedate();
                vehicleMake = viewReserveModel.getVehiclemake();
                vehicleModel = viewReserveModel.getVehiclemodel();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(ReserveMainActivity.this, "Error", Toast.LENGTH_LONG).show();
            }
        });
    }
    private String createReservationId() {
        String s = "";
        double d;
        for (int i = 1; i <= 5; i++) {
            d = Math.random() * 10;
            s = s + ((int)d);
        }
        System.out.println("unique_id:" + s);
        return s;
    }

    private String calculateReservationEndTime(String startTime){
        try{
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            Date d = df.parse(startTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            cal.add(Calendar.MINUTE, 30);
            String newTime = df.format(cal.getTime());
            return newTime;
        }catch(ParseException ex){
            System.out.println(ex);
        }
        return null;
    }



    private class ReservationWebService extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;

        protected ReservationWebService(ReserveMainActivity activity){
            pDialog = new ProgressDialog(activity);
        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Please Wait");
            pDialog.show();

            dateSelected = etSelectDate.getText().toString();


        }

        @Override
        protected String doInBackground(String... userms) {
            String return_text;

            MyURL Url = new MyURL();
            String URL = Url.getUrlR()+"/reservation";

            try {
                HttpClient httpClient = new DefaultHttpClient();
                List<NameValuePair> user = new ArrayList<>();
                user.add(new BasicNameValuePair("reservationid", reservationId));
                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat sdfDateServer = new SimpleDateFormat("dd-MM-yyyy");
                String selectedDateString = null;
                try {
                    System.out.println("DATE SELECTED: " + dateSelected);
                    Date selectedDate = sdfDate.parse(dateSelected);
                    selectedDateString = sdfDateServer.format(selectedDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                user.add(new BasicNameValuePair("reservedate", selectedDateString));
                user.add(new BasicNameValuePair("level2price", level2price));
                user.add(new BasicNameValuePair("reservestarttime", reservationStartTime));
                user.add(new BasicNameValuePair("reserve_endtime", reservationEndTime));
                user.add(new BasicNameValuePair("userid", userId));
                user.add(new BasicNameValuePair("porttype", portLevel));
                user.add(new BasicNameValuePair("siteid", siteId));
                HttpPost httpPost = new HttpPost(URL);
                httpPost.setEntity(new UrlEncodedFormEntity(user));
                System.out.println("URL-->>>>> " +  httpPost.toString());
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
            if (s.equals("not_available")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(myApp.getCurrentActivity())
                        .setTitle("Attention")
                        .setPositiveButton("Ok", null)
                        .setMessage("Please select some other time");
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                if (pDialog.isShowing()) pDialog.dismiss();
            } else {
                int newReserveId = Integer.parseInt(s);
                if (newReserveId > 0) {
                    //Toast.makeText(getApplicationContext(), "proceeding to next Step..", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ReserveMainActivity.this, ReserveSummaryActivity.class);
                    intent.putExtra("SITE_ID", siteId);
                    intent.putExtra("RESERVATION_ID", s);
                    intent.putExtra("SITE_OWNER", siteOwner);
                    intent.putExtra("ADDRESS1", address1);
                    intent.putExtra("ADDRESS2", address2);
                    intent.putExtra("RESERVATION_START_TIME", tvTimeSelected.getText().toString());
                    intent.putExtra("RESERVATION_END_TIME", reservationEndTime);
                    intent.putExtra("RESERVATION_TYPE", reservationType);
                    dateSelected = etSelectDate.getText().toString();
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat sdfDateServer = new SimpleDateFormat("dd-MM-yyyy");
                    String selectedDateString = null;
                    try {
                        Date selectedDate = sdfDate.parse(dateSelected);
                        selectedDateString = sdfDateServer.format(selectedDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    //  System.out.println("Putting to intent, date: " + selectedDateString);
                    intent.putExtra("RESERVATION_DATE", selectedDateString);
                    intent.putExtra("USER_ID", userId);
                    intent.putExtra("PORT_TYPE", portLevel);
                    intent.putExtra("VEHICLE_MAKE", vehicleMake);
                    intent.putExtra("VEHICLE_MODEL", vehicleModel);
                    if (pDialog.isShowing()) pDialog.dismiss();
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_fade_in, R.anim.empty);
                    //finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Reservation failed!", Toast.LENGTH_SHORT).show();
                    if (pDialog.isShowing()) pDialog.dismiss();
                }
            }
        }
    }

}
