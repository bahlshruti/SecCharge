package com.aidor.secchargemobile.seccharge;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.custom.SecCharge;
import com.aidor.secchargemobile.model.CsModel;
import com.aidor.secchargemobile.rest.RestClientReservation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddressSelectorVoice extends AppCompatActivity {

    private SecCharge myApp;

    private double latitude;
    private double longitude;

    // Buttons on screen
    private Button firstOption;
    private Button secondOption;
    private Button thirdOption;

    // Image switcher for changing record and speak icon
    private ImageSwitcher addressSelectorImageSwitcher;

    private TextView addressesWithinMiles;

    // TTS Engine instance and TTSManager reference
    private TextToSpeech mainTTS;
    private TTSManager addressSelectorTTS;

    private SpeechRecognizerManager addressSelectorSpeechManager;

    private String AddressSelector_Tag = "AddressSelector_Tag"; // Tag for logging

    ProgressDialog pDialog;

    private List<CsModel> csModels = new ArrayList<>(); // List of charging stations

    private String[] addresses = new String[3]; // List of addresses
    private float[] distanceToAddress = new float[3]; // Distance to address array
    private String[] siteIds = new String[3];
    private String choosenAddress; // Chosen address
    private String choosenSiteId; // Site Id of chosen address

    private boolean activityChangeFlag; // Flag for handling back press

    private int greenForSelection; // Green color for selection

    private int defaultDistanceToSearch = 10;

    // Async Task for speech recognition
    private SpeechRecognizerAsync addressSelectorSpeechRecognizerAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_selector_voice);

        myApp = (SecCharge) this.getApplicationContext();
        checkNetwork();

        pDialog = new ProgressDialog(this);

        // Getting data from EngineIntializerVoice activity
        Bundle bundle = getIntent().getExtras();
        latitude = bundle.getDouble("Latitude");
        longitude = bundle.getDouble("Longitude");

        Log.i(AddressSelector_Tag, "Received value of Latitude is - " + latitude);
        Log.i(AddressSelector_Tag, "Received value of Longitude is - " + longitude);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initializing UI elements
        firstOption = (Button) findViewById(R.id.firstOption);
        secondOption = (Button) findViewById(R.id.secondOption);
        thirdOption = (Button) findViewById(R.id.thirdOption);
        addressesWithinMiles = (TextView) findViewById(R.id.addressesWithinMiles);
        greenForSelection = Color.parseColor(getResources().getString(R.string.colorAccent));

        Log.i(AddressSelector_Tag, "Value of TextView in CodeAndUICheck is - " + firstOption);

        // SpeechRecognizerManager initialization
        addressSelectorSpeechManager = new SpeechRecognizerManager(AddressSelectorVoice.this,
                AddressSelectorVoice.this.getClass().getSimpleName());

        // Assigning TTS Engine to mainTTS variable
        mainTTS = TTSManager.getTts();

        Log.i(AddressSelector_Tag, "Value of TTS is - " + mainTTS);
        Log.i(AddressSelector_Tag, "Value of isLoaded is - " + TTSManager.isLoaded());

        addressSelectorTTS = new TTSManager();

        // Operations of image switcher for changing record and speak icon on the screen
        addressSelectorImageSwitcher =
                (ImageSwitcher) findViewById(R.id.addressSelectorImageSwitcher);

        addressSelectorImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
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
        addressSelectorImageSwitcher.setInAnimation(inAnimation);
        addressSelectorImageSwitcher.setOutAnimation(outAnimation);

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
                AddressSelectorVoice.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (utteranceId.equals("AddressesWithinMiles")) {
                            Log.i(AddressSelector_Tag, "On Done executed for " + utteranceId);
                            Log.i(AddressSelector_Tag, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            sayOption1(distanceToAddress[0], addresses[0]);
                        }
                        if (utteranceId.equals("First")) {
                            Log.i(AddressSelector_Tag, "On Done executed for " + utteranceId);
                            Log.i(AddressSelector_Tag, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            sayOption2(distanceToAddress[1], addresses[1]);
                        }
                        if (utteranceId.equals("Second")) {
                            Log.i(AddressSelector_Tag, "On Done executed for " + utteranceId);
                            Log.i(AddressSelector_Tag, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            sayOption3(distanceToAddress[2], addresses[2]);

                        }
                        if (utteranceId.equals("Third")) {
                            Log.i(AddressSelector_Tag, "On Done executed for " + utteranceId);
                            Log.i(AddressSelector_Tag, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            askForChoice();
                        }
                        if (utteranceId.equals("Repeat")) {
                            Log.i(AddressSelector_Tag, "On Done executed for " + utteranceId);
                            Log.i(AddressSelector_Tag, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            afterAddress();
                        }
                        if (utteranceId.equals("NoAddressesFound")) {
                            Log.i(AddressSelector_Tag, "On Done executed for " + utteranceId);
                            Log.i(AddressSelector_Tag, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            goToHomeActivity();
                        }
                        if (utteranceId.equals("FoundAddresses")) {
                            Log.i(AddressSelector_Tag, "On Done executed for " + utteranceId);
                            Log.i(AddressSelector_Tag, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            sayOption1(distanceToAddress[0], addresses[0]);
                        }
                        if (utteranceId.equals("AskChoice")) {
                            Log.i(AddressSelector_Tag, "On Done executed for " + utteranceId);
                            Log.i(AddressSelector_Tag, "In on done Thread name is:"
                                    + Thread.currentThread().getName());
                            afterAddress();
                        }
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {

            }
        });

        fetchChargingStations(); // Fetching charging stations
    }

    /**
     * Method for going back to the home activity on back button press.
     */
    private void goToHomeActivity() {
        if (pDialog.isShowing()) pDialog.dismiss();
        addressSelectorTTS.shutdown();
        Log.i(AddressSelector_Tag, "Shutting down TTS Engine to free up resources.");
        Intent myIntent = new Intent(this, HomeActivity.class);
        startActivity(myIntent);
    }

    /**
     * Method to set the UI elements and start speaking.
     */
    private void sayAddresses() {
        if (pDialog.isShowing()) pDialog.dismiss();

        // Setting image switcher to speak icon
        addressSelectorImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(AddressSelector_Tag, "In say addresses before calling speak Thread name is:"
                + Thread.currentThread().getName());

        String textToShow = "List of addresses within " + defaultDistanceToSearch + " miles";
        addressesWithinMiles.setText(textToShow);

        firstOption.setText(distanceToAddress[0] + " km = " + addresses[0]);
        secondOption.setText(distanceToAddress[1] + " km = " + addresses[1]);
        thirdOption.setText(distanceToAddress[2] + " km = " + addresses[2]);

        String toSpeak = "Following are the list of addresses within a radius of "
                + defaultDistanceToSearch + "miles.";
        String utteranceId = "AddressesWithinMiles";
        addressSelectorTTS.speak(toSpeak, utteranceId);

        Log.i(AddressSelector_Tag, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method invoked if no address is found.
     */
    private void sayNoAddressesFound() {
        if (pDialog.isShowing()) pDialog.dismiss();
        addressSelectorImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(AddressSelector_Tag, "In say addresses before calling speak Thread name is:"
                + Thread.currentThread().getName());

        String textToShow = "No addresses found within " + defaultDistanceToSearch + " miles";
        addressesWithinMiles.setText(textToShow);

        String toSpeak = "No addresses found within a radius of " + defaultDistanceToSearch
                + "miles. Please book a charging station through app. Taking you to the home" +
                " screen.";
        String utteranceId = "NoAddressesFound";
        addressSelectorTTS.speak(toSpeak, utteranceId); // TTS Engine speaks

        Log.i(AddressSelector_Tag, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method for fetching charging stations through web service call.
     */
    private void fetchChargingStations() {

        pDialog.setMessage("Fetching charging stations near your location");
        pDialog.setCancelable(false);
        pDialog.show();

        // Calling web service to fetch charging station. Result will be in the callback list
        RestClientReservation.get().getCsSiteWithinRadius(latitude, longitude,
                defaultDistanceToSearch, new Callback<List<CsModel>>() {
                    @Override
                    public void success(List<CsModel> csSiteModels, Response response) {
                        Log.i(AddressSelector_Tag, "Came here to fetch charging stations.");
                        csModels = csSiteModels;
                        Log.i(AddressSelector_Tag, "In retrofit callback. Thread name is:"
                                + Thread.currentThread().getName());
                        checkNumberOfSitesAndCalculateDistances();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        pDialog.dismiss();
                        error.printStackTrace();

                        Log.i(AddressSelector_Tag, "Failed to fetch charging stations.");

                        Toast.makeText(getApplicationContext(), "Could not get charging sites " +
                                        "from server", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Method for checking number of sites received and calculate distance of them from current
     * location.
     */
    private void checkNumberOfSitesAndCalculateDistances() {
        Log.i(AddressSelector_Tag, "Now checking the size of csModel list.");
        Log.i(AddressSelector_Tag, "Size of csModel list is - " + csModels.size());
        if (csModels.size() < 3) {
            defaultDistanceToSearch += 5; // Default radius to search
            Log.i(AddressSelector_Tag, "Less than 3 sites. Fetching more with increased distance");

            // Fetch stations till 100 miles radius
            if (defaultDistanceToSearch == 100 && csModels.size() == 0) {
                Log.i(AddressSelector_Tag, "No sites found.");
                sayNoAddressesFound();
            } else if (defaultDistanceToSearch == 100 && csModels.size() > 0) {
                sayFoundAddress();
            } else {
                fetchChargingStations(); // Keep fetching the stations
            }
        } else {
            Log.i(AddressSelector_Tag, "Fetched at least 3 stations. Now calculating distances.");
            calculateDistances();
        }
    }

    /**
     * Method called if less than 3 addresses are found in the 100 mile radius.
     */
    private void sayFoundAddress() {
        if (pDialog.isShowing()) pDialog.dismiss();
        addressSelectorImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(AddressSelector_Tag, "In say found addresses before calling speak Thread name is:"
                + Thread.currentThread().getName());
        String textToShow = csModels.size() +"Addresses found within " + defaultDistanceToSearch +
                " miles";
        addressesWithinMiles.setText(textToShow);

        // Updating blank options UI
        if (csModels.size() == 1) {
            secondOption.setText("------");
            thirdOption.setText("------");
        }
        if (csModels.size() == 2) {
            thirdOption.setText("------");
        }

        String toSpeak = csModels.size() + " addresses found within a radius of "
                + defaultDistanceToSearch + "miles.";
        String utteranceId = "FoundAddresses";
        addressSelectorTTS.speak(toSpeak, utteranceId);

        Log.i(AddressSelector_Tag, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method for calculating distance of a charging station from current location.
     */
    private void calculateDistances() {
        Log.i(AddressSelector_Tag, "Value of latitude is - " + latitude);
        Log.i(AddressSelector_Tag, "Value of longitude is - " + longitude);
        TreeMap<Float, CsModel> distanceToCsModelMap = new TreeMap<>();
        DecimalFormat f = new DecimalFormat("##.00");
        for (CsModel csSiteModel : csModels) {
            float[] distances = new float[1];

            // Calculating the distance
            Location.distanceBetween(latitude, longitude, Double.parseDouble(csSiteModel
                            .getLatLocation()), Double.parseDouble(csSiteModel.getLongLocation()),
                    distances);
            Float distanceInKm = distances[0] / 1000;
            Log.i(AddressSelector_Tag, "Distance of " + csSiteModel.getAddress1() + " from current "
                    + "location is - " + distances[0] / 1000 + " km.");
            float roundedDistanceInKm = (float) (Math.round(distanceInKm * 100.0) / 100.0);
            float result = Float.parseFloat(f.format(distanceInKm));
            Log.i(AddressSelector_Tag, "The result after formatting is - " + result);
            Log.i(AddressSelector_Tag, "The result after formatting through round is - "
                    + roundedDistanceInKm);
            // Putting into the tree map
            distanceToCsModelMap.put(roundedDistanceInKm, csSiteModel);
        }

        // Extract top three address for speaking
        int i = 0;
        for (Map.Entry<Float, CsModel> entry : distanceToCsModelMap.entrySet()) {
            distanceToAddress[i] = entry.getKey();
            addresses[i] = entry.getValue().getAddress1();
            siteIds[i] = entry.getValue().getId();
            i++;
            if (i > 2) break;
        }

        sayAddresses();
    }

    /**
     * Method for saying option 1.
     * @param distanceToAddress Distance to address
     * @param addressToSay Address value to speak
     */
    private void sayOption1(float distanceToAddress, String addressToSay) {
        String textToDisplay = distanceToAddress + " km = " + addressToSay;
        firstOption.setText(textToDisplay);
        addressSelectorImageSwitcher.setImageResource(R.drawable.speak_icon);

        Log.i(AddressSelector_Tag, "In say options before calling speak Thread name is:"
                + Thread.currentThread().getName());
        String toSpeak = "The first address at " + distanceToAddress + " km is " + addressToSay;
        String utteranceId = "First";
        addressSelectorTTS.speak(toSpeak, utteranceId);
        Log.i(AddressSelector_Tag, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    /**
     * Method for saying option 2.
     * @param distanceToAddress Distance to address
     * @param addressToSay Address value to speak
     */
    private void sayOption2(float distanceToAddress, String addressToSay) {
        if (distanceToAddress == 0.0) {
            secondOption.setText("------");
            askForChoice();
        } else {
            Log.i(AddressSelector_Tag, "Value of second address to say is - " + addressToSay
                    + " at distance " + distanceToAddress + " km.");
            Log.i(AddressSelector_Tag, "In say option 2 before calling speak Thread name is:"
                    + Thread.currentThread().getName());
            String toSpeak = "The second address at " + distanceToAddress + " km is "
                    + addressToSay;
            String utteranceId = "Second";
            addressSelectorTTS.speak(toSpeak, utteranceId);
            Log.i(AddressSelector_Tag, "During speak Thread name is:"
                    + Thread.currentThread().getName());
        }
    }

    /**
     * Method for saying option 3.
     * @param distanceToAddress Distance to address
     * @param addressToSay Address value to speak
     */
    private void sayOption3(float distanceToAddress, String addressToSay) {
        if (distanceToAddress == 0.0) {
            thirdOption.setText("------");
            askForChoice();
        } else {
            Log.i(AddressSelector_Tag, "Value of third address to say is - " + addressToSay
                    + " at distance " + distanceToAddress + " km.");
            Log.i(AddressSelector_Tag, "In say option 3 before calling speak Thread name is:"
                    + Thread.currentThread().getName());
            String toSpeak = "The third address at " + distanceToAddress + " km is " + addressToSay;
            String utteranceId = "Third";
            addressSelectorTTS.speak(toSpeak, utteranceId);
            Log.i(AddressSelector_Tag, "During speak Thread name is:"
                    + Thread.currentThread().getName());
        }
    }

    /**
     * Method called for speaking choice options.
     */
    private void askForChoice() {
        if (csModels.size() == 1) {
            String toSpeak = "Say 1 for option 1 after the beep.";
            String utteranceId = "AskChoice";
            addressSelectorTTS.speak(toSpeak, utteranceId);
        } else if (csModels.size() == 2) {
            String toSpeak = "Choose one of the option. Say 1 for option 1, 2 for option 2 after" +
                    " the beep.";
            String utteranceId = "AskChoice";
            addressSelectorTTS.speak(toSpeak, utteranceId);
        } else {
            String toSpeak = "Choose one of the option. Say 1 for option 1, 2 for option 2 and 3" +
                    " for option 3 after the beep.";
            String utteranceId = "AskChoice";
            addressSelectorTTS.speak(toSpeak, utteranceId);
        }
    }

    /**
     * Method for starting speech recognition Async Task for starting speech recognition engine.
     */
    private void afterAddress() {
        Log.i(AddressSelector_Tag, "Reached here after saying all the options.");
        Log.i(AddressSelector_Tag, "In after address method. Thread name is: "
                + Thread.currentThread().getName());

        // Starting speech recognition
        addressSelectorSpeechRecognizerAsync = new SpeechRecognizerAsync();
        addressSelectorSpeechRecognizerAsync.execute();
    }

    /**
     * Async Task for performing speech recognition.
     */
    private class SpeechRecognizerAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(AddressSelector_Tag, "Thread name outside async task is "
                    + Thread.currentThread().getName());

            // Speech recognition should run on UI thread only
            AddressSelectorVoice.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(AddressSelector_Tag, "Thread name inside async task is "
                            + Thread.currentThread().getName());
                    Log.i(AddressSelector_Tag, "Value of application context is - "
                            + getApplicationContext());

                    // Setting context and activity names to their corresponding lists
                    addressSelectorSpeechManager.setContextList(AddressSelectorVoice.this);
                    addressSelectorSpeechManager
                            .addActivityNameToList(AddressSelectorVoice.this.getClass()
                                    .getSimpleName());
                    addressSelectorImageSwitcher.setImageResource(R.drawable.record_icon);

                    // Initializing speech recognition
                    addressSelectorSpeechManager.initializeSpeechRecognizer();
                }
            });

            // This is done to wait till the speech recognition is being performed. Otherwise
            // control goes ahead and synchronization is disturbed.
            waitForMilliSeconds(6000);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i(AddressSelector_Tag, "Now executing post execute one.");
            Log.i(AddressSelector_Tag, "Thread inside post execute is - "
                    + Thread.currentThread().getName());
            Log.i(AddressSelector_Tag, "Result Value is - "
                    + SpeechRecognizerManager.resultValue);
            Log.i(AddressSelector_Tag, "No speech input value is - "
                    + SpeechRecognizerManager.noSpeechInput);

            // Driving the logic according to the resultValue and noSpeechInput
            if (SpeechRecognizerManager.resultValue == 0 &&
                    SpeechRecognizerManager.noSpeechInput == 0) {
                addressSelectorSpeechManager.stopSpeechRecognizer();
                Log.i(AddressSelector_Tag, "Came inside infinite loop stopper.");
                repeatVoice();
            }
            if (SpeechRecognizerManager.resultValue == 0 &&
                    SpeechRecognizerManager.noSpeechInput == 1) {
                Log.i(AddressSelector_Tag, "Result Value is - "
                        + SpeechRecognizerManager.resultValue);
                Log.i(AddressSelector_Tag, "No speech input value is - "
                        + SpeechRecognizerManager.noSpeechInput);
                Log.i(AddressSelector_Tag, "Stopped the speech recognizer in no speech input and" +
                        " no match from listening.");
                SpeechRecognizerManager.noSpeechInput = 0;
                repeatVoice();
            }

            // If user chooses first address
            if (SpeechRecognizerManager.resultValue == 1) {
                firstOption.setBackgroundColor(greenForSelection);
                choosenAddress = addresses[0];
                choosenSiteId = siteIds[0];
                Log.i(AddressSelector_Tag, "Value of choosen address is - " + choosenAddress);
                Log.i(AddressSelector_Tag, "Value of choosen site id is - " + choosenSiteId);
                goToAddressConfirmActivity();
            }
            // If user chooses second address
            if (SpeechRecognizerManager.resultValue == 2) {
                secondOption.setBackgroundColor(greenForSelection);
                choosenAddress = addresses[1];
                choosenSiteId = siteIds[1];
                Log.i(AddressSelector_Tag, "Value of choosen address is - " + choosenAddress);
                Log.i(AddressSelector_Tag, "Value of choosen site id is - " + choosenSiteId);
                goToAddressConfirmActivity();
            }
            // If user chooses third address
            if (SpeechRecognizerManager.resultValue == 3) {
                thirdOption.setBackgroundColor(greenForSelection);
                choosenAddress = addresses[2];
                choosenSiteId = siteIds[2];
                Log.i(AddressSelector_Tag, "Value of choosen address is - " + choosenAddress);
                Log.i(AddressSelector_Tag, "Value of choosen site id is - " + choosenSiteId);
                goToAddressConfirmActivity();
            }
        }
    }

    /**
     * Method for going to Address Confirm activity for further processing.
     */
    private void goToAddressConfirmActivity() {
        addressSelectorImageSwitcher.setVisibility(View.INVISIBLE);

        // Setting flags back to default values
        SpeechRecognizerManager.resultValue = 0;
        SpeechRecognizerManager.noSpeechInput = 0;
        activityChangeFlag = true;

        // Spawning the intent to change activities
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
     * Method called if no speech or incorrect speech is received.
     */
    private void repeatVoice() {
        addressSelectorImageSwitcher.setImageResource(R.drawable.speak_icon);
        Log.i(AddressSelector_Tag, "In repeat voice before calling speak Thread name is:"
                + Thread.currentThread().getName());
        CharSequence toSpeak = "You did not provide any input. Please say it again after the beep";
        String utteranceId = "Repeat";
        addressSelectorTTS.speak(toSpeak, utteranceId);
        Log.i(AddressSelector_Tag, "During speak Thread name is:"
                + Thread.currentThread().getName());
    }

    private void waitForMilliSeconds(int milliseconds) {
        try {
            Log.i(AddressSelector_Tag, "Thread which I am trying here is "
                    + Thread.currentThread().getName());
            Log.i(AddressSelector_Tag, "Waiting for " + milliseconds + " milliseconds.");
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Log.i(AddressSelector_Tag, "Thread interrupted.");
        }
    }

    private void checkNetwork() {
        if (!isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(), "No Internet Connection!",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddressSelectorVoice.this, NoInternetActivity.class));
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
     * Method written for handling unexpected closing of activity while application is running.
     */
    @Override
    protected void onStop() {
        addressSelectorSpeechManager.destroySpeechRecognizer();
        Log.i(AddressSelector_Tag, "Speech recognizer destroyed for this activity in on stop.");
        Log.i(AddressSelector_Tag, "Value of isSpeaking - " + addressSelectorTTS.checkSpeaking());
        if (addressSelectorTTS.checkSpeaking()) {
            // This check is made because onStop also gets called when activity is changing. There
            // can be instance that engine is speaking of next activity. In that case it should
            // continue to behave in a normal fashion.
            if (activityChangeFlag) {
                Log.i(AddressSelector_Tag, "Came from activity change. Do nothing.");
            } else {
                addressSelectorTTS.stopTTS();
                Log.i(AddressSelector_Tag, "Value of change flag is - " + activityChangeFlag);
                Log.i(AddressSelector_Tag, "TTS stopped from speaking in onStop.");
            }

        }

        // Closing Async Task if speech recognition is in process
        Log.i(AddressSelector_Tag, "Speech Recognizer Asyn value is - "
                + addressSelectorSpeechRecognizerAsync);
        if (addressSelectorSpeechRecognizerAsync != null) {
            addressSelectorSpeechRecognizerAsync.cancel(true);
            Log.i(AddressSelector_Tag, "Speech Recognizer Async cancelled in onStop.");
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        addressSelectorSpeechManager.destroySpeechRecognizer();
        Log.i(AddressSelector_Tag, "Speech recognizer destroyed for this activity in on destroy.");
        super.onDestroy();
    }

    /**
     * Method for handling back press.
     */
    @Override
    public void onBackPressed() {
        Log.i(AddressSelector_Tag, "On back press called.");
        if (addressSelectorTTS.checkSpeaking()) {
            addressSelectorTTS.stopTTS();
            Log.i(AddressSelector_Tag, "TTS stopped from speaking before going to previous " +
                    "activity.");
        }

        // Closing Async Task if speech recognition is in process
        Log.i(AddressSelector_Tag, "Speech Recognizer Asyn value is - "
                + addressSelectorSpeechRecognizerAsync);
        if (addressSelectorSpeechRecognizerAsync != null) {
            addressSelectorSpeechRecognizerAsync.cancel(true);
            Log.i(AddressSelector_Tag, "Speech Recognizer Async cancelled.");
        }

        SpeechRecognizerManager.resultValue = 0;
        SpeechRecognizerManager.noSpeechInput = 0;
        addressSelectorTTS.shutdown();
        Log.i(AddressSelector_Tag, "Shutting down the TTS.");
        Intent myIntent = new Intent(this, VoicePermissionCheck.class);
        startActivity(myIntent);
        super.onBackPressed();
    }
}
