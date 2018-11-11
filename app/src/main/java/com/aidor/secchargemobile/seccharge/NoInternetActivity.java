package com.aidor.secchargemobile.seccharge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;

public class NoInternetActivity extends Activity {
    Class callingActivity;
    String activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);

        activity = getIntent().getStringExtra("activityName");

    }

    public void onRetryClicked(View view) {
        try {
            if (activity!=null) {
                if (activity.equals("SplashActivity"))
                    startActivity(new Intent(NoInternetActivity.this, SplashActivity.class));

                else if (activity.equals("HomeActivity")) {
                    startActivity(new Intent(NoInternetActivity.this, HomeActivity.class));
                } else if (activity.equals("LoginActivity")) {
                    startActivity(new Intent(NoInternetActivity.this, LoginActivity.class));
                } else if (activity.equals("BeforeLoginMapActivity")) {
                    startActivity(new Intent(NoInternetActivity.this, BeforeLoginMapActivity.class));

                } else if (activity.equals("AfterForgetPassActivity")) {
                    startActivity(new Intent(NoInternetActivity.this, SplashActivity.class));

                } else if (activity.equals("ForgetPasswordActivity")) {
                    startActivity(new Intent(NoInternetActivity.this, SplashActivity.class));

                } else if (activity.equals("RegisterActivity")) {
                    startActivity(new Intent(NoInternetActivity.this, RegisterActivity.class));

                }
            }else {
                if (LoginActivity.loginAttemptFailedInternet){
                    startActivity(new Intent(NoInternetActivity.this, LoginActivity.class));
                } else {
                    startActivity(new Intent(NoInternetActivity.this, HomeActivity.class));
                }
            }

        } catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }
    }
}
