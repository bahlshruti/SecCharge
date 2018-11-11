package com.aidor.secchargemobile.seccharge;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import com.aidor.secchargemobile.model.CardInfo;
import com.aidor.secchargemobile.model.CardInfoTest;
import com.aidor.secchargemobile.rest.RestClientReservation;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Nikhil.
 * Class encapsulating the logic of reservation location and time confirmation.
 */
public class ReservationSummaryConfirmVoice extends AppCompatActivity {

    // UI elements
    private Button reservationSummary;
    private Button confirmSummaryButton;
    private Button rejectSummaryButton;

    private int greenForSelection, redForRejection;

    SecCharge myApp;

    String choosenAddress, choosenTime, userId, reservationId, chosenSiteId;

    List<CardInfo> cardInfoList;
    private String cardId;
    private String last4Digits;
    private String stripteUserID;
    private String cardBrand;
    private int exp_month;
    private int exp_year;
    private String cvc;

    ProgressDialog pDialog;

    private ImageSwitcher reservationSummaryImageSwitcher;

    private TextToSpeech mainTTS;
    private TTSManager resTimeAndDateConfirmTTS;

    private SpeechRecognizerManager resTimeAndDateConfirmSpeechManager;
    private SpeechRecognizerAsync resConfirmSpeechRecognizerAsync;

    private boolean activityChangeFlag;

    private String ReservationTimeAndDateConfirm_TAG = "ResTimeDateConfirmTag"; // Tag for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_summary_confirm_voice);

        myApp = (SecCharge) this.getApplicationContext();
        checkNetwork();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        pDialog = new ProgressDialog(ReservationSummaryConfirmVoice.this);

        // Get data from Intent
        choosenAddress = getIntent().getStringExtra("ADDRESS1");
        choosenTime = getIntent().getStringExtra("RESERVATION_START_TIME");
        userId = getIntent().getExtras().getString("USER_ID");
        reservationId = getIntent().getExtras().getString("RESERVATION_ID");
        chosenSiteId = getIntent().getExtras().getString("SITE_ID");

        Log.i(ReservationTimeAndDateConfirm_TAG, "Received value of choosen address is - "
                +choosenAddress);

        reservationSummary = (Button) findViewById(R.id.reservationSummary);
        confirmSummaryButton = (Button) findViewById(R.id.confirmSummaryButton);
        rejectSummaryButton = (Button) findViewById(R.id.rejectSummaryButton);

        String reservationSummaryToPrint = choosenAddress + " at " +choosenTime;
        reservationSummary.setText(reservationSummaryToPrint);

        greenForSelection = Color.parseColor(getResources().getString(R.string.colorAccent));
        redForRejection = Color.parseColor(getResources().getString(R.string.redColor));

        resTimeAndDateConfirmSpeechManager =
                new SpeechRecognizerManager(ReservationSummaryConfirmVoice.this,
                        ReservationSummaryConfirmVoice.this.getClass().getSimpleName());


        mainTTS = TTSManager.getTts();

        Log.i(ReservationTimeAndDateConfirm_TAG, "Value of TTS is - " +mainTTS);
        Log.i(ReservationTimeAndDateConfirm_TAG, "Value of isLoaded is - " +TTSManager.isLoaded());

        resTimeAndDateConfirmTTS = new TTSManager();

        reservationSummaryImageSwitcher =
                (ImageSwitcher) findViewById(R.id.reservationSummaryImageSwitcher);

        reservationSummaryImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
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

        //Setting the animation to image switcher
        reservationSummaryImageSwitcher.setInAnimation(inAnimation);
        reservationSummaryImageSwitcher.setOutAnimation(outAnimation);

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
                ReservationSummaryConfirmVoice.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(utteranceId.equals("Reservation") ){
                            Log.i(ReservationTimeAndDateConfirm_TAG, "On Done executed for "
                                    + utteranceId);
                            Log.i(ReservationTimeAndDateConfirm_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            afterTimeAndDateConfirm();
                        }
                        if (utteranceId.equals("Repeat")) {
                            Log.i(ReservationTimeAndDateConfirm_TAG, "On Done executed for "
                                    + utteranceId);
                            Log.i(ReservationTimeAndDateConfirm_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            afterTimeAndDateConfirm();
                        }
                        if (utteranceId.equals("YesOrNo")) {
                            Log.i(ReservationTimeAndDateConfirm_TAG, "On Done executed for "
                                    + utteranceId);
                            Log.i(ReservationTimeAndDateConfirm_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            chooseYesOrNo();
                        }
                        if (utteranceId.equals("ConfirmAndFetchDefaultCard")) {
                            Log.i(ReservationTimeAndDateConfirm_TAG, "On Done executed for "
                                    + utteranceId);
                            Log.i(ReservationTimeAndDateConfirm_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            pDialog.setMessage("Confirming reservation and fetching your card " +
                                    "details.");
                            pDialog.show();
                            reservationConfirm();
                        }
                        if (utteranceId.equals("ChangeTime")) {
                            Log.i(ReservationTimeAndDateConfirm_TAG, "On Done executed for "
                                    + utteranceId);
                            Log.i(ReservationTimeAndDateConfirm_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            goToReservationTimeVoice();
                        }
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {

            }
        });

        sayResult();
    }

    /**
     * Method called for saying the chosen address and time.
     */
    private void sayResult() {
        reservationSummaryImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(ReservationTimeAndDateConfirm_TAG, "In say result before calling speak Thread name " +
                "is:" + Thread.currentThread().getName());
        CharSequence toSpeak = "You have chosen " +choosenAddress +" to reserve from "
                +choosenTime +" hours.";
        String utteranceId = "Reservation";
        resTimeAndDateConfirmTTS.speak(toSpeak, utteranceId);
        Log.i(ReservationTimeAndDateConfirm_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method for saying yes or no option.
     */
    private void afterTimeAndDateConfirm() {

        Log.i(ReservationTimeAndDateConfirm_TAG, "In after address confirm before calling speak" +
                " Thread name is:" + Thread.currentThread().getName());
        CharSequence toSpeak = "Say yes to confirm or no to choose another address and time from " +
                "the list after the beep.";
        String utteranceId = "YesOrNo";
        resTimeAndDateConfirmTTS.speak(toSpeak, utteranceId);
        Log.i(ReservationTimeAndDateConfirm_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method for starting speech recognition Async Task for starting speech recognition engine.
     */
    private void chooseYesOrNo() {
        reservationSummaryImageSwitcher.setImageResource(R.drawable.record_icon);
        Log.i(ReservationTimeAndDateConfirm_TAG, "Reached here after saying all the options.");
        Log.i(ReservationTimeAndDateConfirm_TAG, "In after address method. Thread name is: "
                + Thread.currentThread().getName());

        // Starting speech recognition
        resConfirmSpeechRecognizerAsync = new SpeechRecognizerAsync();
        resConfirmSpeechRecognizerAsync.execute();
    }

    /**
     * Async Task for performing speech recognition.
     */
    private class SpeechRecognizerAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(ReservationTimeAndDateConfirm_TAG, "Thread name outside async task is "
                    + Thread.currentThread().getName());

            // Speech recognition should run on UI thread only
            ReservationSummaryConfirmVoice.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(ReservationTimeAndDateConfirm_TAG, "Thread name inside async task is "
                            + Thread.currentThread().getName());
                    Log.i(ReservationTimeAndDateConfirm_TAG, "Value of application context is - "
                            +getApplicationContext());

                    resTimeAndDateConfirmSpeechManager
                            .setContextList(ReservationSummaryConfirmVoice.this);
                    resTimeAndDateConfirmSpeechManager
                            .addActivityNameToList(ReservationSummaryConfirmVoice.this.getClass()
                                    .getSimpleName());
                    resTimeAndDateConfirmSpeechManager.initializeSpeechRecognizer();
                }
            });

            // This is done to wait till the speech recognition is being performed. Otherwise
            // control goes ahead and synchronization is disturbed.
            try {
                Log.i(ReservationTimeAndDateConfirm_TAG, "Thread which I am trying here is "
                        + Thread.currentThread().getName());
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                Log.i(ReservationTimeAndDateConfirm_TAG, "Thread interrupted.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i(ReservationTimeAndDateConfirm_TAG, "Now executing post execute one.");
            Log.i(ReservationTimeAndDateConfirm_TAG, "Thread inside post execute is - "
                    + Thread.currentThread().getName());

            Log.i(ReservationTimeAndDateConfirm_TAG, "Result Value is - "
                    + SpeechRecognizerManager.resultValue);
            Log.i(ReservationTimeAndDateConfirm_TAG, "No speech input value is - "
                    + SpeechRecognizerManager.noSpeechInput);

            if (SpeechRecognizerManager.resultValue == 0 &&
                    SpeechRecognizerManager.noSpeechInput == 0) {
                resTimeAndDateConfirmSpeechManager.stopSpeechRecognizer();
                Log.i(ReservationTimeAndDateConfirm_TAG, "Came inside infinite loop stopper.");
                repeatVoice();
            }
            if (SpeechRecognizerManager.resultValue == 0 &&
                    SpeechRecognizerManager.noSpeechInput ==1) {
                Log.i(ReservationTimeAndDateConfirm_TAG, "Result Value is - "
                        + SpeechRecognizerManager.resultValue);
                Log.i(ReservationTimeAndDateConfirm_TAG, "No speech input value is - "
                        + SpeechRecognizerManager.noSpeechInput);
                Log.i(ReservationTimeAndDateConfirm_TAG, "Stopped the speech recognizer in no " +
                        "speech input and no match from listening.");
                SpeechRecognizerManager.noSpeechInput = 0;
                repeatVoice();
            }

            // User confirmed the reservation
            if (SpeechRecognizerManager.resultValue ==1) {
                confirmAndFetchDefaultCard();
            }

            // User didn't confirm the reservation
            if (SpeechRecognizerManager.resultValue == 2) {
                speakChangeTime();
            }
        }
    }

    /**
     * Method called if the user selected to change the reservation summary
     */
    private void speakChangeTime() {
        rejectSummaryButton.setBackgroundColor(redForRejection);
        rejectSummaryButton.setTextColor(Color.WHITE);
        reservationSummary.setBackgroundColor(redForRejection);
        reservationSummary.setTextColor(Color.WHITE);
        reservationSummaryImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(ReservationTimeAndDateConfirm_TAG, "In speak change time before calling speak " +
                "Thread name is:" + Thread.currentThread().getName());
        CharSequence toSpeak = "You chose not to select this time. Going back to previous " +
        "screen." + "Please select another time from the list.";
        String utteranceId = "ChangeTime";
        resTimeAndDateConfirmTTS.speak(toSpeak, utteranceId);
        Log.i(ReservationTimeAndDateConfirm_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method called to go back to ReservationTimeVoice activity
     */
    private void goToReservationTimeVoice() {
        SpeechRecognizerManager.resultValue = 0;
        SpeechRecognizerManager.noSpeechInput = 0;
        activityChangeFlag = true;
        Intent myIntent = new Intent(this, ReservationTimeVoice.class);
        myIntent.putExtra("Site_Id", chosenSiteId);
        myIntent.putExtra("MODIFICATION_STATUS", "normalReservation");
        startActivity(myIntent);
    }

    /**
     * Method called if no speech or incorrect speech is received.
     */
    private void repeatVoice() {
        reservationSummaryImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(ReservationTimeAndDateConfirm_TAG, "In repeat voice before calling speak Thread " +
                "name is:" + Thread.currentThread().getName());
        CharSequence toSpeak = "You did not provide any input. Please say it again after the beep";
        String utteranceId = "Repeat";
        resTimeAndDateConfirmTTS.speak(toSpeak, utteranceId);
        Log.i(ReservationTimeAndDateConfirm_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method called to say reservation confirm and fetch card details.
     */
    private void confirmAndFetchDefaultCard() {
        confirmSummaryButton.setBackgroundColor(greenForSelection);
        confirmSummaryButton.setTextColor(Color.WHITE);
        reservationSummary.setBackgroundColor(greenForSelection);
        reservationSummary.setTextColor(Color.WHITE);
        reservationSummaryImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(ReservationTimeAndDateConfirm_TAG, "In repeat voice before calling speak Thread " +
                "name is:" + Thread.currentThread().getName());
        CharSequence toSpeak = "Please wait while we are confirming your reservation and " +
                "fetching your default card details.";
        String utteranceId = "ConfirmAndFetchDefaultCard";
        resTimeAndDateConfirmTTS.speak(toSpeak, utteranceId);
        Log.i(ReservationTimeAndDateConfirm_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method to confirm the reservation.
     */
    private void reservationConfirm() {
        // Calling web service to confirm reservation. Card details will be in the callback
        RestClientReservation.get().postReservationConfirm("Confirm", reservationId, userId,
                new Callback<CardInfoTest>() {

            @Override
            public void success(CardInfoTest cardInfoTest, Response response) {
                cardInfoList = cardInfoTest.getCardInfos();
                for (CardInfo cardInfo: cardInfoList) {
                    cardId = cardInfo.getCardId();
                    Log.i(ReservationTimeAndDateConfirm_TAG, "Card id is - " +cardId);
                    last4Digits = cardInfo.getLast4Digits();
                    Log.i(ReservationTimeAndDateConfirm_TAG, "Last 4 digits are - " +last4Digits);
                    stripteUserID = cardInfo.getStripteUserID();
                    Log.i(ReservationTimeAndDateConfirm_TAG, "Stripe User ID is - " +stripteUserID);
                    cardBrand = cardInfo.getCardBrand();
                    Log.i(ReservationTimeAndDateConfirm_TAG, "Card brand is - " +cardBrand);
                    exp_month = cardInfo.getExp_month();
                    Log.i(ReservationTimeAndDateConfirm_TAG, "Expiry month is - " +exp_month);
                    exp_year = cardInfo.getExp_year();
                    Log.i(ReservationTimeAndDateConfirm_TAG, "Expiry year is - " +exp_year);
                    cvc = cardInfo.getCvc();
                    Log.i(ReservationTimeAndDateConfirm_TAG, "Cvc is - " +cvc);
                }
                goToPaymentInfo();
            }

            @Override
            public void failure(RetrofitError error) {
                if (pDialog.isShowing()) pDialog.dismiss();
                Log.i(ReservationTimeAndDateConfirm_TAG, "Coming here and displaying error.");
                Toast.makeText(ReservationSummaryConfirmVoice.this, "Error in fetching card details"
                        , Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Method to go to PaymentVoice activity.
     */
    private void goToPaymentInfo() {
        SpeechRecognizerManager.resultValue = 0;
        SpeechRecognizerManager.noSpeechInput = 0;
        activityChangeFlag = true;
        Intent myIntent = new Intent(this, PaymentVoice.class);
        myIntent.putExtra("cardId", cardId);
        myIntent.putExtra("last4Digits", last4Digits);
        myIntent.putExtra("stripteUserID", stripteUserID);
        myIntent.putExtra("cardBrand", cardBrand);
        myIntent.putExtra("exp_month", Integer.toString(exp_month));
        myIntent.putExtra("exp_year", Integer.toString(exp_year));
        myIntent.putExtra("cvc", cvc);
        myIntent.putExtra("RESERVATION_ID", reservationId);
        if (pDialog.isShowing()) pDialog.dismiss();
        startActivity(myIntent);
    }

    private void checkNetwork() {
        if (!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), "No Internet Connection!",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ReservationSummaryConfirmVoice.this,
                    NoInternetActivity.class));
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
        Log.i(ReservationTimeAndDateConfirm_TAG, "On back press called.");
        // Stop the TTS Engine from speaking
        if (resTimeAndDateConfirmTTS.checkSpeaking()){
            resTimeAndDateConfirmTTS.stopTTS();
            Log.i(ReservationTimeAndDateConfirm_TAG, "TTS stopped from speaking before going to" +
                    " previous activity.");
        }

        // Closing Async Task if speech recognition is in process
        Log.i(ReservationTimeAndDateConfirm_TAG, "Speech Recognizer Asyn value is - "
                + resConfirmSpeechRecognizerAsync);
        if (resConfirmSpeechRecognizerAsync != null) {
            resConfirmSpeechRecognizerAsync.cancel(true);
            Log.i(ReservationTimeAndDateConfirm_TAG, "Speech Recognizer Async cancelled.");
        }
        goToReservationTimeVoice();
        super.onBackPressed();
    }

    /**
     * Method written for handling unexpected closing of activity while application is running.
     */
    @Override
    protected void onStop() {
        resTimeAndDateConfirmSpeechManager.destroySpeechRecognizer();
        Log.i(ReservationTimeAndDateConfirm_TAG, "Speech recognizer destroyed for this activity " +
                "in on stop.");
        if (resTimeAndDateConfirmTTS.checkSpeaking()){
            // This check is made because onStop also gets called when activity is changing. There
            // can be instance that engine is speaking of next activity. In that case it should
            // continue to behave in a normal fashion.
            if (activityChangeFlag) {
                Log.i(ReservationTimeAndDateConfirm_TAG, "Came from activity change. Do nothing.");
            }
            else {
                resTimeAndDateConfirmTTS.stopTTS();
                Log.i(ReservationTimeAndDateConfirm_TAG, "Value of change flag is - "
                        +activityChangeFlag);
                Log.i(ReservationTimeAndDateConfirm_TAG, "TTS stopped from speaking in onStop.");
            }
        }

        // Closing Async Task if speech recognition is in process
        Log.i(ReservationTimeAndDateConfirm_TAG, "Speech Recognizer Asyn value is - "
                + resConfirmSpeechRecognizerAsync);
        if (resConfirmSpeechRecognizerAsync != null) {
            resConfirmSpeechRecognizerAsync.cancel(true);
            Log.i(ReservationTimeAndDateConfirm_TAG, "Speech Recognizer Async cancelled.");
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        resTimeAndDateConfirmSpeechManager.destroySpeechRecognizer();
        Log.i(ReservationTimeAndDateConfirm_TAG, "Speech recognizer destroyed for this activity " +
                "in on destroy.");
        super.onDestroy();
    }
}
