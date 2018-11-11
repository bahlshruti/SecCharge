package com.aidor.secchargemobile.seccharge;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class ForgetPasswordActivity extends AppCompatActivity {
    String str_fPwd, str_Email;
    EditText ed_email;
    Button nextBtn;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkNetwork();
        setContentView(R.layout.forgettpassword);

        pDialog = new ProgressDialog(this);
        ed_email = (EditText) findViewById(R.id.etForgetEmail);
        nextBtn = (Button)findViewById(R.id.btnnext);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUsername();
            }
        });

    }

    private void checkUsername() {
        if(ed_email.getText().length() == 0){
            ed_email.setError("Enter Username");
            ed_email.requestFocus();
            Log.d("Here", "null");
        }else{
            str_Email = ed_email.getText().toString();
            Log.d("str_email",str_Email);
            ForgetWebService webService = new ForgetWebService();
            webService.execute();
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        checkNetwork();
    }

    private void checkNetwork() {
        if (!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ForgetPasswordActivity.this, NoInternetActivity.class).putExtra("activityName", "ForgetPasswordActivity"));
            finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class ForgetWebService extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Loading...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String return_text;
            MyURL Url = new MyURL();
            String URL = Url.getUrl()+"forgotpassword/finduser/"+str_Email;
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(URL);
                HttpResponse response = httpClient.execute(httpGet);
                String res = response.toString();
                System.out.println("Http Post Response : " + res);
                InputStream is = response.getEntity().getContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                String line = "";
                StringBuilder stringBuffer = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
                return_text = stringBuffer.toString();
                System.out.println("DATA EDITEV --> " + return_text);
                return return_text;
            } catch (ClientProtocolException | UnsupportedEncodingException clientEx) {
                clientEx.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "false";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("userids",s);
            pDialog.dismiss();
            Intent i = new Intent(ForgetPasswordActivity.this,AfterForgetPassActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("userid",s);
            i.putExtra("username",str_Email);
            startActivity(i);
        }
    }
}
