package com.aidor.secchargemobile.seccharge;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
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
import com.aidor.secchargemobile.rest.RestClientReservation;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Nikhil.
 * Class for Handling payment for reservation.
 */
public class PaymentVoice extends AppCompatActivity {

    String reservationId;
    SecCharge myApp;
    ProgressDialog pDialog;
    private String cardId;
    private String last4Digits;
    private String stripteUserID;
    private String cardBrand;
    private String exp_month;
    private String exp_year;
    private String cvc;
    private TextToSpeech mainTTS;
    private TTSManager paymentVoiceTTS;
    private SpeechRecognizerManager paymentVoiceSpeechManager;

    String choosenAddress, choosenTime, userId, chosenSiteId;

    private Button defaultCardDetails, confirmPaymentButton, rejectPaymentButton;

    private int greenForSelection, redForRejection;

    private ImageSwitcher paymentImageSwitcher;

    private SpeechRecognizerAsync paymentSpeechRecognizerAsync;

    private String PaymentVoice_TAG = "PaymentVoiceTag"; // Tag for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_voice);

        myApp = (SecCharge) this.getApplicationContext();
        checkNetwork();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        pDialog = new ProgressDialog(PaymentVoice.this);

        // Get data from Intent
        cardId = getIntent().getStringExtra("cardId");
        last4Digits = getIntent().getStringExtra("last4Digits");
        stripteUserID = getIntent().getStringExtra("stripteUserID");
        cardBrand = getIntent().getStringExtra("cardBrand");
        exp_month = getIntent().getStringExtra("exp_month");
        exp_year = getIntent().getStringExtra("exp_year");
        cvc = getIntent().getStringExtra("cvc");
        cardId = getIntent().getStringExtra("cardId");
        reservationId = getIntent().getExtras().getString("RESERVATION_ID");

        // For going back to Reservation Summary activity on back pressed

        choosenAddress = getIntent().getStringExtra("ADDRESS1");
        choosenTime = getIntent().getStringExtra("RESERVATION_START_TIME");
        userId = getIntent().getExtras().getString("USER_ID");
        chosenSiteId = getIntent().getExtras().getString("SITE_ID");

        defaultCardDetails = (Button) findViewById(R.id.defaultCardDetails);
        confirmPaymentButton = (Button) findViewById(R.id.confirmPaymentButton);
        rejectPaymentButton = (Button) findViewById(R.id.rejectPaymentButton);

        String cardDetails = cardBrand + " with last four digits as " + last4Digits;
        defaultCardDetails.setText(cardDetails);

        greenForSelection = Color.parseColor(getResources().getString(R.string.colorAccent));
        redForRejection = Color.parseColor(getResources().getString(R.string.redColor));

        paymentImageSwitcher = (ImageSwitcher) findViewById(R.id.paymentImageSwitcher);

        paymentImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
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
        paymentImageSwitcher.setInAnimation(inAnimation);
        paymentImageSwitcher.setOutAnimation(outAnimation);


        paymentVoiceSpeechManager = new SpeechRecognizerManager(PaymentVoice.this,
                PaymentVoice.this.getClass().getSimpleName());


        mainTTS = TTSManager.getTts();

        Log.i(PaymentVoice_TAG, "Value of TTS is - " + mainTTS);
        Log.i(PaymentVoice_TAG, "Value of isLoaded is - " + TTSManager.isLoaded());

        paymentVoiceTTS = new TTSManager();

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
                PaymentVoice.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (utteranceId.equals("PaymentConfirm")) {
                            Log.i(PaymentVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(PaymentVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            chooseYesOrNo();
                        }
                        if (utteranceId.equals("Repeat")) {
                            Log.i(PaymentVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(PaymentVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            afterPaymentCardConfirm();
                        }
                        if (utteranceId.equals("PaymentSuccess")) {
                            Log.i(PaymentVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(PaymentVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            goToHomeActivity();
                        }
                        if (utteranceId.equals("Yes")) {
                            Log.i(PaymentVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(PaymentVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            chooseYesOrNo();
                        }
                        if (utteranceId.equals("PaymentFailure")) {
                            Log.i(PaymentVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(PaymentVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            goToHomeActivity();
                        }
                        if (utteranceId.equals("PaymentMessage")) {
                            Log.i(PaymentVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(PaymentVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            pDialog.setMessage("Please Wait, Processing your payment.");
                            pDialog.show();
                            doPayment();
                        }
                        if (utteranceId.equals("RejectPaymentMessage")) {
                            Log.i(PaymentVoice_TAG, "On Done executed for " + utteranceId);
                            Log.i(PaymentVoice_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            pDialog.setMessage("Please Wait, Taking you to home screen.");
                            pDialog.show();
                            goToHomeActivity();
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
     * Method for saying fetched card details.
     */
    private void sayResult() {
        paymentImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(PaymentVoice_TAG, "In say result before calling speak Thread name is:"
                + Thread.currentThread().getName());
        CharSequence toSpeak = "Your default" + cardBrand + " card with last four digits"
                + last4Digits + " will be used for payment. Say yes to confirm and no to deny " +
                "this card for use after the beep.";
        String utteranceId = "PaymentConfirm";
        paymentVoiceTTS.speak(toSpeak, utteranceId);
        Log.i(PaymentVoice_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    private void afterPaymentCardConfirm() {

        Log.i(PaymentVoice_TAG, "In after address confirm before calling speak Thread name is:"
                + Thread.currentThread().getName());
        CharSequence toSpeak = "Say yes to confirm.";
        String utteranceId = "Yes";
        paymentVoiceTTS.speak(toSpeak, utteranceId);
        Log.i(PaymentVoice_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method for starting speech recognition Async Task for starting speech recognition engine.
     */
    private void chooseYesOrNo() {
        paymentImageSwitcher.setImageResource(R.drawable.record_icon);
        Log.i(PaymentVoice_TAG, "Reached here after saying all the options.");
        Log.i(PaymentVoice_TAG, "In after address method. Thread name is: "
                + Thread.currentThread().getName());

        paymentSpeechRecognizerAsync = new SpeechRecognizerAsync();
        paymentSpeechRecognizerAsync.execute();
    }

    /**
     * Method for going to home activity.
     */
    private void goToHomeActivity() {
        if (pDialog.isShowing()) pDialog.dismiss();
        SpeechRecognizerManager.resultValue = 0;
        SpeechRecognizerManager.noSpeechInput = 0;
        paymentVoiceTTS.shutdown();
        Log.i(PaymentVoice_TAG, "Shutting down TTS Engine to free up resources.");
        Intent myIntent = new Intent(this, HomeActivity.class);
        startActivity(myIntent);
    }

    /**
     * Method called if no speech or incorrect speech is received.
     */
    private void repeatVoice() {
        paymentImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(PaymentVoice_TAG, "In repeat voice before calling speak Thread name is:"
                + Thread.currentThread().getName());
        CharSequence toSpeak = "You did not provide any input. Please say it again after the beep";
        String utteranceId = "Repeat";
        paymentVoiceTTS.speak(toSpeak, utteranceId);
        Log.i(PaymentVoice_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method to speak if the user confirmed payment through fetched card details.
     */
    private void confirmPaymentMessage() {
        confirmPaymentButton.setBackgroundColor(greenForSelection);
        confirmPaymentButton.setTextColor(Color.WHITE);
        paymentImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(PaymentVoice_TAG, "In confirm payment message before calling speak Thread name is:"
                + Thread.currentThread().getName());
        CharSequence toSpeak = "Please wait while your payment is being processed.";
        String utteranceId = "PaymentMessage";
        paymentVoiceTTS.speak(toSpeak, utteranceId);
        Log.i(PaymentVoice_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method to speak if the user did not confirmed payment through fetched card details.
     */
    private void rejectPaymentMessage() {
        rejectPaymentButton.setBackgroundColor(redForRejection);
        rejectPaymentButton.setTextColor(Color.WHITE);
        defaultCardDetails.setBackgroundColor(redForRejection);
        defaultCardDetails.setTextColor(Color.WHITE);
        paymentImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(PaymentVoice_TAG, "In reject payment message before calling speak Thread name is:"
                + Thread.currentThread().getName());
        CharSequence toSpeak = "Please go to your profile and change the default card from your " +
                "saved cards. Thank you for using driver mode.";
        String utteranceId = "RejectPaymentMessage";
        paymentVoiceTTS.speak(toSpeak, utteranceId);
        Log.i(PaymentVoice_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method for calling web services for doing payment.
     */
    private void doPayment() {
        Log.i(PaymentVoice_TAG, "Control comes inside reservation confirm.");

        // Calling web service for payment. Result will be in the callback
        RestClientReservation.get().doPayment("Pay", cardId, reservationId,
                new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        if (pDialog.isShowing()) pDialog.dismiss();
                        defaultCardDetails.setBackgroundColor(greenForSelection);
                        defaultCardDetails.setTextColor(Color.WHITE);
                        paymentImageSwitcher.setImageResource(R.drawable.speak_icon);
                        Log.i(PaymentVoice_TAG, "Payment status is - " + s);
                        if (s.equals("success")) {
                            Log.i(PaymentVoice_TAG, "In payment success before calling speak " +
                                    "Thread name is:" + Thread.currentThread().getName());
                            CharSequence toSpeak = "Your payment is successful and your " +
                                    "reservation is booked. Thank you.";
                            String utteranceId = "PaymentSuccess";
                            paymentVoiceTTS.speak(toSpeak, utteranceId);
                            Log.i(PaymentVoice_TAG, "During speak Thread name is:"
                                    + Thread.currentThread().getName());
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (pDialog.isShowing()) pDialog.dismiss();
                        paymentImageSwitcher.setImageResource(R.drawable.speak_icon);
                        Log.i(PaymentVoice_TAG, "In payment failure before calling speak Thread " +
                                "name is:" + Thread.currentThread().getName());
                        CharSequence toSpeak = "Your payment was not successful. Please try again.";
                        String utteranceId = "PaymentFailure";
                        paymentVoiceTTS.speak(toSpeak, utteranceId);
                        Log.i(PaymentVoice_TAG, "During speak Thread name is:"
                                + Thread.currentThread().getName());
                    }
                });
    }

    private void checkNetwork() {
        if (!isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(), "No Internet Connection!",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PaymentVoice.this, NoInternetActivity.class));
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
     * Async Task for performing speech recognition.
     */
    private class SpeechRecognizerAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(PaymentVoice_TAG, "Thread name outside async task is "
                    + Thread.currentThread().getName());

            // Speech recognition should run on UI thread only
            PaymentVoice.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(PaymentVoice_TAG, "Thread name inside async task is "
                            + Thread.currentThread().getName());
                    Log.i(PaymentVoice_TAG, "Value of application context is - "
                            + getApplicationContext());

                    paymentVoiceSpeechManager.setContextList(PaymentVoice.this);
                    paymentVoiceSpeechManager.addActivityNameToList(PaymentVoice.this.getClass()
                            .getSimpleName());
                    paymentVoiceSpeechManager.initializeSpeechRecognizer();
                }
            });

            // This is done to wait till the speech recognition is being performed. Otherwise
            // control goes ahead and synchronization is disturbed.
            try {
                Log.i(PaymentVoice_TAG, "Thread which I am trying here is "
                        + Thread.currentThread().getName());
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                Log.i(PaymentVoice_TAG, "Thread interrupted.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i(PaymentVoice_TAG, "Now executing post execute one.");
            Log.i(PaymentVoice_TAG, "Thread inside post execute is - "
                    + Thread.currentThread().getName());

            Log.i(PaymentVoice_TAG, "Result Value is - " + SpeechRecognizerManager.resultValue);
            Log.i(PaymentVoice_TAG, "No speech input value is - "
                    + SpeechRecognizerManager.noSpeechInput);

            if (SpeechRecognizerManager.resultValue == 0 &&
                    SpeechRecognizerManager.noSpeechInput == 0) {
                paymentVoiceSpeechManager.stopSpeechRecognizer();
                Log.i(PaymentVoice_TAG, "Came inside infinite loop stopper.");
                repeatVoice();
            }
            if (SpeechRecognizerManager.resultValue == 0 &&
                    SpeechRecognizerManager.noSpeechInput == 1) {
                Log.i(PaymentVoice_TAG, "Result Value is - " + SpeechRecognizerManager.resultValue);
                Log.i(PaymentVoice_TAG, "No speech input value is - "
                        + SpeechRecognizerManager.noSpeechInput);
                Log.i(PaymentVoice_TAG, "Stopped the speech recognizer in no speech input and no" +
                        " match from listening.");
                SpeechRecognizerManager.noSpeechInput = 0;
                repeatVoice();
            }

            // User confirmed payment through said card details
            if (SpeechRecognizerManager.resultValue == 1) {
                Log.i(PaymentVoice_TAG, "Coming in confirm payment message");
                confirmPaymentMessage();
            }

            // User did not confirmed payment through said card details
            if (SpeechRecognizerManager.resultValue == 2) {
                Log.i(PaymentVoice_TAG, "Coming in reject payment message");
                rejectPaymentMessage();
            }
        }
    }

    /**
     * Method for going to previous ReservationSummaryConfirmVoice activity.
     */
    private void goToPreviousActivity() {
        SpeechRecognizerManager.resultValue = 0;
        SpeechRecognizerManager.noSpeechInput = 0;
        Intent myIntent = new Intent(this, ReservationSummaryConfirmVoice.class);
        myIntent.putExtra("ADDRESS1", choosenAddress);
        myIntent.putExtra("RESERVATION_START_TIME", choosenTime);
        myIntent.putExtra("USER_ID", userId);
        myIntent.putExtra("RESERVATION_ID", reservationId);
        myIntent.putExtra("SITE_ID", chosenSiteId);
        startActivity(myIntent);
    }

    /**
     * Method for handling back press.
     */
    @Override
    public void onBackPressed() {
        Log.i(PaymentVoice_TAG, "On back press called.");

        // Stop the TTS Engine from speaking
        if (paymentVoiceTTS.checkSpeaking()) {
            paymentVoiceTTS.stopTTS();
            Log.i(PaymentVoice_TAG, "TTS stopped from speaking before going to previous activity.");
        }

        // Closing Async Task if speech recognition is in process
        Log.i(PaymentVoice_TAG, "Speech Recognizer Asyn value is - " +paymentSpeechRecognizerAsync);
        if (paymentSpeechRecognizerAsync != null) {
            paymentSpeechRecognizerAsync.cancel(true);
            Log.i(PaymentVoice_TAG, "Speech Recognizer Async cancelled.");
        }
        goToPreviousActivity();
        super.onBackPressed();
    }

    /**
     * Method written for handling unexpected closing of activity while application is running.
     */
    @Override
    protected void onStop() {
        paymentVoiceSpeechManager.destroySpeechRecognizer();
        if (paymentVoiceTTS.checkSpeaking()) {
            paymentVoiceTTS.stopTTS();
            Log.i(PaymentVoice_TAG, "TTS stopped from speaking before going to previous activity.");
        }

        // Closing Async Task if speech recognition is in process
        Log.i(PaymentVoice_TAG, "Speech Recognizer Asyn value is - " +paymentSpeechRecognizerAsync);
        if (paymentSpeechRecognizerAsync != null) {
            paymentSpeechRecognizerAsync.cancel(true);
            Log.i(PaymentVoice_TAG, "Speech Recognizer Async cancelled.");
        }
        Log.i(PaymentVoice_TAG, "Speech recognizer destroyed for this activity in on stop.");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        paymentVoiceSpeechManager.destroySpeechRecognizer();
        Log.i(PaymentVoice_TAG, "Speech recognizer destroyed for this activity in on destroy.");
        super.onDestroy();
    }
}
