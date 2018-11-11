package com.aidor.secchargemobile.seccharge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.model.CsModel;
import com.aidor.secchargemobile.model.CsSitesResponse;
import com.aidor.secchargemobile.model.UpdatedSocModel;
import com.aidor.secchargemobile.rest.RestClientReservation;
import com.aidor.secchargemobile.rest.SecchargeRestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SplashActivity extends AppCompatActivity {
    private long splashDelay = 3000;

    SharedPreferences pref;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String UserID = "UserID";
    String CheckLogin;
    ImageView splash_img;
    Animation rotate;
    boolean isServerAvailable;
    boolean chargingSitesCached = false;

    public static List<CsModel> csModels = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,

                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        checkNetwork();
        checkServerStatus();

        setContentView(R.layout.splash_activity);
        Animation zoom_in = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.zoom_in);

        splash_img = (ImageView) findViewById(R.id.splash_img);

        splash_img.startAnimation(zoom_in);
        splash_img.setVisibility(View.VISIBLE);

        RestClientReservation.get().getChargingSites(new Callback<List<CsModel>>() {
            @Override
            public void success(List<CsModel> csSiteModels, Response response) {

              csModels = csSiteModels;
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Could not get charging sites from server", Toast.LENGTH_LONG).show();
            }
        });
        //splash_img.animate().rotationBy(360).setDuration(320);

//        TextView text = (TextView)findViewById(R.id.textView);
//        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/Alice.ttf");
//        text.setTypeface(type);



        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {

                finish();

                pref = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
                CheckLogin = pref.getString(UserID, "");

              if (isServerAvailable){

                    if (CheckLogin.equals("")) {
                        Log.d("Check Login inside null:", CheckLogin);

                        Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                     else {
                        Log.d("Check Login inside id:", CheckLogin);

                        Intent i = new Intent(SplashActivity.this, HomeActivity.class);
                        startActivity(i);
                    }
               }
                  else {
                    Intent i = new Intent(SplashActivity.this, ServerNotAvailableActivity.class);
                    startActivity(i);
                }


            }
        };

        Timer timer = new Timer();
        timer.schedule(timerTask, splashDelay);

    }

    @Override
    public void onResume(){
        super.onResume();
        //splash_img.animate().rotationBy(360).setDuration(350);
        checkNetwork();
    }

    private void checkNetwork() {
        if (!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SplashActivity.this, NoInternetActivity.class).putExtra("activityName", "SplashActivity"));
            finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void checkServerStatus(){
        try {
            RestClientReservation.get().getServerStatus(new Callback<UpdatedSocModel>() {
                @Override
                public void success(UpdatedSocModel updatedSocModel, Response response) {
                    String status = updatedSocModel.getServer_status();
                    isServerAvailable = true;
                    System.out.println("SERVER IS AVAILABLE ---------------->>>>>>>>>>>");
                }

                @Override
                public void failure(RetrofitError error) {
                    isServerAvailable = false;
                }
            });

        }catch (Exception ce){
            isServerAvailable= false;
        }
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();

    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        SplashActivity.this.finish();
    }
}
