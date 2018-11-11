package com.aidor.secchargemobile.seccharge;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.custom.SecCharge;
import com.aidor.secchargemobile.model.MyReserveTime;
import com.aidor.secchargemobile.model.ReserveTimeSlotTest;
import com.aidor.secchargemobile.model.ReserveTimeSlots;
import com.aidor.secchargemobile.model.ReserveView;
import com.aidor.secchargemobile.model.ServerTimeModel;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Nikhil.
 * Class encapsulating the logic for presentation and selection of available reservation time slots.
 */
public class ReservationTimeVoice extends AppCompatActivity {

    // UI elements
    private Button time1, time2, time3, time4, time5, time6, goToHome;
    private ImageSwitcher reservationTimeImageSwitcher;

    ProgressDialog pDialog;

    private int greenForSelection, redForRejection;

    SecCharge myApp;

    String choosenAddress;
    String choosenSiteId;
    private double latitude;
    private double longitude;

    private TextToSpeech mainTTS;
    private TTSManager resTimeTTS;

    private SpeechRecognizerManager resTimeAndDateSpeechManager;

    private String ReservationTimeVoice_TAG = "ResTimeAndDateTag"; // Tag for logging.

    private String serverTime, serverDate;
    private String dateSelected;


    SharedPreferences sharedPreferences;
    String userId, siteId, siteOwner, address1, address2,level2price,
            portLevel,reservationDate,reservationEndTime, reservationId, vehicleMake,
            vehicleModel, reservationType;

    private ReserveTimeSlots reserveTimeSlots;
    private ReserveView reserveView;

    List<String> reserveTimeList;

    List<MyReserveTime> myTimeList;

    String [] slotTimesToSay = new String[6];
    String reservationStartTime;

    private SpeechRecognizerAsync reservationSpeechRecognizerAsync;

    private boolean activityChangeFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_time_voice);

        myApp = (SecCharge) this.getApplicationContext();
        checkNetwork();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        pDialog = new ProgressDialog(ReservationTimeVoice.this);

        // Get data from intent
        Bundle bundle = getIntent().getExtras();
        latitude = bundle.getDouble("Latitude");
        longitude = bundle.getDouble("Longitude");
        choosenSiteId = bundle.getString("Site_Id");
        choosenAddress = bundle.getString("Chosen_Address");
        reservationType = bundle.getString("MODIFICATION_STATUS");

        Log.i(ReservationTimeVoice_TAG, "Value of received site id is - " +choosenSiteId);
        Log.i(ReservationTimeVoice_TAG, "Value of received latitude is - " +latitude);
        Log.i(ReservationTimeVoice_TAG, "Value of received longitude is - " +longitude);
        Log.i(ReservationTimeVoice_TAG, "Value of received reservation type is - "
                +reservationType);

        // Initialize UI elements
        time1 = (Button) findViewById(R.id.time1);
        time2 = (Button)findViewById(R.id.time2);
        time3 = (Button)findViewById(R.id.time3);
        time4 = (Button)findViewById(R.id.time4);
        time5 = (Button)findViewById(R.id.time5);
        time6 = (Button)findViewById(R.id.time6);
        goToHome = (Button)findViewById(R.id.goToHome);

        greenForSelection = Color.parseColor(getResources().getString(R.string.colorAccent));
        redForRejection = Color.parseColor(getResources().getString(R.string.redColor));

        resTimeAndDateSpeechManager = new SpeechRecognizerManager(ReservationTimeVoice.this,
                ReservationTimeVoice.this.getClass().getSimpleName());


        mainTTS = TTSManager.getTts();

        Log.i(ReservationTimeVoice_TAG, "Value of TTS is - " +mainTTS);
        Log.i(ReservationTimeVoice_TAG, "Value of isLoaded is - " +TTSManager.isLoaded());

        resTimeTTS = new TTSManager();

        reservationTimeImageSwitcher =
                (ImageSwitcher) findViewById(R.id.reservationTimeImageSwitcher);

        reservationTimeImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                return imageView;
            }
        });

        //Defining animation
        Animation inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);

        //Setting the animation to imageswitcher
        reservationTimeImageSwitcher.setInAnimation(inAnimation);
        reservationTimeImageSwitcher.setOutAnimation(outAnimation);


        serverTime = "Time not set";
        serverDate = "Date from server not set";

        dateSelected = "";

        // Setting utterance progress listener for listening to TTS Engine
        mainTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            /**
             * Method for handling the flow when TTS Engine is finished speaking the utterance with
             * the given utterance id. It is called when TTS Engine completes speech synthesis from
             * the text.
             * @param utteranceId Utterance Id of the spoken text
             */
            @Override
            public void onDone(final String utteranceId) {
                // TTS Engine and its tasks should run on UI Thread
                ReservationTimeVoice.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(utteranceId.equals("FirstTime") ){
                            Log.i(ReservationTimeVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(ReservationTimeVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            saySecondTime();
                        }
                        if(utteranceId.equals("SecondTime") ){
                            Log.i(ReservationTimeVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(ReservationTimeVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            sayThirdTime();
                        }
                        if(utteranceId.equals("ThirdTime") ){
                            Log.i(ReservationTimeVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(ReservationTimeVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            sayFourthTime();
                        }
                        if(utteranceId.equals("FourthTime") ){
                            Log.i(ReservationTimeVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(ReservationTimeVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            sayFifthTime();
                        }
                        if(utteranceId.equals("FifthTime") ){
                            Log.i(ReservationTimeVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(ReservationTimeVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            saySixthTime();
                        }
                        if(utteranceId.equals("SixthTime") ){
                            Log.i(ReservationTimeVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(ReservationTimeVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            sayHomeOption();
                        }
                        if(utteranceId.equals("NoTimeFound") ){
                            Log.i(ReservationTimeVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(ReservationTimeVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            goToHomeActivity();
                        }
                        if(utteranceId.equals("HomeOption") ){
                            Log.i(ReservationTimeVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(ReservationTimeVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            chooseTime();
                        }
                        if(utteranceId.equals("TakeToHome") ){
                            Log.i(ReservationTimeVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(ReservationTimeVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            goToHomeActivity();
                        }
                        if (utteranceId.equals("Repeat")) {
                            Log.i(ReservationTimeVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(ReservationTimeVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            chooseTime();
                        }
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {

            }

        });

        fetchCurrentServerTime(); // Fetching current server time
    }

    private void chooseTime() {
        reservationTimeImageSwitcher.setImageResource(R.drawable.record_icon);
        Log.i(ReservationTimeVoice_TAG, "Reached here after saying all the options.");
        Log.i(ReservationTimeVoice_TAG, "In choose time method. Thread name is: "
                + Thread.currentThread().getName());

        reservationSpeechRecognizerAsync = new SpeechRecognizerAsync();
        reservationSpeechRecognizerAsync.execute();
    }

    private void checkValues() {
        Log.i(ReservationTimeVoice_TAG, "Site owner is - " +siteOwner);
        Log.i(ReservationTimeVoice_TAG, "Address 1 is - " +address1);
        Log.i(ReservationTimeVoice_TAG, "Address 2 is - " +address2);
        Log.i(ReservationTimeVoice_TAG, "Level 2 Price is - " +level2price);
        Log.i(ReservationTimeVoice_TAG, "Port level is - " +portLevel);
        Log.i(ReservationTimeVoice_TAG, "Reservation Date is - " +reservationDate);
        Log.i(ReservationTimeVoice_TAG, "Vehicle make is - " +vehicleMake);
        Log.i(ReservationTimeVoice_TAG, "Model is - " +vehicleModel);
        Log.i(ReservationTimeVoice_TAG, "Reserve view is - " +reserveView);
        Log.i(ReservationTimeVoice_TAG, "Reservation id of reserve view is -"
                +reserveView.getMyReservationId());
        Log.i(ReservationTimeVoice_TAG, "Reserve time list is -" +reserveTimeList);
        Log.i(ReservationTimeVoice_TAG, "My time list is - " +myTimeList);
        Log.i(ReservationTimeVoice_TAG, "First value of my time list is -"
                +myTimeList.get(0).getSlotTime());

    }

    /**
     * Method for fetching current server time through web service call.
     */
    private void fetchCurrentServerTime() {

        pDialog.setMessage("Please wait, Fetching available time slots.");
        pDialog.show();
        Log.i(ReservationTimeVoice_TAG, "Control comes here.");

        // Calling web service to fetch current server time. Result will be in the callback list
        RestClientReservation.get().getCurrentServerTime(new Callback<ServerTimeModel>() {
            @Override
            public void success(ServerTimeModel serverTimeModel, Response response) {

                serverTime = serverTimeModel.getCurrentTime();
                serverDate = serverTimeModel.getCurrentDate();

                Log.i(ReservationTimeVoice_TAG, "Current server time is - " +serverTime);
                Log.i(ReservationTimeVoice_TAG, "Current server date is - " +serverDate);
                fetchReservationDetails(); // Fetch reservation details
            }

            @Override
            public void failure(RetrofitError error) {
                Log.i(ReservationTimeVoice_TAG, "Failed to get current time from server.");
                Toast.makeText(getApplicationContext(), "Failed to get current time from server",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Method to fetch reservation details view for chosen site id.
     */
    private void fetchReservationDetails() {
        Log.i(ReservationTimeVoice_TAG, "Control comes inside fetchReservation Details.");
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("UserID", "");
        siteId = choosenSiteId;

        // Calling web service to fetch reservation view. Result will be in the callback list
        RestClientReservation.get().getReserveViewVoice(siteId, userId,
                new Callback<ReserveTimeSlotTest>() {

            @Override
            public void success(ReserveTimeSlotTest reserveTimeSlotTest, Response response) {

                // Initializing variables from model which is constructed by response
                reserveTimeSlots = reserveTimeSlotTest.getReserveTimeSlots();
                reservationId = createReservationId();
                siteOwner = reserveTimeSlots.getSiteowner();
                address1 = reserveTimeSlots.getAddress1();
                address2 = reserveTimeSlots.getCity() + "," + reserveTimeSlots.getProvince() +
                        "," + reserveTimeSlots.getCountry() + ","
                        +reserveTimeSlots.getPostalcode();
                level2price = reserveTimeSlots.getLevel2price();
                portLevel = reserveTimeSlots.getPortlevel();
                reservationDate = reserveTimeSlots.getReservedate();
                vehicleMake = reserveTimeSlots.getVehiclemake();
                vehicleModel = reserveTimeSlots.getVehiclemodel();

                reserveView = reserveTimeSlots.getReserveView();
                reserveTimeList = reserveView.getReserveTimeList();
                myTimeList = reserveView.getMyTimeList();

                constructTimeListToSay();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.i(ReservationTimeVoice_TAG, "Coming here and displaying error.");
                Toast.makeText(ReservationTimeVoice.this, "Error in fetching time slots",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Method to construct available time list to present to the user.
     */
    private void constructTimeListToSay() {
        int i = 0;
        for (MyReserveTime myReserveTime: myTimeList){
            Log.i(ReservationTimeVoice_TAG, "Slot time is - " +myReserveTime.getSlotTime());
            Log.i(ReservationTimeVoice_TAG, "Slot status is - "+myReserveTime.getDisabled());
        }
        for (MyReserveTime myReserveTime: myTimeList) {
            DateFormat sdf = new SimpleDateFormat("HH:mm");
            Date datelistTime = null, dateServerTime = null;
            Boolean datelistTimeDisabledStatus = myReserveTime.getDisabled();

            Log.i(ReservationTimeVoice_TAG, "Parsing the time - " +myReserveTime.getSlotTime());
            Log.i(ReservationTimeVoice_TAG, "Parsing the disabled status"
                    +myReserveTime.getDisabled());

            try {
                datelistTime = sdf.parse(myReserveTime.getSlotTime());
                dateServerTime = sdf.parse(serverTime);
            } catch (ParseException e) {
                Toast.makeText(getApplicationContext(), "Unable to parse time to Date object",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            Log.i(ReservationTimeVoice_TAG, "Now checking if it can be added to the say list");
            if (datelistTime.after(dateServerTime) && !datelistTimeDisabledStatus) {
                slotTimesToSay[i] = myReserveTime.getSlotTime();
                Log.i(ReservationTimeVoice_TAG, "Time added in the say list is - "
                        + slotTimesToSay[i]);
                Log.i(ReservationTimeVoice_TAG, "Slot status in the time list is - "
                        +myReserveTime.getDisabled());
                i++;
            }
            if (i>5) break;
        }
        displayTimeList();
        sayFirstTime();

    }

    /**
     * Method for displaying time slots on screen.
     */
    private void displayTimeList() {
        time1.setText(slotTimesToSay[0]);
        time2.setText(slotTimesToSay[1]);
        time3.setText(slotTimesToSay[2]);
        time4.setText(slotTimesToSay[3]);
        time5.setText(slotTimesToSay[4]);
        time6.setText(slotTimesToSay[5]);
    }

    /**
     * Method called if there is no availability.
     */
    private void sayNoTimeFound() {
        reservationTimeImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(ReservationTimeVoice_TAG, "In say no time found before calling speak Thread name is:"
                + Thread.currentThread().getName());
        time1.setText("---");
        time2.setText("---");
        time3.setText("---");
        time4.setText("---");
        time5.setText("---");
        time6.setText("---");
        CharSequence toSpeak = "Sorry, No time slots found for today. Please select another date " +
                "and time via app. Taking you to the home screen.";
        String utteranceId = "NoTimeFound";
        resTimeTTS.speak(toSpeak, utteranceId);
        Log.i(ReservationTimeVoice_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method called to speak first time.
     */
    private void sayFirstTime() {
        if (pDialog.isShowing()) pDialog.dismiss();
        if (slotTimesToSay[0] == null) {
            sayNoTimeFound();
        }
        else {
            reservationTimeImageSwitcher.setImageResource(R.drawable.speak_icon);
            Log.i(ReservationTimeVoice_TAG, "In say first time before calling speak Thread name is:"
                    + Thread.currentThread().getName());
            time1.setText(slotTimesToSay[0]);
            CharSequence toSpeak = "Following are the available time slots. Please choose one " +
                    "after hearing the options. " + "Say 1 to choose " +slotTimesToSay[0]
                    +" hours.";
            String utteranceId = "FirstTime";
            resTimeTTS.speak(toSpeak, utteranceId);
            Log.i(ReservationTimeVoice_TAG, "During speak Thread name is:"
                    + Thread.currentThread().getName());
        }
    }

    /**
     * Method called to speak second time.
     */
    private void saySecondTime() {
        if (slotTimesToSay[1] == null) {
            time2.setText("---");
            time3.setText("---");
            time4.setText("---");
            time5.setText("---");
            time6.setText("---");
            sayHomeOption();
        }
        else {
            reservationTimeImageSwitcher.setImageResource(R.drawable.speak_icon);
            Log.i(ReservationTimeVoice_TAG, "In say second time before calling speak Thread name" +
                    " is:" + Thread.currentThread().getName());
            time2.setText(slotTimesToSay[1]);
            CharSequence toSpeak = "Say 2 to choose " +slotTimesToSay[1] +" hours.";
            String utteranceId = "SecondTime";
            resTimeTTS.speak(toSpeak, utteranceId);
            Log.i(ReservationTimeVoice_TAG, "During speak Thread name is:"
                    + Thread.currentThread().getName());
        }
    }

    /**
     * Method called to speak third time.
     */
    private void sayThirdTime() {
        if (slotTimesToSay[2] == null) {
            time3.setText("---");
            time4.setText("---");
            time5.setText("---");
            time6.setText("---");
            sayHomeOption();
        }
        else {
            reservationTimeImageSwitcher.setImageResource(R.drawable.speak_icon);
            Log.i(ReservationTimeVoice_TAG, "In say third time before calling speak Thread name is:"
                    + Thread.currentThread().getName());
            time3.setText(slotTimesToSay[2]);
            CharSequence toSpeak = "Say 3 to choose " +slotTimesToSay[2] +" hours.";
            String utteranceId = "ThirdTime";
            resTimeTTS.speak(toSpeak, utteranceId);
            Log.i(ReservationTimeVoice_TAG, "During speak Thread name is:"
                    + Thread.currentThread().getName());
        }
    }

    /**
     * Method called to speak fourth time.
     */
    private void sayFourthTime() {
        if (slotTimesToSay[3] == null) {
            time4.setText("---");
            time5.setText("---");
            time6.setText("---");
            sayHomeOption();
        }
        else {
            reservationTimeImageSwitcher.setImageResource(R.drawable.speak_icon);
            Log.i(ReservationTimeVoice_TAG, "In say fourth time before calling speak Thread name" +
                    " is:" + Thread.currentThread().getName());
            time4.setText(slotTimesToSay[3]);
            CharSequence toSpeak = "Say 4 to choose " +slotTimesToSay[3] +" hours.";
            String utteranceId = "FourthTime";
            resTimeTTS.speak(toSpeak, utteranceId);
            Log.i(ReservationTimeVoice_TAG, "During speak Thread name is:"
                    + Thread.currentThread().getName());
        }
    }

    /**
     * Method called to speak fifth time.
     */
    private void sayFifthTime() {
        if (slotTimesToSay[4] == null) {
            time5.setText("---");
            time6.setText("---");
            sayHomeOption();
        }
        else {
            reservationTimeImageSwitcher.setImageResource(R.drawable.speak_icon);
            Log.i(ReservationTimeVoice_TAG, "In say fifth time before calling speak Thread name is:"
                    + Thread.currentThread().getName());
            time5.setText(slotTimesToSay[4]);
            CharSequence toSpeak = "Say 5 to choose " +slotTimesToSay[4] +" hours.";
            String utteranceId = "FifthTime";
            resTimeTTS.speak(toSpeak, utteranceId);
            Log.i(ReservationTimeVoice_TAG, "During speak Thread name is:"
                    + Thread.currentThread().getName());
        }
    }

    /**
     * Method called to speak sixth time.
     */
    private void saySixthTime() {
        if (slotTimesToSay[5] == null) {
            time6.setText("---");
            sayHomeOption();
        }
        else {
            reservationTimeImageSwitcher.setImageResource(R.drawable.speak_icon);
            Log.i(ReservationTimeVoice_TAG, "In say sixth time before calling speak Thread name is:"
                    + Thread.currentThread().getName());
            time6.setText(slotTimesToSay[5]);
            CharSequence toSpeak = "Say 6 to choose " +slotTimesToSay[5] +" hours.";
            String utteranceId = "SixthTime";
            resTimeTTS.speak(toSpeak, utteranceId);
            Log.i(ReservationTimeVoice_TAG, "During speak Thread name is:"
                    + Thread.currentThread().getName());
        }
    }

    /**
     * Method called to speak seventh home option.
     */
    private void sayHomeOption() {
        reservationTimeImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(ReservationTimeVoice_TAG, "In say sixth time before calling speak Thread name is:"
                + Thread.currentThread().getName());
        CharSequence toSpeak = "Say 7 if you want to reserve another time and go to home screen"
                +" for reserving via app. Please say your choice after the beep";
        String utteranceId = "HomeOption";
        resTimeTTS.speak(toSpeak, utteranceId);
        Log.i(ReservationTimeVoice_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method to create reservation id. This is method is taken from previous implemented logic.
     * @return Reservation id
     */
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

    /**
     * Async Task for performing speech recognition.
     */
    private class SpeechRecognizerAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(ReservationTimeVoice_TAG, "Thread name outside async task is "
                    + Thread.currentThread().getName());

            // Speech recognition should run on UI thread only
            ReservationTimeVoice.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(ReservationTimeVoice_TAG, "Thread name inside async task is "
                            + Thread.currentThread().getName());
                    Log.i(ReservationTimeVoice_TAG, "Value of application context is - "
                            +getApplicationContext());

                    resTimeAndDateSpeechManager.setContextList(ReservationTimeVoice.this);
                    resTimeAndDateSpeechManager
                            .addActivityNameToList(ReservationTimeVoice.this.getClass()
                                    .getSimpleName());
                    resTimeAndDateSpeechManager.initializeSpeechRecognizer();
                }
            });

            // This is done to wait till the speech recognition is being performed. Otherwise
            // control goes ahead and synchronization is disturbed.
            try {
                Log.i(ReservationTimeVoice_TAG, "Thread which I am trying here is "
                        + Thread.currentThread().getName());
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                Log.i(ReservationTimeVoice_TAG, "Thread interrupted.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i(ReservationTimeVoice_TAG, "Now executing post execute one.");
            Log.i(ReservationTimeVoice_TAG, "Thread inside post execute is - "
                    + Thread.currentThread().getName());

            Log.i(ReservationTimeVoice_TAG, "Result Value is - "
                    + SpeechRecognizerManager.resultValue);
            Log.i(ReservationTimeVoice_TAG, "No speech input value is - "
                    + SpeechRecognizerManager.noSpeechInput);

            if (SpeechRecognizerManager.resultValue == 0 &&
                    SpeechRecognizerManager.noSpeechInput == 0) {
                resTimeAndDateSpeechManager.stopSpeechRecognizer();
                Log.i(ReservationTimeVoice_TAG, "Came inside infinite loop stopper.");
                repeatVoice();
            }
            if (SpeechRecognizerManager.resultValue == 0 &&
                    SpeechRecognizerManager.noSpeechInput ==1) {
                Log.i(ReservationTimeVoice_TAG, "Result Value is - "
                        + SpeechRecognizerManager.resultValue);
                Log.i(ReservationTimeVoice_TAG, "No speech input value is - "
                        + SpeechRecognizerManager.noSpeechInput);

                Log.i(ReservationTimeVoice_TAG, "Stopped the speech recognizer in no speech input" +
                        " and no match from listening.");
                SpeechRecognizerManager.noSpeechInput = 0;
                repeatVoice();
            }

            // First slot time selected
            if (SpeechRecognizerManager.resultValue ==1) {
                time1.setBackgroundColor(greenForSelection);
                reservationStartTime = slotTimesToSay[0];
                reservationEndTime = calculateReservationEndTime(reservationStartTime);
                Log.i(ReservationTimeVoice_TAG, "Value of choosen time slot is -"
                        + reservationStartTime);
                goToReservationSummaryVoiceActivity();
            }
            // Second slot time selected
            if (SpeechRecognizerManager.resultValue ==2) {
                time2.setBackgroundColor(greenForSelection);
                reservationStartTime = slotTimesToSay[1];
                reservationEndTime = calculateReservationEndTime(reservationStartTime);
                Log.i(ReservationTimeVoice_TAG, "Value of choosen time slot is -"
                        + reservationStartTime);
                goToReservationSummaryVoiceActivity();
            }
            // Third slot time selected
            if (SpeechRecognizerManager.resultValue ==3) {
                time3.setBackgroundColor(greenForSelection);
                reservationStartTime = slotTimesToSay[2];
                reservationEndTime = calculateReservationEndTime(reservationStartTime);
                Log.i(ReservationTimeVoice_TAG, "Value of choosen time slot is -"
                        + reservationStartTime);
                goToReservationSummaryVoiceActivity();
            }
            // Fourth slot time selected
            if (SpeechRecognizerManager.resultValue ==4) {
                time4.setBackgroundColor(greenForSelection);
                reservationStartTime = slotTimesToSay[3];
                reservationEndTime = calculateReservationEndTime(reservationStartTime);
                Log.i(ReservationTimeVoice_TAG, "Value of choosen time slot is -"
                        + reservationStartTime);
                goToReservationSummaryVoiceActivity();
            }
            // Fifth slot time selected
            if (SpeechRecognizerManager.resultValue ==5) {
                time5.setBackgroundColor(greenForSelection);
                reservationStartTime = slotTimesToSay[4];
                reservationEndTime = calculateReservationEndTime(reservationStartTime);
                Log.i(ReservationTimeVoice_TAG, "Value of choosen time slot is -"
                        + reservationStartTime);
                goToReservationSummaryVoiceActivity();
            }
            // Sixth slot time selected
            if (SpeechRecognizerManager.resultValue ==6) {
                time6.setBackgroundColor(greenForSelection);
                reservationStartTime = slotTimesToSay[5];
                reservationEndTime = calculateReservationEndTime(reservationStartTime);
                Log.i(ReservationTimeVoice_TAG, "Value of choosen time slot is -"
                        + reservationStartTime);
                goToReservationSummaryVoiceActivity();
            }
            // Home option selected
            if (SpeechRecognizerManager.resultValue ==7) {
                goToHome.setBackgroundColor(greenForSelection);
                sayHomeResult();
            }
        }
    }

    /**
     * Method for going to next activity.
     */
    private void goToReservationSummaryVoiceActivity() {
        reservationTimeImageSwitcher.setVisibility(View.INVISIBLE);
        SpeechRecognizerManager.resultValue = 0;
        SpeechRecognizerManager.noSpeechInput = 0;
        new ReservationWebService(ReservationTimeVoice.this).execute();
    }

    private void sayHomeResult() {
        SpeechRecognizerManager.resultValue = 0;
        SpeechRecognizerManager.noSpeechInput = 0;
        reservationTimeImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(ReservationTimeVoice_TAG, "In say home result before calling speak Thread name is:"
                + Thread.currentThread().getName());
        CharSequence toSpeak = "Thank you for using driver mode. Taking you to the home screen";
        String utteranceId = "TakeToHome";
        resTimeTTS.speak(toSpeak, utteranceId);
        Log.i(ReservationTimeVoice_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method for going to home activity.
     */
    private void goToHomeActivity() {
        Intent myIntent = new Intent(this, HomeActivity.class);
        startActivity(myIntent);
    }

    /**
     * Method called if no speech or incorrect speech is received.
     */
    private void repeatVoice() {
        reservationTimeImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(ReservationTimeVoice_TAG, "In repeat voice before calling speak Thread name is:"
                + Thread.currentThread().getName());
        CharSequence toSpeak = "You did not provide any input. Please say it again after the beep";
        String utteranceId = "Repeat";
        resTimeTTS.speak(toSpeak, utteranceId);
        Log.i(ReservationTimeVoice_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method to call /reservation web service. This logic is taken from previous implemented logic
     * and tweaked to support Driver Mode.
     */
    private class ReservationWebService extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;

        protected ReservationWebService(ReservationTimeVoice activity){
            pDialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Please Wait");
            pDialog.show();

            dateSelected = serverDate;
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

                    // Going to ReservationSummaryConfirmVoice activity
                    activityChangeFlag = true;
                    Intent intent = new Intent(ReservationTimeVoice.this,
                            ReservationSummaryConfirmVoice.class);
                    intent.putExtra("SITE_ID", siteId);
                    intent.putExtra("RESERVATION_ID", s);
                    intent.putExtra("SITE_OWNER", siteOwner);
                    intent.putExtra("ADDRESS1", address1);
                    intent.putExtra("ADDRESS2", address2);
                    intent.putExtra("RESERVATION_START_TIME", reservationStartTime);
                    intent.putExtra("RESERVATION_END_TIME", reservationEndTime);
                    intent.putExtra("RESERVATION_TYPE", reservationType);
                    dateSelected = serverDate;
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat sdfDateServer = new SimpleDateFormat("dd-MM-yyyy");
                    String selectedDateString = null;
                    try {
                        Date selectedDate = sdfDate.parse(dateSelected);
                        selectedDateString = sdfDateServer.format(selectedDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    intent.putExtra("RESERVATION_DATE", selectedDateString);
                    intent.putExtra("USER_ID", userId);
                    intent.putExtra("PORT_TYPE", portLevel);
                    intent.putExtra("VEHICLE_MAKE", vehicleMake);
                    intent.putExtra("VEHICLE_MODEL", vehicleModel);
                    if (pDialog.isShowing()) pDialog.dismiss();
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Reservation failed!",
                            Toast.LENGTH_SHORT).show();
                    if (pDialog.isShowing()) pDialog.dismiss();
                }
            }
        }
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

    private void checkNetwork() {
        if (!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), "No Internet Connection!",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ReservationTimeVoice.this, NoInternetActivity.class));
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
     * Method for handling back press.
     */
    @Override
    public void onBackPressed() {
        Log.i(ReservationTimeVoice_TAG, "On back press called.");
        // Stop the TTS Engine from speaking
        if (resTimeTTS.checkSpeaking()){
            resTimeTTS.stopTTS();
            Log.i(ReservationTimeVoice_TAG, "TTS stopped from speaking before going to previous" +
                    " activity.");
        }

        // Closing Async Task if speech recognition is in process
        Log.i(ReservationTimeVoice_TAG, "Speech Recognizer Asyn value is - "
                + reservationSpeechRecognizerAsync);
        if (reservationSpeechRecognizerAsync != null) {
            reservationSpeechRecognizerAsync.cancel(true);
            Log.i(ReservationTimeVoice_TAG, "Speech Recognizer Async cancelled.");
        }
        goToPreviousActivity();
        super.onBackPressed();
    }

    /**
     * Method for going to previous activity.
     */
    private void goToPreviousActivity() {
        SpeechRecognizerManager.resultValue = 0;
        SpeechRecognizerManager.noSpeechInput = 0;
        Intent myIntent = new Intent(this, AddressConfirmVoice.class);
        Bundle bundle = new Bundle();
        bundle.putDouble("Latitude", latitude);
        bundle.putDouble("Longitude", longitude);
        bundle.putString("Address", choosenAddress);
        bundle.putString("Site_Id", choosenSiteId);
        myIntent.putExtras(bundle);
        startActivity(myIntent);
    }

    /**
     * Method written for handling unexpected closing of activity while application is running.
     */
    @Override
    protected void onStop() {
        resTimeAndDateSpeechManager.destroySpeechRecognizer();
        Log.i(ReservationTimeVoice_TAG, "Speech recognizer destroyed for this activity in on " +
                "stop.");
        if (resTimeTTS.checkSpeaking()){
            // This check is made because onStop also gets called when activity is changing. There
            // can be instance that engine is speaking of next activity. In that case it should
            // continue to behave in a normal fashion.
            if (activityChangeFlag) {
                Log.i(ReservationTimeVoice_TAG, "Came from activity change. Do nothing.");
            }
            else {
                resTimeTTS.stopTTS();
                Log.i(ReservationTimeVoice_TAG, "Value of change flag is - " +activityChangeFlag);
                Log.i(ReservationTimeVoice_TAG, "TTS stopped from speaking in onStop.");
            }
        }

        // Closing Async Task if speech recognition is in process
        Log.i(ReservationTimeVoice_TAG, "Speech Recognizer Asyn value is - "
                + reservationSpeechRecognizerAsync);
        if (reservationSpeechRecognizerAsync != null) {
            reservationSpeechRecognizerAsync.cancel(true);
            Log.i(ReservationTimeVoice_TAG, "Speech Recognizer Async cancelled.");
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        resTimeAndDateSpeechManager.destroySpeechRecognizer();
        Log.i(ReservationTimeVoice_TAG, "Speech recognizer destroyed for this activity in on" +
                " destroy.");
        super.onDestroy();
    }
}
