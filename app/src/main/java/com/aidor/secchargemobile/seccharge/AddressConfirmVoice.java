package com.aidor.secchargemobile.seccharge;

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

/**
 * Created by Nikhil.
 * Class for handling address confirmation logic.
 */
public class AddressConfirmVoice extends AppCompatActivity {

    // UI elements
    private Button chosenAddress;
    private Button confirmAddressButton;
    private Button rejectAddressButton;

    private boolean activityChangeFlag;

    private int greenForSelection, redForRejection;

    SecCharge myApp;

    String choosenAddress;
    String choosenSiteId;
    private double latitude;
    private double longitude;

    private ImageSwitcher addressConfirmImageSwitcher;

    private TextToSpeech mainTTS;
    private TTSManager addressConfirmTTS;

    private SpeechRecognizerManager addressConfirmSpeechManager;

    private SpeechRecognizerAsync addressConfirmSpeechRecognizerAsync;

    private String Address_Confirm_TAG = "AddConfirmTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_confirm_voice);

        myApp = (SecCharge) this.getApplicationContext();
        checkNetwork();

        // Extract the values from intent
        Bundle bundle = getIntent().getExtras();
        latitude = bundle.getDouble("Latitude");
        longitude = bundle.getDouble("Longitude");
        choosenAddress = bundle.getString("Address");
        choosenSiteId = bundle.getString("Site_Id");

        Log.i(Address_Confirm_TAG, "Received value of latitude is - " + latitude);
        Log.i(Address_Confirm_TAG, "Received value of longitude is - " + longitude);
        Log.i(Address_Confirm_TAG, "Received value of choosen address is - " + choosenAddress);
        Log.i(Address_Confirm_TAG, "Received value of site id is - " + choosenSiteId);

        chosenAddress = (Button) findViewById(R.id.chosenAddress);
        confirmAddressButton = (Button) findViewById(R.id.confirmAddressButton);
        rejectAddressButton = (Button) findViewById(R.id.rejectAddressButton);

        chosenAddress.setText(choosenAddress);

        greenForSelection = Color.parseColor(getResources().getString(R.string.colorAccent));
        redForRejection = Color.parseColor(getResources().getString(R.string.redColor));

        addressConfirmSpeechManager = new SpeechRecognizerManager(AddressConfirmVoice.this,
                AddressConfirmVoice.this.getClass().getSimpleName());


        mainTTS = TTSManager.getTts();

        Log.i(Address_Confirm_TAG, "Value of TTS is - " + mainTTS);
        Log.i(Address_Confirm_TAG, "Value of isLoaded is - " + TTSManager.isLoaded());

        addressConfirmTTS = new TTSManager();

        addressConfirmImageSwitcher =
                (ImageSwitcher) findViewById(R.id.addressConfirmImageSwitcher);

        addressConfirmImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
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
        addressConfirmImageSwitcher.setInAnimation(inAnimation);
        addressConfirmImageSwitcher.setOutAnimation(outAnimation);

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
                AddressConfirmVoice.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (utteranceId.equals("Address")) {
                            Log.i(Address_Confirm_TAG, "On Done executed for " + utteranceId);
                            Log.i(Address_Confirm_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            afterAddressConfirm();
                        }
                        if (utteranceId.equals("Repeat")) {
                            Log.i(Address_Confirm_TAG, "On Done executed for " + utteranceId);
                            Log.i(Address_Confirm_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            afterAddressConfirm();
                        }
                        if (utteranceId.equals("Success")) {
                            Log.i(Address_Confirm_TAG, "On Done executed for " + utteranceId);
                            Log.i(Address_Confirm_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            activityChangeFlag = true;
                            goToSuccessActivity();
                        }
                        if (utteranceId.equals("Change")) {
                            Log.i(Address_Confirm_TAG, "On Done executed for " + utteranceId);
                            Log.i(Address_Confirm_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            activityChangeFlag = true;
                            goToPreviousActivity();
                        }
                        if (utteranceId.equals("YesOrNo")) {
                            Log.i(Address_Confirm_TAG, "On Done executed for " + utteranceId);
                            Log.i(Address_Confirm_TAG, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            chooseYesOrNo();
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
     * Method for saying chosen address.
     */
    private void sayResult() {
        addressConfirmImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(Address_Confirm_TAG, "In say result before calling speak Thread name is:"
                + Thread.currentThread().getName());
        CharSequence toSpeak = "You have chosen " + choosenAddress;
        String utteranceId = "Address";
        addressConfirmTTS.speak(toSpeak, utteranceId);
        Log.i(Address_Confirm_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method to say yes or no choice.
     */
    private void afterAddressConfirm() {

        Log.i(Address_Confirm_TAG, "In after address confirm before calling speak Thread name is:"
                + Thread.currentThread().getName());
        CharSequence toSpeak = "Say yes to confirm and no to choose another address after the " +
                "beep.";
        String utteranceId = "YesOrNo";
        addressConfirmTTS.speak(toSpeak, utteranceId);
        Log.i(Address_Confirm_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method for starting speech recognition Async Task for starting speech recognition engine.
     */
    private void chooseYesOrNo() {
        addressConfirmImageSwitcher.setImageResource(R.drawable.record_icon);
        Log.i(Address_Confirm_TAG, "Reached here after saying all the options.");
        Log.i(Address_Confirm_TAG, "In after address method. Thread name is: "
                + Thread.currentThread().getName());

        // Starting speech recognition
        addressConfirmSpeechRecognizerAsync = new SpeechRecognizerAsync();
        addressConfirmSpeechRecognizerAsync.execute();
    }

    /**
     * Method called when user confirmed the chosen address.
     */
    private void speakSuccess() {
        // Setting UI elements
        confirmAddressButton.setBackgroundColor(greenForSelection);
        confirmAddressButton.setTextColor(Color.WHITE);
        chosenAddress.setBackgroundColor(greenForSelection);
        chosenAddress.setTextColor(Color.WHITE);

        addressConfirmImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(Address_Confirm_TAG, "In speak success before calling speak Thread name is:"
                + Thread.currentThread().getName());
        CharSequence toSpeak = "Selected" + choosenAddress + "for reservation. Now choose the " +
                "time from one of the available time slots for your reservation.";
        String utteranceId = "Success";
        addressConfirmTTS.speak(toSpeak, utteranceId);
        Log.i(Address_Confirm_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method called if user did not confirm the chosen address and wants to choose another address.
     */
    private void speakChangeAddress() {
        // Updating UI
        rejectAddressButton.setBackgroundColor(redForRejection);
        rejectAddressButton.setTextColor(Color.WHITE);
        chosenAddress.setBackgroundColor(redForRejection);
        chosenAddress.setTextColor(Color.WHITE);

        addressConfirmImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(Address_Confirm_TAG, "In speak success before calling speak Thread name is:"
                + Thread.currentThread().getName());
        CharSequence toSpeak = "You chose not to select this address. Going back to previous " +
                "screen." + "Please select another address from the list.";
        String utteranceId = "Change";
        addressConfirmTTS.speak(toSpeak, utteranceId);
        Log.i(Address_Confirm_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method for progressing to next activity.
     */
    private void goToSuccessActivity() {
        SpeechRecognizerManager.resultValue = 0;
        SpeechRecognizerManager.noSpeechInput = 0;

        Intent myIntent = new Intent(this, ReservationTimeVoice.class);
        Bundle bundle = new Bundle();
        bundle.putDouble("Latitude", latitude);
        bundle.putDouble("Longitude", longitude);
        bundle.putString("Site_Id", choosenSiteId);
        bundle.putString("Chosen_Address", choosenAddress);
        bundle.putString("MODIFICATION_STATUS", "normalReservation");
        myIntent.putExtras(bundle);
        startActivity(myIntent);
    }

    /**
     * Method for going to previous activity.
     */
    private void goToPreviousActivity() {
        SpeechRecognizerManager.resultValue = 0;
        SpeechRecognizerManager.noSpeechInput = 0;
        Intent myIntent = new Intent(this, AddressSelectorVoice.class);
        Bundle bundle = new Bundle();
        bundle.putDouble("Latitude", latitude);
        bundle.putDouble("Longitude", longitude);
        myIntent.putExtras(bundle);
        startActivity(myIntent);
    }

    /**
     * Method called if no speech or incorrect speech is received.
     */
    private void repeatVoice() {
        addressConfirmImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(Address_Confirm_TAG, "In repeat voice before calling speak Thread name is:"
                + Thread.currentThread().getName());
        CharSequence toSpeak = "You did not provide any input. Please say it again after the beep";
        String utteranceId = "Repeat";
        addressConfirmTTS.speak(toSpeak, utteranceId);
        Log.i(Address_Confirm_TAG, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    private void checkNetwork() {
        if (!isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(), "No Internet Connection!",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddressConfirmVoice.this, NoInternetActivity.class));
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
            Log.i(Address_Confirm_TAG, "Thread name outside async task is "
                    + Thread.currentThread().getName());

            // Speech recognition should run on UI thread only
            AddressConfirmVoice.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(Address_Confirm_TAG, "Thread name inside async task is "
                            + Thread.currentThread().getName());
                    Log.i(Address_Confirm_TAG, "Value of application context is - "
                            + getApplicationContext());

                    addressConfirmSpeechManager.setContextList(AddressConfirmVoice.this);
                    addressConfirmSpeechManager
                            .addActivityNameToList(AddressConfirmVoice.this.getClass()
                                    .getSimpleName());
                    addressConfirmSpeechManager.initializeSpeechRecognizer();
                }
            });

            // This is done to wait till the speech recognition is being performed. Otherwise
            // control goes ahead and synchronization is disturbed.
            try {
                Log.i(Address_Confirm_TAG, "Thread which I am trying here is "
                        + Thread.currentThread().getName());
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                Log.i(Address_Confirm_TAG, "Thread interrupted.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i(Address_Confirm_TAG, "Now executing post execute one.");
            Log.i(Address_Confirm_TAG, "Thread inside post execute is - "
                    + Thread.currentThread().getName());
            Log.i(Address_Confirm_TAG, "Result Value is - " + SpeechRecognizerManager.resultValue);
            Log.i(Address_Confirm_TAG, "No speech input value is - "
                    + SpeechRecognizerManager.noSpeechInput);

            if (SpeechRecognizerManager.resultValue == 0 &&
                    SpeechRecognizerManager.noSpeechInput == 0) {
                addressConfirmSpeechManager.stopSpeechRecognizer();
                Log.i(Address_Confirm_TAG, "Came inside infinite loop stopper.");
                repeatVoice();
            }
            if (SpeechRecognizerManager.resultValue == 0 &&
                    SpeechRecognizerManager.noSpeechInput == 1) {
                Log.i(Address_Confirm_TAG, "Result Value is - "
                        + SpeechRecognizerManager.resultValue);
                Log.i(Address_Confirm_TAG, "No speech input value is - "
                        + SpeechRecognizerManager.noSpeechInput);
                Log.i(Address_Confirm_TAG, "Stopped the speech recognizer in no speech input and " +
                        "no match from listening.");
                SpeechRecognizerManager.noSpeechInput = 0;
                repeatVoice();
            }

            // User confirmed the chosen address
            if (SpeechRecognizerManager.resultValue == 1) {
                speakSuccess();
            }

            // User did not confirm and want to select another address
            if (SpeechRecognizerManager.resultValue == 2) {
                speakChangeAddress();
            }

        }
    }

    /**
     * Method for handling back press.
     */
    @Override
    public void onBackPressed() {
        Log.i(Address_Confirm_TAG, "On back press called.");
        // Stop speaking
        if (addressConfirmTTS.checkSpeaking()) {
            addressConfirmTTS.stopTTS();
            Log.i(Address_Confirm_TAG, "TTS stopped from speaking before going to previous" +
                    " activity.");
        }

        // Closing Async Task if speech recognition is in process
        Log.i(Address_Confirm_TAG, "Speech Recognizer Asyn value is - "
                + addressConfirmSpeechRecognizerAsync);
        if (addressConfirmSpeechRecognizerAsync != null) {
            addressConfirmSpeechRecognizerAsync.cancel(true);
            Log.i(Address_Confirm_TAG, "Speech Recognizer Async cancelled.");
        }
        goToPreviousActivity();
        super.onBackPressed();
    }

    /**
     * Method written for handling unexpected closing of activity while application is running.
     */
    @Override
    protected void onStop() {
        addressConfirmSpeechManager.destroySpeechRecognizer();
        Log.i(Address_Confirm_TAG, "Speech recognizer destroyed for this activity in on stop.");
        Log.i(Address_Confirm_TAG, "Value of isSpeaking - " + addressConfirmTTS.checkSpeaking());
        if (addressConfirmTTS.checkSpeaking()) {
            // This check is made because onStop also gets called when activity is changing. There
            // can be instance that engine is speaking of next activity. In that case it should
            // continue to behave in a normal fashion.
            if (activityChangeFlag) {
                Log.i(Address_Confirm_TAG, "Came from activity change. Do nothing.");
            } else {
                addressConfirmTTS.stopTTS();
                Log.i(Address_Confirm_TAG, "Value of change flag is - " + activityChangeFlag);
                Log.i(Address_Confirm_TAG, "TTS stopped from speaking in onStop.");
            }

        }

        // Closing Async Task if speech recognition is in process
        Log.i(Address_Confirm_TAG, "Speech Recognizer Asyn value is - "
                + addressConfirmSpeechRecognizerAsync);
        if (addressConfirmSpeechRecognizerAsync != null) {
            addressConfirmSpeechRecognizerAsync.cancel(true);
            Log.i(Address_Confirm_TAG, "Speech Recognizer Async cancelled in onStop.");
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        addressConfirmSpeechManager.destroySpeechRecognizer();
        Log.i(Address_Confirm_TAG, "Speech recognizer destroyed for this activity in on destroy.");
        super.onDestroy();
    }
}
