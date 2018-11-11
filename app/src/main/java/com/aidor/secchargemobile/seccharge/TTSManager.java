package com.aidor.secchargemobile.seccharge;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by Nikhil on 20/11/2016.
 * Manager for TTS Engine operations.
 */

public class TTSManager extends Application {

    private static TextToSpeech tts; // Text to speech engine variable
    private static boolean isLoaded = false; // Loaded flag

    private String TTSManager_TAG = "TTSManager"; // Tag for logging

    /**
     * Method for initializing TTS Engine.
     * @param context Context for TTS Engine
     */
    public void initializeTTSEngine(Context context) {
        try {
            tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status != TextToSpeech.ERROR) {
                        Log.i(TTSManager_TAG, "Setting the language.");
                        if (tts.isLanguageAvailable(Locale.CANADA) == TextToSpeech.LANG_AVAILABLE) {
                            tts.setLanguage(Locale.CANADA);
                            isLoaded = true;
                        }
                        else {
                            tts.setLanguage(Locale.US);
                            isLoaded = true;
                        }
                    }
                }


            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter for TTS Engine.
     * @return Initialized TTS Engine variable
     */
    public static TextToSpeech getTts() {
        return tts;
    }

    /**
     * Getter for loaded flag.
     * @return loaded flag
     */
    public static boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Method for shutting down TTS Engine and release the resources.
     */
    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    /**
     * Method for stopping TTS Engine from speaking.
     */
    public void stopTTS() {
        if (tts != null)
            tts.stop();
    }

    /**
     * Method for checking if TTS Engine is busy speaking.
     * @return Speaking status of TTS Engine
     */
    public boolean checkSpeaking() {
        return tts.isSpeaking();
    }

    /**
     * Method for synthesizing speech from text.
     * @param toSpeak Text to speak
     * @param utteranceId Utterance Id for spoken text
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void speak(CharSequence toSpeak, String utteranceId) {
        tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }
}
