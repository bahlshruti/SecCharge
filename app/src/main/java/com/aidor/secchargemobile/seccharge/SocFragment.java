package com.aidor.secchargemobile.seccharge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.model.Reservationdetail;
import com.aidor.secchargemobile.model.StatusModel;
import com.aidor.secchargemobile.rest.RestClientReservation;
import com.aidor.secchargemobile.services.NotificationEventReceiver;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SocFragment extends Fragment {
    String userID;
    SharedPreferences pref;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String UserID = "UserID";
    JavaScriptInterface JSInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.soclayout, container, false);

        WebView webView = (WebView)rootView.findViewById(R.id.socWebView);

        webView.setInitialScale(1);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        pref = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userID = pref.getString(UserID, "");
        System.out.println("SOC USERID:"+userID);

        webView.addJavascriptInterface(new WebViewJavaScriptInterface(getContext(), userID), "app");
        JSInterface = new JavaScriptInterface(getContext());
        webView.addJavascriptInterface(JSInterface, "JSInterface");

        webView.loadUrl("file:///android_asset/demoguage.html");
        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:tempChangeBatteryCharging()");
            }
        });
        return rootView;
    }
    public  class WebViewJavaScriptInterface {
        private Context context;
        private String user_id;
        public WebViewJavaScriptInterface(Context context, String user_id){
            this.context = context;
            this.user_id = user_id;

        }
        @JavascriptInterface
        public String setCurrentUserID(){
            return user_id;
        }
    }
    public class JavaScriptInterface {
        Context mContext;
        JavaScriptInterface(Context c) {
            mContext = c;
        }
        @JavascriptInterface
        public void changeActivity()
        {
            System.out.println("Value Obtained");
            cancelReservation();
        }
    }





    private void cancelReservation() {
        RestClientReservation.get().getReservationIdForCancelation( pref.getString(UserID, ""), new Callback<Reservationdetail>() {
            @Override
            public void success(Reservationdetail reservationdetail, Response response) {
                Long reservationId = Long.parseLong(reservationdetail.getReservationid().toString());
                if (reservationId != null){
                   // CancelReservationTask cancelReservationTask = new CancelReservationTask(reservationId);
                   // cancelReservationTask.execute(reservationId);

                    RestClientReservation.get().getRemoteStopStatus(reservationId, new Callback<StatusModel>() {
                        @Override
                        public void success(StatusModel statusModel, Response response) {
                            if (statusModel.getStatus().equals("success")) {
                                Toast.makeText(getActivity().getApplicationContext(), "Reservation successfully stopped!: SUCCESS", Toast.LENGTH_SHORT).show();
                                //NotificationEventReceiver.setupAlarm(getActivity().getApplicationContext(), new Intent().putExtra("USER_ID", userID).putExtra("ACTION", "stop"));
                                Intent intent = new Intent(getActivity().getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), "Remote stop transaction status returned from CC: FAILED", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(getActivity().getApplicationContext(), "Failed to fetch remote stop transaction status!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity().getApplicationContext(),"Unable to get reservationID", Toast.LENGTH_LONG).show();
            }
        });
    }


    private class CancelReservationTask extends AsyncTask<String, Void, String> {


        protected CancelReservationTask(String reservationId){
        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String cancelId = params[0];
            MyURL url = new MyURL();
            String URL = url.getUrlR()+"/cancelReserve?CancelId="+cancelId+"&type=afterCharging";
            System.out.println("URL is:"+URL);
            String return_text;

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

            } catch (ClientProtocolException | UnsupportedEncodingException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "false";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equals("success")){
                Toast.makeText(getActivity().getApplicationContext(), "Reservation successfully cancelled!", Toast.LENGTH_SHORT).show();
                NotificationEventReceiver.setupAlarm(getActivity().getApplicationContext(), new Intent().putExtra("USER_ID", userID).putExtra("ACTION", "stop"));
                Intent intent = new Intent(getActivity().getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Reservation cancellation failed!. Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
