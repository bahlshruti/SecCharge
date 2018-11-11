package com.aidor.secchargemobile.seccharge.Voice;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import com.aidor.projects.seccharge.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import com.aidor.secchargemobile.Constants.Constant;
import com.aidor.secchargemobile.Interface.ServiceCallbacks;
import com.aidor.secchargemobile.services.TTSService;


public class VoiceActivity extends Activity implements ServiceCallbacks{


    private static final String TAG = "Main";

    private TTSService TTS;
    private boolean bound = false;
    private Intent speechIntent;
    private static final int REQUEST_RECORD_PERMISSION = 100;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    boolean flag = false;
    String response = "yes";

    List<String> positiveArray = Constant.positiveArray;
    List<String> negativeArray  = Constant.negativeArray;


    public void checkforpermissions() {
        if (ContextCompat.checkSelfPermission(VoiceActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions
                    (VoiceActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            REQUEST_RECORD_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {

                        Log.i(TAG, "START");
                    } catch (Exception e) {
                        Toast.makeText(VoiceActivity.this, "unable to listen", Toast
                                .LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(VoiceActivity.this, "Permission Denied!", Toast
                            .LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_voice );
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.i(TAG, "Current Thread is Main Thread");
        }
        checkforpermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();

        speechIntent = new Intent(VoiceActivity.this, TTSService.class);
        speechIntent.putExtra("content_to_speak", "Welcome to aev ! Do you want to turn on voice mode");


        Log.v(TAG, "starting service");

        bindService(speechIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(speechIntent);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume called");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause called ");
        super.onPause();
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
            TTS.setCallbacks(VoiceActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    /* Defined by ServiceCallbacks interface */
    @Override
    public void doSomething() {
        promptspeechinput();
    }

    @Override
    public void doNothing() {

    }

    /*
     * Showing google speech input dialog
     * */

    private void promptspeechinput() {
        Log.i(TAG, "start speech recogniser... ");

        Intent intent = new Intent( RecognizerIntent.ACTION_RECOGNIZE_SPEECH); //Simply takes user’s speech input and returns it to same activity
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log.i(TAG,"Current thread is not the UI/Main thread");
        }

        try {
            Log.i(TAG,"starting activty for result");
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Log.i(TAG,"Exception");
            /**
             Toast.makeText(VoiceActivity.this,
             " exception",
             Toast.LENGTH_SHORT).show();
             **/
        }
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "Stop activity");
        super.onStop();
        if(bound) {
            unbindService(serviceConnection);
            bound = false;
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        //stopIntent=new Intent(VoiceActivity.this,TTSService.class);
        //stopService(stopIntent);

        Log.i(TAG, "Destroy Activity");
        if(TTS !=null || speechIntent !=null)
            TTS=null;
        speechIntent=null;

    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> Result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Toast.makeText(VoiceActivity.this,
                            "result: " + Result,
                            Toast.LENGTH_SHORT).show();

                    if (positiveArray.contains( Result.get(0)) || negativeArray.contains(Result.get(0))) {

                        if (!flag) {
                            response = Result.get(0);
                            confirmation(Result);

                        } else if (positiveArray.contains(Result.get(0)) && positiveArray.contains(response)) {
                            Intent intent = new Intent(VoiceActivity.this, ModesActivity.class);
                            startActivity(intent);
                            finish();

                        } else if ((negativeArray.contains(Result.get(0)) && negativeArray.contains(response)) || (negativeArray.contains(Result.get(0)) && positiveArray.contains(response))) {

                            flag = false;
                            speechIntent.putExtra("content_to_speak", "Do you want to turn on voice mode!");
                            startService(speechIntent);

                        } else {
                            //result='yes' and response ='no'
                            // exit the application...
                            finish();
                        }
                    } else {
                        flag = false;

                        speechIntent.putExtra("content_to_speak", "sorry please try again");
                        startService(speechIntent);
                    }
                    //Result.clear();
                    break;
                }
            }
        }
    }

    public void confirmation(ArrayList<String> Result)
    {
        flag=true;
        Log.i(TAG, "Result: "+Result);
        speechIntent.putExtra("content_to_speak", "did you say"+ Result);
        startService(speechIntent);

    }
}