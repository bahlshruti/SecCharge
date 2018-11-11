package com.aidor.secchargemobile.seccharge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.custom.Dec;
import com.aidor.secchargemobile.custom.DirectionsJSONParser;
import com.aidor.secchargemobile.custom.PlaceDetailsJSONParser;
import com.aidor.secchargemobile.custom.PlaceJSONParser;
import com.aidor.secchargemobile.custom.SecCharge;
import com.aidor.secchargemobile.database.DBHelper;
import com.aidor.secchargemobile.database.SqlLiteDbHelper;
import com.aidor.secchargemobile.model.CsModel;
import com.aidor.secchargemobile.model.CsSiteModel;
import com.aidor.secchargemobile.model.DurationResult;
import com.aidor.secchargemobile.model.Legs;
import com.aidor.secchargemobile.model.Route;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TripPlanerActivity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMyLocationChangeListener {
    View view;
    GoogleMap mMap;
    GPSTracker gpsTracker;
    double lon, latt, fetchedSourceLat, fetchedSourceLan, fetchedDestinationLat,fetchedDestinationLan, updatedLon, updatedLatt;
    static AutoCompleteTextView sourceAutoTextView;
    static AutoCompleteTextView destinationAutoTextView;
    Button goButton;
    Button loadDirectionButton;
    ParserTask placesParserTask;
    ParserTask placeDetailsParserTask;
    final int PLACES=0, PLACES_DETAILS=1;
    ListView map_list;
    private String id;
    List<LocatoinModel> setIdList;
    ClusterManager<MyMapItem> clusterManager;
    MyMapItem clickClusterItem;
    Marker selectedMarker;
    TextView durationTextView, ownerName;
    boolean isDataLoaded = false;
    String duration;
    String location = "MyLocation";
    private static final String API_KEY = "AIzaSyCXqkyWVyVdW5wgPlhTCBJitJ84TydOVJo";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    RelativeLayout headerLinearLayout;
    LinearLayout mapFragmentLinearLayout;
    private SlidingUpPanelLayout slidingLayout;
    TextView slideupTextView;
    public String addr1, siteType1, siteOwner1, postalCode1, province1, country1, siteNumber1, sitePhone1, level2Price1, fastDCPrice1, accessTypeTime1, usageType1;
    DBHelper myDatabase;
    ListView historyListView;
    TextView historyTextView;
    SqlLiteDbHelper myCsSiteDatabase;
    CsSiteModel csSiteModel;
    String markerId;
    double criticalPointLat, criticalPointLang;
    int soc;
    AlertDialog.Builder alertDialog;

    private SecCharge myApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApp = (SecCharge) this.getApplicationContext();
        checkNetwork();
        setContentView(R.layout.activity_trip_planer);

        alertDialog = new AlertDialog.Builder(this);
        initComponents();
        getSocFromWebService();
        myDatabase = new DBHelper(this);
        myCsSiteDatabase = new SqlLiteDbHelper(this);
        csSiteModel = new CsSiteModel();
        if (initMap()) {
            enableMap();
        }

        clusterManager = new ClusterManager<>(this, mMap);

        mMap.setOnCameraChangeListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyMapItem>() {
            @Override
            public boolean onClusterItemClick(final MyMapItem myMapItem) {

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
        sourceAutoTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ArrayList<String> historyAddress = myDatabase.retriveData();
                    if (historyAddress.get(0).toString() != "no_data") {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 250);
                        historyTextView.setVisibility(View.VISIBLE);
                        historyListView.setLayoutParams(params);
                        ArrayAdapter adapter = new ArrayAdapter(TripPlanerActivity.this, R.layout.list_item_history, historyAddress);
                        historyListView.setAdapter(adapter);
                        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String address = parent.getItemAtPosition(position).toString();
                                sourceAutoTextView.setText(address);
                            }
                        });
                    } else {
                        // Toast.makeText(getApplicationContext(), "No History", Toast.LENGTH_LONG).show();
                    }
                } else {
                    historyListView.setAdapter(null);
                }
            }
        });
        destinationAutoTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ArrayList<String> historyAddress = myDatabase.retriveData();
                    if (historyAddress.get(0).toString() != "no_data") {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 250);
                        historyTextView.setVisibility(View.VISIBLE);
                        historyListView.setLayoutParams(params);
                        ArrayAdapter adapter = new ArrayAdapter(TripPlanerActivity.this, R.layout.list_item_history, historyAddress);
                        historyListView.setAdapter(adapter);
                        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String address = parent.getItemAtPosition(position).toString();
                                destinationAutoTextView.setText(address);
                            }
                        });
                    } else {
                        //Toast.makeText(getApplicationContext(), "No History", Toast.LENGTH_LONG).show();
                    }
                } else {
                    historyListView.setAdapter(null);
                }
            }
        });


    }


    private void getSocFromWebService() {
        SocWebService socWebService = new SocWebService();
        socWebService.execute();
    }

    @Override
    public void onMyLocationChange(Location loc) {
        gpsTracker = new GPSTracker(this);
        if (gpsTracker.canGetLoaction()) {
            updatedLon = gpsTracker.getLongitide();
            updatedLatt = gpsTracker.getLattitude();
            String url = updatedMapPolylineUrl(updatedLatt, updatedLon);
            PolyLineDownloadTask polyTask = new PolyLineDownloadTask();
            polyTask.execute(url);




            if (location.equals(sourceAutoTextView.getText().toString())){
              mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latt, lon), 10));
               // mMap.animateCamera(cu);
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(fetchedSourceLat, fetchedSourceLan), 10));
                //mMap.animateCamera(cu);
            }
            addUpdatedMarkers();
        }
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
            startActivity(new Intent(TripPlanerActivity.this, NoInternetActivity.class));
            finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public LatLng getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    private void initComponents(){
        slidingLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        historyListView = (ListView)findViewById(R.id.history_list);
        historyTextView = (TextView)findViewById(R.id.historyTF);
        slideupTextView = (TextView)findViewById(R.id.slideupTF);
        sourceAutoTextView = (AutoCompleteTextView) findViewById(R.id.source_locationTF);
      //  sourceAutoTextView.setText("MyLocation");
        sourceAutoTextView.setThreshold(1);
        sourceAutoTextView.setAdapter(new GooglePlacesAutocompleteAdapter(this, android.R.layout.simple_list_item_1));
        destinationAutoTextView = (AutoCompleteTextView)findViewById(R.id.destination_locationTF);
        destinationAutoTextView.setThreshold(1);
        destinationAutoTextView.setAdapter(new GooglePlacesAutocompleteAdapter(this, android.R.layout.simple_list_item_1));
        headerLinearLayout = (RelativeLayout)findViewById(R.id.header_linear_layout);
        map_list=(ListView)findViewById(R.id.map_list);
        goButton = (Button)findViewById(R.id.trip_button);
        loadDirectionButton = new Button(this);
        loadDirectionButton.setText("Load Direction");
        loadDirectionButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 60));
        loadDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLayoutAlignment();
                loadShowMapButton();
            }
        });
    }

    public void goClicked(View ob){



        if (sourceAutoTextView.getText().toString().length() == 0 || destinationAutoTextView.getText().toString().length() == 0){
            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setTitle("Select Locations");
            ad.setMessage("Please enter source and destination locations");
            ad.setPositiveButton("OK", null);
            ad.show();
        } else {
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            mMap.clear();
            historyListView.setAdapter(null);
            historyListView.setVisibility(View.GONE);
            historyTextView.setVisibility(View.GONE);
            String sourceAddress = sourceAutoTextView.getText().toString();
            LatLng sourceLatLang = getLocationFromAddress(getApplicationContext(), sourceAddress);
            if (sourceLatLang != null) {
                historyInsert(sourceAddress);
                fetchedSourceLat = sourceLatLang.latitude;
                fetchedSourceLan = sourceLatLang.longitude;

                //sourceAutoTextView.setText(getCompleteAddressString(fetchedSourceLat, fetchedSourceLan));
                String destinationAddress = destinationAutoTextView.getText().toString();
                LatLng destinationLatLang = getLocationFromAddress(getApplicationContext(), destinationAddress);

                if (destinationLatLang != null) {
                    historyInsert(destinationAddress);
                    fetchedDestinationLat = destinationLatLang.latitude;
                    fetchedDestinationLan = destinationLatLang.longitude;
                   // destinationAutoTextView.setText(getCompleteAddressString(fetchedDestinationLat, fetchedDestinationLan));

                    if (String.valueOf(fetchedSourceLat) == "0.0" && String.valueOf(fetchedDestinationLat) == "0.0")
                        Toast.makeText(this, "Please enter location", Toast.LENGTH_LONG).show();
                    else {
                        addMarkersPolyLine();
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                } else {
                    AlertDialog.Builder ad = new AlertDialog.Builder(this);
                    ad.setTitle("Incorrect Address");
                    ad.setMessage("Please enter correct destination address.");
                    ad.setPositiveButton("OK", null);
                    ad.show();
                }
            } else {
                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                ad.setTitle("Incorrect Address");
                ad.setMessage("Please enter correct source address.");
                ad.setPositiveButton("OK", null);
                ad.show();
            }

        }
    }

    /*
    private void getAddressFromLocation(double lat, double lon, int i) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    */

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("My Current loction address", "" + strReturnedAddress.toString());
            } else {
                Log.w("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current loction address", "Canont get Address!");
        }
        return strAdd;
    }

    private void historyInsert(String address) {
        myDatabase.insertData(address);
        // Toast.makeText(TripPlanerActivity.this, "Data inserted", Toast.LENGTH_SHORT).show();
    }

    private SlidingUpPanelLayout.PanelSlideListener onSlideListener() {
        return new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {}

            @Override
            public void onPanelCollapsed(View view) {}

            @Override
            public void onPanelExpanded(View view) {}

            @Override
            public void onPanelAnchored(View view) {}

            @Override
            public void onPanelHidden(View view) {}
        };
    }

    private void changeLayoutAlignment() {
        mapFragmentLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 280));
    }

    private void loadShowMapButton() {
        Button showFullMap = new Button(this);
        showFullMap.setText("Show Map");
        showFullMap.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 60));
        headerLinearLayout.removeAllViews();
        headerLinearLayout.addView(showFullMap);
        showFullMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapFragmentLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
                headerLinearLayout.removeAllViews();
                headerLinearLayout.addView(loadDirectionButton);
            }
        });
    }

    private void addChargingSites(){
        String totalDistance = Dec.distance;
        float calDistance = Float.parseFloat(totalDistance)/1000;
        int finalDistance = Math.round(calDistance);
        GetLatLon getLatLon = new GetLatLon(finalDistance);
        getLatLon.execute();
    }

    private void addMarkersPolyLine(){
        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(fetchedSourceLat, fetchedSourceLan));
        options.position(new LatLng(fetchedDestinationLat, fetchedDestinationLan));
        mMap.addMarker(options);
        String url = getMapsApiDirectionsUrl(sourceAutoTextView.getText().toString());
        PolyLineDownloadTask polyTask = new PolyLineDownloadTask();
        polyTask.execute(url);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(new LatLng(fetchedSourceLat, fetchedSourceLan));
        builder.include(new LatLng(fetchedDestinationLat, fetchedDestinationLan));
        // begin new code:
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen
        LatLngBounds bounds = builder.build();
       // int padding = 5; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        if (location.equals(sourceAutoTextView.getText().toString())){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latt, lon), 5));
           // mMap.animateCamera(cu);
        } else {
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(fetchedSourceLat, fetchedSourceLan), 5));
            mMap.animateCamera(cu);
        }
        addMarkers(sourceAutoTextView.getText().toString());

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent intent = new Intent(this, DetailInfoActivity.class);
        intent.putExtra("ADDRESS", addr1);
        intent.putExtra("SITE_TYPE", siteType1);
        intent.putExtra("SITE_OWNER", siteOwner1);
        intent.putExtra("POSTAL_CODE",postalCode1);
        intent.putExtra("PROVINCE",province1);
        intent.putExtra("COUNTRY",country1);
        intent.putExtra("SITE_NUMBER",siteNumber1);
        intent.putExtra("SITE_PHONE",sitePhone1);
        intent.putExtra("LEVEL_2_PRICE",level2Price1);
        intent.putExtra("FAST_DC_PRICE", fastDCPrice1);
        intent.putExtra("ACCESS_TYPE_TIME",accessTypeTime1);
        intent.putExtra("USAGE_TYPE", usageType1);
        intent.putExtra("CAR_DURATION", duration);
        intent.putExtra("SITE_ID",markerId);
        intent.putExtra("LATITUDE", String.valueOf(marker.getPosition().latitude));
        intent.putExtra("LONGITUDE", String.valueOf(marker.getPosition().longitude));
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up, R.anim.empty);
    }

    private void addMarkers(String myLocation) {
        if (mMap != null) {
            if (location.equals(myLocation)) {
                mMap.addMarker(new MarkerOptions().position(new LatLng(latt, lon))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        .title("Starting Point"));
            }
            else {
                mMap.addMarker(new MarkerOptions().position(new LatLng(fetchedSourceLat, fetchedSourceLan))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        .title("Starting Point"));
            }
            mMap.addMarker(new MarkerOptions().position(new LatLng(fetchedDestinationLat, fetchedDestinationLan))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("End Point"));
        }
    }
    private void addUpdatedMarkers() {
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(updatedLatt, updatedLon))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title("Starting Point"));

            mMap.addMarker(new MarkerOptions().position(new LatLng(fetchedDestinationLat, fetchedDestinationLan))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("End Point"));
        }
    }

    private String updatedMapPolylineUrl(double updateLat, double updateLang) {
        String waypoints = "origin="
                + updateLat + "," + updateLang
                + "&destination=" + fetchedDestinationLat + ","
                + fetchedDestinationLan;

        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        Log.d("route url", url);
        return url;
    }

    private String getMapsApiDirectionsUrl(String myLocation) {

        String waypoints = null;
        if (location.equals(myLocation)){
            waypoints = "origin="
                    + latt + "," + lon
                    + "&destination=" + fetchedDestinationLat + ","
                    + fetchedDestinationLan;
        } else {
            waypoints = "origin="
                    + fetchedSourceLat + "," + fetchedSourceLan
                    + "&destination=" + fetchedDestinationLat + ","
                    + fetchedDestinationLan;
        }
        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        Log.d("route url", url);
        return url;
    }

    private void goToLocation(double lat, double lon) {
        LatLng setlatLng = new LatLng(lat, lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(setlatLng, 11.0f));
        //Marker marker = mMap.addMarker(new MarkerOptions()
          //      .position(setlatLng)
           //     .title("you")
           //     .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

    private boolean initMap() {
        if (mMap == null) {
            com.google.android.gms.maps.SupportMapFragment findFrag = (com.google.android.gms.maps.SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = findFrag.getMap();
            mMap.setOnInfoWindowClickListener(this);
        }
        return (mMap != null);
    }

    private void enableMap(){
        gpsTracker = new GPSTracker(this);
        if (gpsTracker.canGetLoaction()) {
            lon = gpsTracker.getLongitide();
            latt = gpsTracker.getLattitude();
            mMap.setMyLocationEnabled(true);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14.0f));
            goToLocation(latt, lon);
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
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
            System.out.println("Error downloading Url"+e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    private class DownloadTask extends AsyncTask<String, Void, String> {

        private int downloadType=0;
        private int downloadLocationType = 0;
        public DownloadTask(int type, int locationType){
            this.downloadType = type;
            this.downloadLocationType = locationType;
        }

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
            switch(downloadType){
                case PLACES:
                    placesParserTask = new ParserTask(PLACES, 5);
                    placesParserTask.execute(result);
                    break;

                case PLACES_DETAILS :
                    placeDetailsParserTask = new ParserTask(PLACES_DETAILS, downloadLocationType);
                    placeDetailsParserTask.execute(result);
            }
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{
        ProgressDialog pDialog = new ProgressDialog(TripPlanerActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Loading");
            pDialog.setCancelable(false);
            pDialog.show();
        }


        int parserType = 0;
        int parserLocationType = 0;
        public ParserTask(int type, int locationType){
            this.parserType = type;
            this.parserLocationType = locationType;
        }

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<HashMap<String, String>> list = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                switch(parserType){
                    case PLACES :
                        PlaceJSONParser placeJsonParser = new PlaceJSONParser();
                        list = placeJsonParser.parse(jObject);
                        break;
                    case PLACES_DETAILS :
                        PlaceDetailsJSONParser placeDetailsJsonParser = new PlaceDetailsJSONParser();
                        list = placeDetailsJsonParser.parse(jObject);
                }
            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            switch(parserType){
                case PLACES :
                    String[] from = new String[] { "description"};
                    int[] to = new int[] { android.R.id.text1 };
                    SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);
                    sourceAutoTextView.setAdapter(adapter);
                    destinationAutoTextView.setAdapter(adapter);
                    break;
                case PLACES_DETAILS :
                    HashMap<String, String> hm = result.get(0);
                    if (parserLocationType == 0){
                        double latitude = Double.parseDouble(hm.get("lat"));
                        double longitude = Double.parseDouble(hm.get("lng"));
                        fetchedSourceLat = latitude;
                        fetchedSourceLan = longitude;
                    } else if (parserLocationType == 1) {
                        double latitude = Double.parseDouble(hm.get("lat"));
                        double longitude = Double.parseDouble(hm.get("lng"));
                        fetchedDestinationLat = latitude;
                        fetchedDestinationLan = longitude;
                    }


                    break;
            }
            if (pDialog.isShowing()) pDialog.dismiss();
        }
    }

    private class PolyLineDownloadTask extends AsyncTask<String, Void, String>{
        ProgressDialog pDialog = new ProgressDialog(TripPlanerActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Loading");
            pDialog.setCancelable(false);
            pDialog.show();
        }
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

            PolyLineParserTask parserTask = new PolyLineParserTask();
            parserTask.execute(result);
            if (pDialog.isShowing()){
                pDialog.dismiss();
            }

        }
    }

    private class PolyLineParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{



        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
                addChargingSites();
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }


        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                /* code for critical point */
                int totalDistance = Integer.parseInt(Dec.distance);
                if (soc != 0) {
                    int drivableDistance = soc*2;
                    List<LatLng> criticalPoint = new ArrayList<LatLng>();
                    for (int k=drivableDistance+500;k<points.size();k = k+1000) {
                        criticalPoint.add(points.get(k));
                    }
                    for (int l=0; l<criticalPoint.size(); l++) {
                        criticalPointLat = criticalPoint.get(l).latitude;
                        criticalPointLang = criticalPoint.get(l).longitude;
                        addCriticalPoint(criticalPointLat, criticalPointLang);
                    }
                }
            /* code for critical point */
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.parseColor("#3399ff"));

            }

            if (lineOptions!=null) {
                mMap.addPolyline(lineOptions);
                for (int i = 0; i < Dec.starting_lat.size(); i++) {
                    LatLng latlng = new LatLng(Dec.starting_lat.get(i), Dec.starting_long.get(i));
                    mMap.addMarker(new MarkerOptions().position(latlng).title(Dec.html_instructions.get(i)).icon(BitmapDescriptorFactory.defaultMarker()));
                }
                map_list.setAdapter(null);
                map_list.setAdapter(new dataListAdapter());

            } else {
                goToLocation(gpsTracker.getLattitude(), gpsTracker.getLongitide());
                alertDialog.setTitle("Incorrect Address");
                alertDialog.setMessage("Please enter correct source/destination address.");
                alertDialog.setPositiveButton("OK", null);
                alertDialog.show();
            }

        }



    }


    class dataListAdapter extends BaseAdapter {

        public dataListAdapter() {}
        public int getCount() {
            // TODO Auto-generated method stub
            return Dec.html_instructions.size();
        }

        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            convertView = inflater.inflate(R.layout.path_adapter, parent, false);
            TextView txtcontent = (TextView) convertView.findViewById(R.id.place_name_tv);
            TextView txtcontent2=(TextView)convertView.findViewById(R.id.place_address_tv);
            TextView txttime=(TextView)convertView.findViewById(R.id.time);
            txtcontent.setText(Dec.html_instructions.get(position));
            txtcontent2.setText(Dec.maneuver.get(position));
            txttime.setText(Dec.dis.get(position) +"  "+ Dec.dur.get(position));
            return convertView;
        }
    }

    private class GetLatLon extends AsyncTask<String, Void, String> {
        private int finalDistance;

        public GetLatLon(int fetchedDistance){
            this.finalDistance = fetchedDistance;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            double midPointLat = Dec.centerOfPolyLine.latitude;
            double midPointLang = Dec.centerOfPolyLine.longitude;
            Log.d("val", Dec.centerOfPolyLine.toString());
            MyURL url = new MyURL();
            String newUrl = url.getUrl()+"getLatLongIdsWithInRadius/"+midPointLat+"/"+midPointLang+"/"+finalDistance;
            System.out.println("new Url charging station: "+newUrl);
            String return_text;
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(newUrl);
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
                System.out.println("Radius latlong --> " + return_text);
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
                JSONObject jsonObject;
                for (int i = 0; i < jsonArray.length(); i++) {
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

    private void addCriticalPoint(double criticalPointLat, double criticalPointLang) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(criticalPointLat, criticalPointLang))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .title("charging will be critical at this point"));

    }



    private void addItems(double latOffset, double lngOffset, String id) {
        MyMapItem myMapItem = new MyMapItem(latOffset, lngOffset, id);
        clusterManager.addItem(myMapItem);
        clusterManager.cluster();
    }

    public class MyCustomAdapterForItems implements GoogleMap.InfoWindowAdapter {

        private final View infoWindowView;

        MyCustomAdapterForItems() {
            infoWindowView = getLayoutInflater().inflate(
                    R.layout.info_window_layout, null);

            Button btnReserve2 = (Button) infoWindowView.findViewById(R.id.btnReserve2);

            btnReserve2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TripPlanerActivity.this, ReserveMainActivity.class);
                    intent.putExtra("SITE_ID", markerId);
                    intent.putExtra("MODIFICATION_STATUS", "normalReservation");
                    startActivity(intent);
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
                RestClientMap.get().getNewDuration(fetchedSourceLat+","+fetchedSourceLan,
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
                markerId = clickClusterItem.getId();
                CsModel csSiteModel = new CsModel();

                for (CsModel csModel: SplashActivity.csModels){
                    if (csModel.getId().equals(markerId)) {
                        csSiteModel = csModel;
                        break;
                    }
                }

                //myCsSiteDatabase.openDataBase();
               // csSiteModel = new CsSiteModel();
              //  csSiteModel = myCsSiteDatabase.getCsSiteDetails(markerId);
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

    public static ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());

            System.out.println("URL: "+url);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.d("Error Places API URL", e.toString());
            return resultList;
        } catch (IOException e) {
            Log.d("Error  to Places API", e.toString());
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {

            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.d("Cannot JSON results", e.toString());
        }

        return resultList;
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        resultList = autocomplete(constraint.toString());
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }
    private class SocWebService extends AsyncTask<String, Void, String> {
        String s;
        BufferedReader inStream;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... params) {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String userId = sharedPreferences.getString("UserID", "");
            String url = new MyURL().getUrlR()+"/soc/getstatus/"+userId;
            try{
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpRequest = new HttpGet(url);
                HttpResponse response = httpClient.execute(httpRequest);
                inStream = new BufferedReader(
                        new InputStreamReader(
                                response.getEntity().getContent()));

                StringBuffer buffer = new StringBuffer("");
                String line = "";
                while ((line = inStream.readLine()) != null) {
                    buffer.append(line);
                }
                inStream.close();

                s = buffer.toString();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return s;
        }


        @Override
        protected void onPostExecute(String s) {
            if (s == null){
                soc = 0;
            } else {
                soc = Integer.parseInt(s);
            }
        }
    }
}