package com.aidor.secchargemobile.seccharge;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.model.MeterValueModel;
import com.aidor.secchargemobile.model.StartChargingModel;
import com.aidor.secchargemobile.model.StatusModel;
import com.aidor.secchargemobile.rest.RestClientReservation;

import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MeterValuesActivity extends Activity {
    private TextView tvMeterValue, tvMessage;

    Timer timer;
    String userID;
    SharedPreferences pref;
    private Button btnStop;

    String reservationId = "";

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String UserID = "UserID";

    TimerTask doAsynchronousTask;

    ProgressDialog pDialog;

    private Long transactionId;

    private boolean charging = false;

    ImageView imgBattery0, imgBattery1, imgBattery2, imgBattery3, imgBattery4, imgBattery5;

    boolean paused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_meter_values);

        checkNetwork();

        imgBattery0 = (ImageView) findViewById(R.id.imgBattery0);
        imgBattery1 = (ImageView) findViewById(R.id.imgBattery1);
        imgBattery2 = (ImageView) findViewById(R.id.imgBattery2);
        imgBattery3 = (ImageView) findViewById(R.id.imgBattery3);
        imgBattery4 = (ImageView) findViewById(R.id.imgBattery4);
        imgBattery5 = (ImageView) findViewById(R.id.imgBattery5);




        try {
            reservationId = getIntent().getExtras().getString("RESERVATION_ID");
        } catch (Exception e){
            e.printStackTrace();
        }
        tvMeterValue = (TextView) findViewById(R.id.tvMeterValue);
        tvMessage = (TextView) findViewById(R.id.tvMessage);





        transactionId = getIntent().getExtras().getLong("Transaction_id");
        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setVisibility(View.INVISIBLE);
        pref = this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userID = pref.getString(UserID, "");
        pDialog = new ProgressDialog(this);

        callServerTimeTask();

        startBatteryAnimation();

    }

    private void startBatteryAnimationTimer() {
        final Handler handler = new Handler();
        timer = new Timer();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {

                        startBatteryAnimation();
                    }

                });
            }
        };

        timer.schedule(doAsynchronousTask, 0, 7000); //execute in every 7 seconds
    }

    private void checkNetwork() {
        if (!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MeterValuesActivity.this, NoInternetActivity.class));
            finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



    private void startBatteryAnimation() {

        /*
        imgBattery5.animate().alpha(0);

        imgBattery4.animate().alpha(0);

        imgBattery3.animate().alpha(0);

        imgBattery2.animate().alpha(0);

        imgBattery1.animate().alpha(0);

        imgBattery0.animate().alpha(0);

        */


        imgBattery5.setVisibility(View.INVISIBLE);
        imgBattery4.setVisibility(View.INVISIBLE);
        imgBattery3.setVisibility(View.INVISIBLE);
        imgBattery2.setVisibility(View.INVISIBLE);
        imgBattery1.setVisibility(View.INVISIBLE);
        imgBattery0.setVisibility(View.INVISIBLE);





        imgBattery5.animate().alpha(1).setDuration(1000).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (!checkIfChargingStopped(animation)) {
                    imgBattery5.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animation.cancel();

                if (!checkIfChargingStopped(animation)) {
                imgBattery4.animate().alpha(1).setDuration(1000).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (!checkIfChargingStopped(animation)) {
                            imgBattery4.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animation.cancel();
                        if (!checkIfChargingStopped(animation)) {
                            imgBattery3.animate().alpha(1).setDuration(1000).setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    imgBattery3.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    animation.cancel();

                                    if (!checkIfChargingStopped(animation)) {
                                    imgBattery2.animate().alpha(1).setDuration(1000).setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {
                                            if (!checkIfChargingStopped(animation)) {
                                                imgBattery2.setVisibility(View.VISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animation) {

                                            animation.cancel();

                                            if (!checkIfChargingStopped(animation)) {
                                            imgBattery1.animate().alpha(1).setDuration(1000).setListener(new Animator.AnimatorListener() {
                                                @Override
                                                public void onAnimationStart(Animator animation) {
                                                    imgBattery1.setVisibility(View.VISIBLE);
                                                }

                                                @Override
                                                public void onAnimationEnd(Animator animation) {


                                                    if (!checkIfChargingStopped(animation)) {
                                                        imgBattery0.animate().alpha(1).setDuration(1000).setListener(new Animator.AnimatorListener() {
                                                            @Override
                                                            public void onAnimationStart(Animator animation) {
                                                                imgBattery0.setVisibility(View.VISIBLE);
                                                            }

                                                            @Override
                                                            public void onAnimationEnd(Animator animation) {


                                                                animation.cancel();

                                                        /*
                                                        imgBattery5.setVisibility(View.INVISIBLE);
                                                        imgBattery4.setVisibility(View.INVISIBLE);
                                                        imgBattery3.setVisibility(View.INVISIBLE);
                                                        imgBattery2.setVisibility(View.INVISIBLE);
                                                        imgBattery1.setVisibility(View.INVISIBLE);
                                                        imgBattery0.setVisibility(View.INVISIBLE);

                                                        */


                                                                //startBatteryAnimation();

                                                        /*
                                                        imgBattery5.setAlpha(0);
                                                        imgBattery4.setAlpha(0);
                                                        imgBattery3.setAlpha(0);
                                                        imgBattery2.setAlpha(0);
                                                        imgBattery1.setAlpha(0);
                                                        imgBattery0.setAlpha(0);


*/
                                                                if (!checkIfChargingStopped(animation)) {
                                                                    startBatteryAnimation();
                                                                }

                                                            }

                                                            @Override
                                                            public void onAnimationCancel(Animator animation) {

                                                            }

                                                            @Override
                                                            public void onAnimationRepeat(Animator animation) {

                                                            }
                                                        });

                                                    }
                                                }

                                                @Override
                                                public void onAnimationCancel(Animator animation) {

                                                }

                                                @Override
                                                public void onAnimationRepeat(Animator animation) {

                                                }
                                            });

                                        }
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animation) {

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animation) {

                                        }
                                    });

                                }
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            });

                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

            }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private boolean checkIfChargingStopped(Animator animation) {
        if (!charging) {
            imgBattery5.setVisibility(View.INVISIBLE);
            imgBattery4.setVisibility(View.INVISIBLE);
            imgBattery3.setVisibility(View.INVISIBLE);
            imgBattery2.setVisibility(View.INVISIBLE);
            imgBattery1.setVisibility(View.INVISIBLE);
            imgBattery0.setVisibility(View.INVISIBLE);

            return true;
        } else {
            return false;
        }
    }


    public void callServerTimeTask() {
        final Handler handler = new Handler();
        timer = new Timer();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (!paused) {
                            try {
                                RestClientReservation.get().getMeterValues(userID, new Callback<MeterValueModel>() {
                                    @Override
                                    public void success(MeterValueModel meterValueModel, Response response) {
                                        String variable = meterValueModel.getVariable();
                                        String value = meterValueModel.getValue();


                                        if (variable.equals("stopped")) {
                                            charging = false;
                                            // tvMessage.setText("Charging stopped at: " + value);
                                            tvMessage.setText("Charging stopped");

                                            //timer.cancel();
                                            doAsynchronousTask.cancel();
                                            //  tvMeterValue.setText(value);
                                            tvMeterValue.setVisibility(View.VISIBLE);
                                            btnStop.setVisibility(View.INVISIBLE);
                                        } else if (variable.equals("started")) {
                                            charging = true;
                                            // tvMessage.setText("Charging started at: " + value);
                                            tvMessage.setText("Charging started");

                                            //  tvMeterValue.setText(value);
                                            tvMeterValue.setVisibility(View.VISIBLE);
                                            btnStop.setVisibility(View.VISIBLE);
                                        } else if (variable.equals("inProgress")) {
                                            charging = true;
                                            tvMessage.setText("Charging is in progress");
                                            //  tvMeterValue.setText(value);
                                            btnStop.setVisibility(View.VISIBLE);
                                            tvMeterValue.setVisibility(View.VISIBLE);
                                        } else {
                                            charging = false;
                                            tvMessage.setText("No charging at this point of time");
                                            //  tvMeterValue.setVisibility(View.INVISIBLE);
                                            btnStop.setVisibility(View.INVISIBLE);
                                        }
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        error.printStackTrace();
                                        Toast.makeText(MeterValuesActivity.this, "Could not retrieve Meter Values from server", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                        }

                    }
                });
            }
        };

        timer.schedule(doAsynchronousTask, 0, 3000); //execute in every 3 seconds
    }





    @Override
    public void onBackPressed(){
        //doAsynchronousTask.cancel();
       // timer.cancel();
        finish();
    }

    @Override
    protected void onPause() {
        paused = true;
        super.onPause();
      //  doAsynchronousTask.cancel();
      //  timer.cancel();
    }

    @Override
    public void onDestroy(){
        paused = true;
        timer.cancel();
        super.onDestroy();
       // timer.cancel();
    }

    @Override
    protected void onResume() {
        paused = false;
        super.onResume();
       // callServerTimeTask();
    }

    public void onStopClicked(View view){

        pDialog.setMessage("Stopping...");

        if (transactionId != null) {
            pDialog.setCancelable(false);
            pDialog.show();


            RestClientReservation.get().getRemoteStopStatus(transactionId, new Callback<StatusModel>() {
                @Override
                public void success(StatusModel statusModel, Response response) {
                    if (statusModel.getSuccess().equals("true")){
                        pDialog.dismiss();
                       // timer.cancel();
                        Toast.makeText(MeterValuesActivity.this, "Charging stopped!", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(MeterValuesActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        pDialog.dismiss();
                        Toast.makeText(MeterValuesActivity.this, "Could not connect to CC!", Toast.LENGTH_SHORT).show();

                    }


                }

                @Override
                public void failure(RetrofitError error) {
                    error.printStackTrace();
                    pDialog.dismiss();
                    Toast.makeText(MeterValuesActivity.this, "Could not contact /remoteStopTransaction webservice!", Toast.LENGTH_SHORT).show();
                }
            });


        } else {
            Toast.makeText(MeterValuesActivity.this, "Could not get transaction ID from intent!", Toast.LENGTH_SHORT).show();
        }
    }
}
