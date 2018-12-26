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
import com.aidor.secchargemobile.Interface.ServiceCallbacks;
import com.aidor.secchargemobile.api.VoiceApi;
import com.aidor.secchargemobile.model.VoiceResponse;
import com.aidor.secchargemobile.services.TTSService;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.aidor.secchargemobile.Constants.Constant.negativeArray;
import static com.aidor.secchargemobile.Constants.Constant.option_1;
import static com.aidor.secchargemobile.Constants.Constant.option_2;
import static com.aidor.secchargemobile.Constants.Constant.option_3;
import static com.aidor.secchargemobile.Constants.Constant.positiveArray;
import static com.aidor.secchargemobile.rest.VoiceRestClient.getClient;

public class AutoSteeringActivity extends AppCompatActivity implements ServiceCallbacks {

    private static final String TAG = "AutoPilotCommand";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private TTSService TTS;
    private boolean bound = false;
    boolean flag = false;
    String response = "yes";
    Intent speechIntent;
    Intent finalIntent;

    VoiceApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_auto_steering );
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        speechIntent = new Intent(AutoSteeringActivity.this, TTSService.class);
        finalIntent = new Intent(AutoSteeringActivity.this, TTSService.class);
        speechIntent.putExtra("content_to_speak", "welcome to Auto Pilot Commands section! Which command you want to run? " +
                " 1 for Forward 2 for Reverse 3 for Exit");
        //speechIntent.putExtra("options", " 1 for Forward 2 for Reverse 3 for Exit");
        bindService(speechIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(speechIntent);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "Stop activity");
        super.onStop();
        if(bound) {
            unbindService(serviceConnection);
            bound = false;
            stopService(speechIntent);
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        //stopIntent=new Intent(MainActivity.this,TTSService.class);
        //stopService(stopIntent);

        Log.i(TAG, "Destroy Activity");

        if(TTS !=null || speechIntent !=null)
            TTS=null;

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
            TTS.setCallbacks(AutoSteeringActivity.this); // register
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

    @Override
    public void doNothing() {

        speechIntent.putExtra("content_to_speak", "Do you wish to continue?");
        startService(speechIntent);
    }

    private void promptSpeechInput() {
        Log.i(TAG, "start speech recogniser... ");

        Intent intent = new Intent( RecognizerIntent.ACTION_RECOGNIZE_SPEECH); //Simply takes user’s speech input and returns it to same activity
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        try {
            this.startActivityForResult(intent,REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(AutoSteeringActivity.this,
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

                    Toast.makeText(AutoSteeringActivity.this,
                            "result: " + Result,
                            Toast.LENGTH_SHORT).show();

                    apiService = getClient().create(VoiceApi.class);

                    if (option_1.contains(Result.get(0)))
                    {
                        Call<VoiceResponse> call = apiService.sendAutoSteeringCommand("forward");
                        process(call);
                    }

                    else if(option_2.contains(Result.get(0)))
                    {
                        Call<VoiceResponse> call = apiService.sendAutoSteeringCommand("reverse");
                        process(call);

                    }
                    else if(option_3.contains(Result.get(0)))
                    {
                        Log.i( TAG, "exiting" );
                        finish();
                    }
                    else if(positiveArray.contains(Result.get(0)))
                    {
                        speechIntent.putExtra("content_to_speak", "Which command you want to run? " +
                                " 1 for Forward 2 for Reverse 3 for Exit");
                        startService(speechIntent);
                    }
                    else if (negativeArray.contains(Result.get(0)))
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

    public void process(Call<VoiceResponse> call)
    {
        call.enqueue(new Callback<VoiceResponse>() {
            @Override
            public void onResponse(Call<VoiceResponse> call, Response<VoiceResponse> response) {
                if(response.isSuccessful()) {
                    String resp = response.body().getResponse();
                    finalIntent.putExtra( "final_content", resp );
                    startService( finalIntent );
                    Toast.makeText( AutoSteeringActivity.this, resp,
                            Toast.LENGTH_SHORT ).show();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.i( TAG, "" + t );
            }
        });

    }

}




