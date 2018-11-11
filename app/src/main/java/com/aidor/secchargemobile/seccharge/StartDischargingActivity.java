package com.aidor.secchargemobile.seccharge;

import android.animation.Animator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.model.DischargeStatusModel;
import com.aidor.secchargemobile.model.MeterValueModel;
import com.aidor.secchargemobile.rest.RestClientReservation;

import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class StartDischargingActivity extends Activity {

    ImageView imgBattery0, imgBattery1, imgBattery2, imgBattery3, imgBattery4, imgBattery5;
    TextView tvEvValue, tvGridValue;
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

    private boolean discharging = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_discharging);

        checkNetwork();

        pref = this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userID = pref.getString(UserID, "");


        imgBattery0 = (ImageView) findViewById(R.id.imgBattery0);
        imgBattery1 = (ImageView) findViewById(R.id.imgBattery1);
        imgBattery2 = (ImageView) findViewById(R.id.imgBattery2);
        imgBattery3 = (ImageView) findViewById(R.id.imgBattery3);
        imgBattery4 = (ImageView) findViewById(R.id.imgBattery4);
        imgBattery5 = (ImageView) findViewById(R.id.imgBattery5);

        tvEvValue = (TextView) findViewById(R.id.tvEvValue);
        tvGridValue = (TextView) findViewById(R.id.tvGridValue);


        tvEvValue.setText(String.valueOf(getIntent().getExtras().getFloat("stateOfCharge")));
        tvGridValue.setText(String.valueOf(getIntent().getExtras().getFloat("dischargeStateOfCharge")));

        //callServerTimerTask();
        startBatteryAnimation();
    }

    /*
    public void callServerTimerTask() {
        final Handler handler = new Handler();
        timer = new Timer();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (!paused) {
                            try {
                                RestClientReservation.get().startDischarging(userID, new Callback<DischargeStatusModel>() {
                                    @Override
                                    public void success(DischargeStatusModel dischargeStatusModel, Response response) {
                                        String status = dischargeStatusModel.getSuccess();
                                        float stateOfCharge, dischargeStateOfCharge;
                                        if (status.equals("true")){
                                            stateOfCharge = dischargeStatusModel.getStateOfCharge();
                                            dischargeStateOfCharge = dischargeStatusModel.getDischargeStateOfCharge();


                                        }
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {

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
    */



    private void startBatteryAnimation() {

        imgBattery5.clearAnimation();
        imgBattery4.clearAnimation();
        imgBattery3.clearAnimation();
        imgBattery2.clearAnimation();
        imgBattery1.clearAnimation();
        imgBattery0.clearAnimation();

        imgBattery5.setVisibility(View.VISIBLE);
        imgBattery4.setVisibility(View.VISIBLE);
        imgBattery3.setVisibility(View.VISIBLE);
        imgBattery2.setVisibility(View.VISIBLE);
        imgBattery1.setVisibility(View.VISIBLE);
        imgBattery0.setVisibility(View.VISIBLE);

        /*
        imgBattery5.animate().alpha(1).setDuration(600);

         imgBattery4.animate().alpha(1).setDuration(600);
         imgBattery3.animate().alpha(1).setDuration(600);
         imgBattery2.animate().alpha(1).setDuration(600);
         imgBattery1.animate().alpha(1).setDuration(600);
         imgBattery0.animate().alpha(1).setDuration(600);

*/



        imgBattery0.animate().alpha(0).setDuration(1000).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (!checkIfChargingStopped(animation)) {
                   // imgBattery0.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animation.cancel();

                if (!checkIfChargingStopped(animation)) {
                    imgBattery1.animate().alpha(0).setDuration(1000).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            if (!checkIfChargingStopped(animation)) {
                               // imgBattery1.setVisibility(View.INVISIBLE);
                            }
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animation.cancel();
                            if (!checkIfChargingStopped(animation)) {
                                imgBattery2.animate().alpha(0).setDuration(1000).setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                       // imgBattery2.setVisibility(View.INVISIBLE);
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        animation.cancel();

                                        if (!checkIfChargingStopped(animation)) {
                                            imgBattery3.animate().alpha(0).setDuration(1000).setListener(new Animator.AnimatorListener() {
                                                @Override
                                                public void onAnimationStart(Animator animation) {
                                                    if (!checkIfChargingStopped(animation)) {
                                                  //      imgBattery3.setVisibility(View.INVISIBLE);
                                                    }
                                                }

                                                @Override
                                                public void onAnimationEnd(Animator animation) {

                                                    animation.cancel();

                                                    if (!checkIfChargingStopped(animation)) {
                                                        imgBattery4.animate().alpha(0).setDuration(1000).setListener(new Animator.AnimatorListener() {
                                                            @Override
                                                            public void onAnimationStart(Animator animation) {
                                                           //     imgBattery4.setVisibility(View.INVISIBLE);
                                                            }

                                                            @Override
                                                            public void onAnimationEnd(Animator animation) {


                                                                if (!checkIfChargingStopped(animation)) {
                                                                    imgBattery5.animate().alpha(0).setDuration(1000).setListener(new Animator.AnimatorListener() {
                                                                        @Override
                                                                        public void onAnimationStart(Animator animation) {
                                                                         //   imgBattery5.setVisibility(View.INVISIBLE);
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
                                                                                final Animation aAnim = new AlphaAnimation(0.0f, 1.0f);
                                                                                aAnim.setDuration(500);
                                                                                aAnim.setFillAfter(true);
                                                                                imgBattery0.startAnimation(aAnim);
                                                                                imgBattery1.startAnimation(aAnim);
                                                                                imgBattery2.startAnimation(aAnim);
                                                                                imgBattery3.startAnimation(aAnim);
                                                                                imgBattery4.startAnimation(aAnim);
                                                                                imgBattery5.startAnimation(aAnim);
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
        if (!discharging) {
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


    private void checkNetwork() {
        if (!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(StartDischargingActivity.this, NoInternetActivity.class));
            finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
