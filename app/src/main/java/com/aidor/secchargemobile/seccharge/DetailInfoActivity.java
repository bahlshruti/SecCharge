package com.aidor.secchargemobile.seccharge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.custom.SecCharge;
import com.google.android.gms.maps.MapView;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class DetailInfoActivity extends ActionBarActivity {

    ListView list;
    public String siteId, addr1, siteType1, siteOwner1, postalCode1, province1, country1, siteNumber1, sitePhone1, level2Price1, fastDCPrice1, accessTypeTime1, usageType1;
    String[] preText = {"SiteType", "Site Owner",  "Site Number", "Site Phone", "Level 2 Price", "FastDC Price", "Access Type Time", "Usage Type"
    };

    public static String[] fetchedText;
    static Context context;
    private TextView tvProviderName, tvCompleteAddress, tvPostalCode;
    private Button btnViewMoreDetails;
    private ArrayList<String> listChargingStationInfo;

    private SecCharge myApp;

    private MapView map;

    private ImageView imgMap;

    private String latitude, longitude;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApp = (SecCharge) this.getApplicationContext();
        checkNetwork();
        setContentView(R.layout.detail_information_implement);
        context = this;
        //Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_normal);
        //setSupportActionBar(myToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);
        latitude = getIntent().getStringExtra("LATITUDE");
        longitude = getIntent().getStringExtra("LONGITUDE");
        imgMap = (ImageView) findViewById(R.id.imgMap);
        new SendTask().execute();
        TextView siteOwnerDisplay = (TextView)findViewById(R.id.siteOwnerDisplaytv);
        TextView durationCar = (TextView)findViewById(R.id.blue_car_duration);
        ImageView carImage = (ImageView)findViewById(R.id.blue_car);
        //this.setTitle("Map");
        carImage.setImageResource(R.drawable.icon_green_car);
        siteOwnerDisplay.setTextSize(30);
        siteOwnerDisplay.setTextColor(Color.WHITE);

        tvProviderName = (TextView) findViewById(R.id.tvProviderName);
        tvCompleteAddress = (TextView) findViewById(R.id.tvCompleteAddress);
        tvPostalCode = (TextView) findViewById(R.id.tvPostalCode);

        siteId = getIntent().getExtras().getString("SITE_ID");
        addr1 = getIntent().getExtras().getString("ADDRESS");
        siteType1 = getIntent().getExtras().getString("SITE_TYPE");
        siteOwner1 = getIntent().getExtras().getString("SITE_OWNER");
        postalCode1 = getIntent().getExtras().getString("POSTAL_CODE");
        province1 = getIntent().getExtras().getString("PROVINCE");
        country1 = getIntent().getExtras().getString("COUNTRY");
        siteNumber1 = getIntent().getExtras().getString("SITE_NUMBER");
        sitePhone1 = getIntent().getExtras().getString("SITE_PHONE");
        level2Price1 = getIntent().getExtras().getString("LEVEL_2_PRICE");
        fastDCPrice1 = getIntent().getExtras().getString("FAST_DC_PRICE");
        accessTypeTime1 = getIntent().getExtras().getString("ACCESS_TYPE_TIME");
        usageType1 = getIntent().getExtras().getString("USAGE_TYPE");
        String duration = getIntent().getExtras().getString("CAR_DURATION");
        fetchedText = new String[]{ siteType1, siteOwner1, siteNumber1, sitePhone1, level2Price1, fastDCPrice1, accessTypeTime1, usageType1};
        durationCar.setText(duration);
        siteOwnerDisplay.setText(siteOwner1);
        tvProviderName.setText(siteOwner1);
        tvCompleteAddress.setText(addr1);
        tvPostalCode.setText(postalCode1);
        //InfoDetailAdapter adapter = new InfoDetailAdapter((Activity) DetailInfoActivity.context, preText, fetchedText);


    }

    private class SendTask extends AsyncTask<Bitmap, String, Bitmap> {

        @Override
        protected void onPostExecute(Bitmap bmp){
            imgMap.setImageBitmap(bmp);

        }

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            // TODO Auto-generated method stub
            Bitmap bm = getGoogleMapThumbnail(latitude,longitude);
            return bm;

        }

    };

    public static Bitmap getGoogleMapThumbnail(String latitude, String longitude){

        String URL = "http://maps.google.com/maps/api/staticmap?center=" +latitude + "," + longitude + "&zoom=14&markers="+
                latitude + "," + longitude + "&maptype=normal&size=600x265&sensor=false";
        System.out.println(URL);

        Bitmap bmp = null;
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet request = new HttpGet(URL);

        InputStream in = null;
        try {
            in = httpclient.execute(request).getEntity().getContent();
            bmp = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return bmp;
    }

    @Override
    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences(){
        Activity currActivity = myApp.getCurrentActivity();
        if (this.equals(currActivity))
            myApp.setCurrentActivity(null);
    }
    @Override
    public void onResume(){
        super.onResume();
        myApp.setCurrentActivity(this);
        checkNetwork();
    }


    private void checkNetwork() {
        if (!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DetailInfoActivity.this, NoInternetActivity.class));
            finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onViewMoreDetailsClicked(View view){
        DialogFragment newFragment = new DialogCSInfo();
        newFragment.show(getSupportFragmentManager(), "cs_details");
    }

    public void onReserveClicked(View view){
        Intent intent = new Intent(DetailInfoActivity.this, ReserveMainActivity.class);
        intent.putExtra("SITE_ID", siteId);
        intent.putExtra("MODIFICATION_STATUS", "normalReservation");
        startActivity(intent);
    }
}
