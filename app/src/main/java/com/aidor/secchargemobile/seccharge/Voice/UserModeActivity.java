package com.aidor.secchargemobile.seccharge.Voice;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.aidor.projects.seccharge.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.aidor.secchargemobile.Constants.Constant;
import com.aidor.secchargemobile.Interface.ServiceCallbacks;
import com.aidor.secchargemobile.services.TTSService;


public class UserModeActivity extends AppCompatActivity implements ServiceCallbacks {

    private TTSService TTS;
    private boolean bound = false;
    private static final String TAG = "UserCommand";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    boolean flag = false;
    String response = "yes";
    Intent speechIntent;
    Intent intent;

    List<String> option_1 = Constant.option_1;
    List<String> option_5 = Constant.option_5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_user );
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        speechIntent = new Intent(UserModeActivity.this, TTSService.class);
        speechIntent.putExtra("content_to_speak", "welcome to User Commands section! Which command you want to run?" +
                "one for Acceleration   2 for Left   3 for Right  4 for Reverse 5 for Exit");

        bindService(speechIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(speechIntent);
    }

    @Override
    protected void onStop() {

        if(bound) {
            unbindService(serviceConnection);
            bound = false;
            //stopService(speechIntent);
        }

        Log.i(TAG, "Stop activity");
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        Log.i(TAG, "Destroy Activity");
    }

    /**
     * Callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            TTSService.LocalBinder binder = (TTSService.LocalBinder) service;
            TTS = binder.getService();
            bound = true;
            TTS.setCallbacks(UserModeActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    /* Defined by ServiceCallbacks interface */
    @Override
    public void doSomething() {
        promptSpeechInput();
    }

    /* Defined by ServiceCallbacks interface */
    @Override
    public void doNothing() {
        finish();
    }

    private void promptSpeechInput() {
        Log.i(TAG, "start speech recogniser... ");

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); //Simply takes user’s speech input and returns it to same activity
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        try {
            this.startActivityForResult(intent,REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(UserModeActivity.this,
                    " exception",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> Result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    Toast.makeText(UserModeActivity.this,
                            "result: " + Result,
                            Toast.LENGTH_SHORT).show();

                    if (option_1.contains(Result.get(0)))
                    {
                        speechIntent.putExtra("final_content", "The car is accelerating");
                        startService(speechIntent);
                        Toast.makeText(UserModeActivity.this,
                                "The car is accelerating",
                                Toast.LENGTH_SHORT).show();

                    }
                    else if(option_5.contains(Result.get(0)))
                    {
                        finish();
                    }
                    else
                    {
                        speechIntent.putExtra("content_to_speak", "please try again");
                        startService(speechIntent);
                    }


                }
            }
        }
    }

    public void confirmation(ArrayList<String> Result) {
        flag = true;
        Log.i(TAG, "Result: " + Result);
        speechIntent.putExtra("content_to_speak", "did you say" + Result);
        startService(speechIntent);

    }
}




