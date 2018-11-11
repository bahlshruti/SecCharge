package com.aidor.secchargemobile.seccharge;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.database.SqlLiteDbHelper;
import com.aidor.secchargemobile.model.CsModel;
import com.aidor.secchargemobile.model.CsSiteModel;
import com.aidor.secchargemobile.model.DurationResult;
import com.aidor.secchargemobile.model.Legs;
import com.aidor.secchargemobile.model.Route;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class BeforeLoginMapActivity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener {
    public static GoogleMap mMap;
    GPSTracker gpsTracker;
    double originlat;
    double originlon;
    private String id;
    List<LocatoinModel> setIdList;
    TextView durationTextView, ownerName;
    public String addr1, siteType1, siteOwner1, postalCode1, province1, country1, siteNumber1, sitePhone1, level2Price1, fastDCPrice1, accessTypeTime1, usageType1;
    ClusterManager<MyMapItem> clusterManager;
    MyMapItem clickClusterItem;
    Marker selectedMarker;
    JSONObject jsonObject;
    boolean isDataLoaded = false;
    String duration;
    private CameraPosition cp;
    SqlLiteDbHelper myDatabase;
    CsSiteModel csSiteModel;

    String markerIdSelected = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkNetwork();
        setContentView(R.layout.before_login_map_activity);
        //progressView = (View) findViewById(R.id.progress_bar);
        myDatabase = new SqlLiteDbHelper(this);
        if (initMap()) {
            gpsTracker = new GPSTracker(this);
            if (gpsTracker.canGetLoaction()) {
                originlon = gpsTracker.getLongitide();
                originlat = gpsTracker.getLattitude();
                mMap.setMyLocationEnabled(true);
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14.0f));
                mMap.setOnInfoWindowClickListener(this);
                goToLocatoin(originlat, originlon);
            }
            getJsonLatLon();
        } else {
            Toast.makeText(BeforeLoginMapActivity.this, "Map Not Available!", Toast.LENGTH_SHORT).show();
        }
        clusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraChangeListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyMapItem>() {
            @Override
            public boolean onClusterItemClick(MyMapItem myMapItem) {

                clickClusterItem = myMapItem;
                return false;
            }
        });
        clusterManager
                .setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyMapItem>() {
                    @Override
                    public boolean onClusterClick(final Cluster<MyMapItem> cluster) {

                        LatLngBounds.Builder builder = LatLngBounds.builder();
                        for (ClusterItem item : cluster.getItems()) {
                            builder.include(item.getPosition());
                        }
                        final LatLngBounds bounds = builder.build();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

                        return true;
                    }
                });

        clusterManager.setRenderer(new OwnIconRendered(this, mMap, clusterManager));
        clusterManager.getMarkerCollection().setOnInfoWindowAdapter(new MyCustomAdapterForItems());

        for (CsModel csModel: SplashActivity.csModels){
            addItems(Double.parseDouble(csModel.getLatLocation()), Double.parseDouble(csModel.getLongLocation()), csModel.getId());
        }
    }



    private void checkNetwork() {
        if (!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(BeforeLoginMapActivity.this, NoInternetActivity.class).putExtra("activityName", "BeforeLoginMapActivity"));
            finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cp = mMap.getCameraPosition();
        mMap = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetwork();
        if (cp != null) {
            SupportMapFragment findFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = findFrag.getMap();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
            cp = null;
        }
    }

    private void getJsonLatLon() {
        GetLatLon get = new GetLatLon();
        get.execute();

    }

    private void goToLocatoin(double lat, double lon) {
        LatLng setlatLng = new LatLng(lat, lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.7950272, -98.3985003) , 4.0f));
    }

    private boolean initMap() {
        if (mMap == null) {
            SupportMapFragment findFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = findFrag.getMap();
        }
        return (mMap != null);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {


        finish();

    /*
        Intent intent = new Intent(this, DetailInfoActivity.class);
        intent.putExtra("ADDRESS",addr1);
        intent.putExtra("SITE_TYPE",siteType1);
        intent.putExtra("SITE_OWNER",siteOwner1);
        intent.putExtra("POSTAL_CODE",postalCode1);
        intent.putExtra("PROVINCE",province1);
        intent.putExtra("COUNTRY",country1);
        intent.putExtra("SITE_NUMBER",siteNumber1);
        intent.putExtra("SITE_PHONE",sitePhone1);
        intent.putExtra("LEVEL_2_PRICE",level2Price1);
        intent.putExtra("FAST_DC_PRICE",fastDCPrice1);
        intent.putExtra("ACCESS_TYPE_TIME",accessTypeTime1);
        intent.putExtra("USAGE_TYPE", usageType1);
        intent.putExtra("CAR_DURATION",duration);
        intent.putExtra("LATITUDE", String.valueOf(marker.getPosition().latitude));
        intent.putExtra("LONGITUDE", String.valueOf(marker.getPosition().longitude));
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up, R.anim.empty);

        */
    }

    private class GetLatLon extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... strings) {
            MyURL url = new MyURL();
            String URL = url.getMapURL_getLatLan();
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

            setIdList = new ArrayList<LocatoinModel>();

            try {
                JSONArray jsonArray = new JSONArray(s);
                //JSONObject jsonObject;
                for (int i = 0; i < jsonArray.length(); i++) {
                    LocatoinModel model = new LocatoinModel();
                    jsonObject = jsonArray.getJSONObject(i);
                    double lat = Double.parseDouble(jsonObject.getString("latitude"));
                    double lon = Double.parseDouble(jsonObject.getString("longitude"));
                    id = jsonObject.getString("cs_site_id");
                    Log.d("lat :", id);
                    addItems(lat, lon, id);
                }

            } catch (Exception ex) {
            }
        }


    }

    private void addItems(double latOffset, double lngOffset, String id) {
        MyMapItem myMapItem = new MyMapItem(latOffset, lngOffset, id);
        clusterManager.addItem(myMapItem);
        clusterManager.cluster();
    }

    //    private class GetData{
//        String newId;
//        public GetData(String id){
//            this.newId = id;
//
//            RestClient.get().getDetails(newId, new Callback<DetailResponse>() {
//                @Override
//                public void success(DetailResponse detailResponse, Response response) {
//                addr1 = detailResponse.getAddress1()+"\n"+detailResponse.getCity()+"\n"+detailResponse.getProvince();
//                siteType1 = detailResponse.getSiteType();
//                siteOwner1 = detailResponse.getSiteOwner();
//                postalCode1 = detailResponse.getPostalCode();
//                province1 = detailResponse.getProvince();
//                country1 = detailResponse.getCountry();
//                siteNumber1 = detailResponse.getSiteNumber().toString();
//                sitePhone1 = detailResponse.getSitePhone();
//                level2Price1 = detailResponse.getLevel2Price().toString();
//                fastDCPrice1 = detailResponse.getFastDCPrice().toString();
//                accessTypeTime1 = detailResponse.getAccessTypeTime();
//                usageType1 = detailResponse.getUsageType();
//                ownerName.setText(siteOwner1);
//                isDataLoaded = true;
//                selectedMarker.showInfoWindow();
//
//                    // you get the point...
//                }
//
//                @Override
//                public void failure(RetrofitError error) {
//                    // something went wrong
//                }
//            });
//        }
//
//    }
    public class MyCustomAdapterForItems implements GoogleMap.InfoWindowAdapter {

        private final View infoWindowView;

        MyCustomAdapterForItems() {
            infoWindowView = getLayoutInflater().inflate(
                    R.layout.info_window_layout, null);

            Button btnReserve2 = (Button) infoWindowView.findViewById(R.id.btnReserve2);

            btnReserve2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BeforeLoginMapActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();

                }
            });
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            selectedMarker = marker;
            ImageView imageView = (ImageView)infoWindowView.findViewById(R.id.logocarimg);
            durationTextView = (TextView)infoWindowView.findViewById(R.id.durationtv);
            ownerName= (TextView)infoWindowView.findViewById(R.id.ownertv);
            imageView.setImageResource(R.drawable.white_car);


            if(isDataLoaded == false){
                LatLng destinationLatLng = clickClusterItem.getPosition();
                RestClientMap.get().getNewDuration(originlat+","+originlon,
                        destinationLatLng.latitude+","+destinationLatLng.longitude,
                        new Callback<DurationResult>() {
                            @Override
                            public void success(DurationResult result, Response response) {
                                // success!
                                Route a = result.getRoutes().get(0);
                                Legs l = a.getLegs().get(0);
                                duration = l.getDuration().getText();

                                durationTextView.setText(duration);
                                isDataLoaded = true;
                                selectedMarker.showInfoWindow();
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                // something went wrong
                                System.out.println("failed to load");
                            }
                        });
               // String markerId = clickClusterItem.getSnippet();
                //GetData getMarker = new GetData(markerId);



                CsModel csSiteModel = new CsModel();

                for (CsModel csModel: SplashActivity.csModels){
                    if (csModel.getId().equals(clickClusterItem.getId())) {
                        csSiteModel = csModel;
                        markerIdSelected = String.valueOf(csModel.getId());
                        break;
                    }
                }

                getDataForChargingSite(csSiteModel);

            }else{
                isDataLoaded = false;
            }
            return infoWindowView;
        }
    }

    private void getDataForChargingSite(CsModel csSiteModel) {
        addr1 = csSiteModel.getAddress1()+"\n"+csSiteModel.getCity()+"\n"+csSiteModel.getProvince();
        siteType1 = csSiteModel.getSiteType();
        siteOwner1 = csSiteModel.getSiteOwner();
        postalCode1 = csSiteModel.getPostalCode();
        province1 = csSiteModel.getProvince();
        country1 = csSiteModel.getCountry();
        siteNumber1 = csSiteModel.getSiteNumber();
        sitePhone1 = csSiteModel.getSitePhone();
        level2Price1 = csSiteModel.getLevel2Price();
        fastDCPrice1 = csSiteModel.getFastDCPrice();
        accessTypeTime1 = csSiteModel.getAccessTypeTime();
        usageType1 = csSiteModel.getUsageType();
        ownerName.setText(siteOwner1);
        isDataLoaded = true;
        selectedMarker.showInfoWindow();
    }

    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb  = new StringBuffer();
            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            e.printStackTrace();;
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";

            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task", e.toString());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DurationJSONParser parser = new DurationJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";


            if(result.size()<1){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }


            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();


                List<HashMap<String, String>> path = result.get(i);


                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if(j==0){
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){
                        duration = (String)point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }


                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
            }
            System.out.println("Duration :"+duration);
            durationTextView.setText(duration);
            durationTextView.setTextSize(15);
            isDataLoaded = true;
            selectedMarker.showInfoWindow();
        }
    }
}