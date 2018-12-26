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
import com.aidor.secchargemobile.Constants.Constant;
import com.aidor.secchargemobile.Interface.ServiceCallbacks;
import com.aidor.secchargemobile.api.VoiceApi;
import com.aidor.secchargemobile.model.VoiceResponse;
import com.aidor.secchargemobile.services.TTSService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.aidor.secchargemobile.Constants.Constant.negativeArray;
import static com.aidor.secchargemobile.Constants.Constant.option_2;
import static com.aidor.secchargemobile.Constants.Constant.option_3;
import static com.aidor.secchargemobile.Constants.Constant.option_4;
import static com.aidor.secchargemobile.Constants.Constant.positiveArray;
import static com.aidor.secchargemobile.rest.VoiceRestClient.getClient;


public class UserModeActivity extends AppCompatActivity implements ServiceCallbacks {

    private TTSService TTS;
    private boolean bound = false;
    private static final String TAG = "UserCommand";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    boolean flag = false;
    Intent speechIntent;
    Intent finalIntent;
   // Intent intent;

    List<String> option_1 = Constant.option_1;
    List<String> option_5 = Constant.option_5;

    VoiceApi apiService;


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
        finalIntent = new Intent(UserModeActivity.this, TTSService.class);
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
        //finish();
        //startActivity(getIntent());

        speechIntent.putExtra("content_to_speak", "Do you wish to continue?");
        startService(speechIntent);

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

                    apiService = getClient().create(VoiceApi.class);

                    if (option_1.contains(Result.get(0)))
                    {
                            Call<VoiceResponse> call = apiService.sendUserCommand("acceleration");
                            process(call);
                    }

                    else if(option_2.contains(Result.get(0)))
                    {
                        Call<VoiceResponse> call = apiService.sendUserCommand("left");
                        process(call);

                    }
                    else if(option_3.contains(Result.get(0)))
                    {
                        Call<VoiceResponse> call = apiService.sendUserCommand("right");
                        process(call);
                    }
                    else if(option_4.contains(Result.get(0)))
                    {
                        Call<VoiceResponse> call = apiService.sendUserCommand("reverse");
                        process(call);
                    }
                    else if(option_5.contains(Result.get(0)))
                    {
                        Log.i( TAG, "exiting" );
                        finish();

                    }
                    else if(positiveArray.contains(Result.get(0)))
                    {
                        speechIntent.putExtra("content_to_speak", "Which command you want to run?" +
                                "one for Acceleration   2 for Left   3 for Right  4 for Reverse 5 for Exit");
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
                    Toast.makeText( UserModeActivity.this, resp,
                            Toast.LENGTH_SHORT ).show();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.i( TAG, "" + t );
            }
        });

    }
    public void confirmation(ArrayList<String> Result) {
        flag = true;
        Log.i(TAG, "Result: " + Result);
        speechIntent.putExtra("content_to_speak", "did you say" + Result);
        startService(speechIntent);

    }
}




