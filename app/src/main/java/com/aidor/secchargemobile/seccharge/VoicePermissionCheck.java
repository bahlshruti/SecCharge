package com.aidor.secchargemobile.seccharge;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.aidor.projects.seccharge.R;

/**
 * Created by Nikhil.
 * Class for checking record audio permission and TTS Engine status on the device.
 */
public class VoicePermissionCheck extends AppCompatActivity {

    // Permission code for performing speech recognition in driver mode
    private int MY_PERMISSIONS_FOR_SPEECH_RECOGNITION = 101;

    // Permission code for checking TTS Engine installation on the device
    private int MY_DATA_CHECK_CODE = 100;

    private String VoicePermission_TAG = "VoicePermission"; // Tag for logging

    private Button startDriverMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_permission_check);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        startDriverMode = (Button)findViewById(R.id.startDriverMode);

        // Request permissions for speech recognition
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // ActivityCompat#requestPermissions
            // Here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions,
            // int[] grantResults) to handle the case where the user grants the permission.

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_FOR_SPEECH_RECOGNITION);

            return;
        }
    }

    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // If TTS Engine present then proceed, otherwise spawn an intent to install the same
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                Intent myIntent = new Intent(this, EngineInitializerVoice.class);
                startActivity(myIntent);
                Log.i(VoicePermission_TAG, "TTS is already installed.");
            } else {
                // missing TTS, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
                Log.i(VoicePermission_TAG, "Installation triggered for TTS engine.");
            }
        }
    }

    public void goToEngineInitializer(View view) {
        // Checking TTS Engine on device
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }
}
