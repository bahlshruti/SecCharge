package com.aidor.secchargemobile.seccharge;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class AfterForgetPassActivity extends AppCompatActivity {

    String str_fPwd;
    EditText ed_username, ed_Ans1, ed_Ans2, questionOne, questionTwo;

    Button forgetPwd;
    public static String str_userID;
    String questionId1, questionId2, answer1, answer2;
    List<ForgetPwdQuestionsModel> forgetPwdQuestionsModels;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkNetwork();
        setContentView(R.layout.afterforgettpassword);

        pDialog = new ProgressDialog(this);
        forgetPwd = (Button) findViewById(R.id.buttonSubmit);
        ed_username = (EditText) findViewById(R.id.etusername);
        ed_Ans1 = (EditText) findViewById(R.id.etanswer1);
        ed_Ans2 = (EditText) findViewById(R.id.etAns2);
        questionOne = (EditText) findViewById(R.id.spQue1);
        questionTwo = (EditText) findViewById(R.id.spQue2);

        Intent i = getIntent();
        str_userID = i.getStringExtra("userid");
        ed_username.setText(i.getStringExtra("username"));
        Log.d("getValu", str_userID);
        ForgetWebService webService = new ForgetWebService();
        webService.execute();

        forgetPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkData();
            }

            private void checkData() {
                if (ed_Ans1.getText().length() == 0) {
                    ed_Ans1.setError("Enter Answer 1");
                } else if (ed_Ans2.getText().length() == 0) {
                    ed_Ans2.setError("Enter Answer 2");
                } else {
                    answer1 = ed_Ans1.getText().toString();
                    answer2 = ed_Ans2.getText().toString();
                    AfterForgetWebService afterForgetWebService = new AfterForgetWebService();
                    afterForgetWebService.execute();
                }
            }
        });

    }


    @Override
    public void onResume(){
        super.onResume();
        checkNetwork();
    }

    private void checkNetwork() {
        if (!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AfterForgetPassActivity.this, NoInternetActivity.class).putExtra("activityName", "AfterForgetPassActivity"));
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
            String URL = Url.getUrl()+"forgotpassword/questions/"+str_userID;
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
            Log.d("Post Res load que  :", s);
            String Q1 = null;
            try {
                JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    JSONObject securityQuestionTxtObj = jsonObject.getJSONObject("securityQuestions");
                    questionOne.setText(securityQuestionTxtObj.getString("securityQuestion"));
                    questionId1 = securityQuestionTxtObj.getString("id");
                    System.out.println("Question1:"+questionId1);
                    JSONObject jsonObject2 = jsonArray.getJSONObject(1);
                    JSONObject securityQuestionTxtObj2 = jsonObject2.getJSONObject("securityQuestions");
                    questionTwo.setText(securityQuestionTxtObj2.getString("securityQuestion"));
                    questionId2 = securityQuestionTxtObj2.getString("id");
                    System.out.println("Question2:"+questionId2);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (pDialog.isShowing()){
                pDialog.dismiss();
            }
            // Toast.makeText(getApplicationContext(), "Email Has Been Sent To Your Email Id", Toast.LENGTH_SHORT).show();
        }
    }

    private class AfterForgetWebService extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Verifying security answers...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String return_text;
            MyURL Url = new MyURL();
            //String URL = Url.getValidateAnswer()+str_userID+"/"+questionId1;
            String URL = Url.getUrl()+"forgotpassword/validateAnswer";
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(URL);
                List<NameValuePair> para = new ArrayList<>();
                para.add(new BasicNameValuePair("userId", str_userID));
                para.add(new BasicNameValuePair("securityQuestionId", questionId1));
                para.add(new BasicNameValuePair("answer", answer1));
                httpPost.setEntity(new UrlEncodedFormEntity(para));
                HttpResponse response = httpClient.execute(httpPost);
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
                System.out.println("DATA 1st After PWd--> " + return_text);
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
            Log.d("Res :", s);
            if (s.equals("true")) {
                ValidateAns2 validateAns2 = new ValidateAns2();
                validateAns2.execute();
            } else {
                if (pDialog.isShowing()) pDialog.dismiss();
                Toast.makeText(AfterForgetPassActivity.this, "First Answer Incorrect ! ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ValidateAns2 extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String return_text;
            MyURL Url = new MyURL();
            String URL = Url.getUrl()+"forgotpassword/validateAnswer";
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(URL);
                List<NameValuePair> para = new ArrayList<>();
                para.add(new BasicNameValuePair("userId", str_userID));
                para.add(new BasicNameValuePair("securityQuestionId", questionId2));
                para.add(new BasicNameValuePair("answer", answer2));
                httpPost.setEntity(new UrlEncodedFormEntity(para));
                HttpResponse response = httpClient.execute(httpPost);

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
                System.out.println("DATA  After PWd--> " + return_text);
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
            Log.d("Ans 2 ", s);

            if (pDialog.isShowing()){
                pDialog.dismiss();
            }
            if (s.equals("true")) {
                Toast.makeText(AfterForgetPassActivity.this, "Please Check Your Email.", Toast.LENGTH_LONG).show();
                Intent i = new Intent(AfterForgetPassActivity.this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } else if(s.equals("false")){
                Toast.makeText(AfterForgetPassActivity.this, "Second Answer is Incorrect ! ", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(AfterForgetPassActivity.this, "Something Went Wrong ! Please Try Again " + s, Toast.LENGTH_SHORT).show();

            }


        }
    }
}
