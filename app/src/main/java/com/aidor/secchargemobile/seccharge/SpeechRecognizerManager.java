package com.aidor.secchargemobile.seccharge;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Nikhil on 08/12/2016.
 * Manager for interacting with speech recognition.
 */

public class SpeechRecognizerManager extends Application implements RecognitionListener {

    private SpeechRecognizer speech = null; // Main speech recognizer variable
    private String TAG = "SpeechRecogManager"; // Tag for logging
    private Context myContext; // Context value

    private String activityName; // Activity name

    private List<Context> contextList = new ArrayList<>(); // Context List
    private List<String> acitivityNameList = new ArrayList<>(); // Activity names list
    private int counter = 0;

    public static int resultValue = 0; // Result value to set for helping calling activity
    public static int noSpeechInput = 0; // Value if there is no speech input

    /**
     * Constructor for initialization.
     * @param context Context name
     * @param name Activity name
     */
    public SpeechRecognizerManager(Context context, String name) {
        myContext = context;
        activityName = name;
    }

    /**
     * Method for initializing speech recognition engine.
     */
    public void initializeSpeechRecognizer() {

        // Checks if the calling activity has created a speech recognizer and creates if no
        // speech recognizer for calling activity exists
        if (acitivityNameList.contains(activityName) && counter != 0) {
            Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            Log.i(TAG, "Counter value is - " + counter);
            Log.i(TAG, "Speech recognizer with this context exists. Now starting it.");
            Log.i(TAG, "Speech Recognizer component's value is - " + speech);
            Log.i(TAG, "Context value is -" + myContext);
            Log.i(TAG, "Activity name value is - " + activityName);
            speech.startListening(recognizerIntent); // Starting speech recognition
        } else {
            speech = SpeechRecognizer.createSpeechRecognizer(myContext);
            speech.setRecognitionListener(this);
            Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                    10000);
            Log.i(TAG, "Speech recognizer created. Now starting it.");
            Log.i(TAG, "Speech Recognizer component's value is - " + speech);
            Log.i(TAG, "Context value is -" + myContext);
            Log.i(TAG, "Activity name value is - " + activityName);
            speech.startListening(recognizerIntent);
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.i(TAG, "Inside on Ready for Speech");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(TAG, "Inside on Beginning of Speech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(TAG, "Inside on Rms Changed");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(TAG, "Inside on Buffer Received");
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(TAG, "Inside on End of Speech");
    }

    /**
     * Callback method for handling results after successful speech recognition.
     * @param results Results from speech recognition engine
     */
    @Override
    public void onResults(Bundle results) {
        ArrayList<String> voiceResult = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        StringBuilder builder = new StringBuilder();
        for (String details : voiceResult) {
            builder.append(details + "\n");

            // Check for each activity and set the flag accordingly
            if (activityName.equals("AddressSelectorVoice")) {
                if (details.toUpperCase().contains("ONE") || details.contains("1") ||
                        details.toUpperCase().contains("WON")) {
                    resultValue = 1;
                }

                if (details.toUpperCase().contains("TWO") || details.contains("2") ||
                        details.toUpperCase().contains("TO")) {
                    resultValue = 2;
                }

                if (details.toUpperCase().contains("THREE") || details.contains("3")) {
                    resultValue = 3;
                }
            }

            if (activityName.equals("AddressConfirmVoice")) {
                if (details.toUpperCase().contains("YES") || details.toUpperCase().contains("YA")
                        || details.toUpperCase().contains("YEAH") ||
                        details.toUpperCase().contains("YEA")) {
                    resultValue = 1;
                }
                if (details.toUpperCase().contains("NO") || details.toUpperCase().contains("NAH")) {
                    resultValue = 2;
                }
            }

            if (activityName.equals("ReservationTimeVoice")) {
                if (details.toUpperCase().contains("ONE") || details.contains("1") ||
                        details.toUpperCase().contains("WON")) {
                    resultValue = 1;
                }

                if (details.toUpperCase().contains("TWO") || details.contains("2") ||
                        details.toUpperCase().contains("TO")) {
                    resultValue = 2;
                }

                if (details.toUpperCase().contains("THREE") || details.contains("3")) {
                    resultValue = 3;
                }
                if (details.toUpperCase().contains("FOUR") || details.contains("4") ||
                        details.toUpperCase().contains("FOR")) {
                    resultValue = 4;
                }

                if (details.toUpperCase().contains("FIVE") || details.contains("5")) {
                    resultValue = 5;
                }

                if (details.toUpperCase().contains("SIX") || details.contains("6")) {
                    resultValue = 6;
                }

                if (details.toUpperCase().contains("SEVEN") || details.contains("7")) {
                    resultValue = 7;
                }
            }

            if (activityName.equals("ReservationSummaryConfirmVoice")) {
                if (details.toUpperCase().contains("YES") || details.toUpperCase().contains("YA")
                        || details.toUpperCase().contains("YEAH") ||
                        details.toUpperCase().contains("YEA")) {
                    resultValue = 1;
                }
                if (details.toUpperCase().contains("NO") || details.toUpperCase().contains("NAH")) {
                    resultValue = 2;
                }
            }

            if (activityName.equals("PaymentVoice")) {
                if (details.toUpperCase().contains("YES") || details.toUpperCase().contains("YA")
                        || details.toUpperCase().contains("YEAH") ||
                        details.toUpperCase().contains("YEA")) {
                    resultValue = 1;
                }
                if (details.toUpperCase().contains("NO") || details.toUpperCase().contains("NAH")) {
                    resultValue = 2;
                }
            }
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    /**
     * Callback method for handling error and generating error message for returned error code from
     * speech recognition engine.
     * @param error Error code
     */
    @Override
    public void onError(int error) {
        Log.i(TAG, "Inside on Error of Speech");
        String errorMessage = getErrorText(error);
        Log.i(TAG, errorMessage);

        if (error == 6 || error == 7) {
            noSpeechInput = 1;
        }
    }

    /**
     * Method for generating error message from error code.
     * @param errorCode Error code returned from speech recognition engine
     * @return error message of corresponding error code
     */
    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    /**
     * Method for stopping speech recognizer from listening to the user.
     */
    public void stopSpeechRecognizer() {
        if (speech != null) {
            speech.stopListening();
            speech.cancel();
        }
    }

    /**
     * Method for destroying speech recognizer object.
     */
    public void destroySpeechRecognizer() {
        if (speech != null) {
            speech.destroy();
        }
    }

    /**
     * Method for adding context to the context list.
     * @param context Context name to add
     */
    public void setContextList(Context context) {
        if (contextList.contains(context)) {
            counter++;
            Log.i(TAG, "Counter of the context incremented.");
        } else {
            contextList.add(context);
            Log.i(TAG, "Context added to the context list.");
        }
    }

    /**
     * Method for adding activity name to the activity list.
     * @param activityName Activity name to add
     */
    public void addActivityNameToList(String activityName) {
        if (acitivityNameList.contains(activityName)) {
            counter++;
            Log.i(TAG, "Counter of the activity name incremented.");
        } else {
            acitivityNameList.add(activityName);
            Log.i(TAG, "Activity name added to the activity name list.");
        }
    }
}
