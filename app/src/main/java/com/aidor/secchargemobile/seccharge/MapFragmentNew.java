package com.aidor.secchargemobile.seccharge;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.aidor.projects.seccharge.R;

import com.aidor.secchargemobile.database.SqlLiteDbHelper;
import com.aidor.secchargemobile.model.CsModel;
import com.aidor.secchargemobile.model.CsSiteModel;
import com.aidor.secchargemobile.model.DurationResult;
import com.aidor.secchargemobile.model.Legs;
import com.aidor.secchargemobile.model.Route;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;



import java.io.IOException;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MapFragmentNew extends SupportMapFragment implements GoogleMap.OnInfoWindowClickListener, GoogleApiClient.OnConnectionFailedListener {
    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    View view;
    public static GoogleMap mMap;
    public static SupportMapFragment mMapF;
    TextView durationTextView, ownerName;
    Marker selectedMarker;
    boolean isDataLoaded = false;
    SqlLiteDbHelper myDatabase;
    CsSiteModel csSiteModel;
    private String siteId;

    String longitudePosition[] = {"-88.2556", "1"};
    String lattitudePosition[] = {"42.6727905", "1"};
    public String duration, addr1, siteType1, siteOwner1, postalCode1, province1, country1, siteNumber1, sitePhone1, level2Price1, fastDCPrice1, accessTypeTime1, usageType1;
    double latArray[] = {45.412459, 45.41953};
    double lngArray[] = {-75.68985, -75.6786};
    String markerId[] = {"324", "489"};
    String markerIdSelected = "489";
    TextView tv1, tvAddress, tvSiteOwner, tvPostalCode, tvProvince, tvCountry, tvSiteNumber, tvSitePhone, tvLevel2Price, tvFastDCPrice, tvAccessTypeTime, tvSiteType, tvUsageType;
    Button addToRouteBtn, reserveBtn;
    double lon;
    double latt;
    GPSTracker gpsTracker;
    ClusterManager<MyMapItem> clusterManager;
    MyMapItem clickClusterItem;
    private View progressView;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private GoogleApiClient mGoogleApiClient;



    private ImageButton btnSearch;

    boolean mapSetup = false;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.new_map_fragment_layout, container, false);
        Toolbar toolbar = (Toolbar) ((AppCompatActivity) getActivity()).findViewById(R.id.toolbar);
        //toolbar.findViewById(R.id.searchToolbar).setVisibility(View.VISIBLE);

        mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();

        myDatabase = new SqlLiteDbHelper(getContext());
        csSiteModel = new CsSiteModel();

        getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            mMap = googleMap;
                            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
                            setupMap();
                        }
                    });





        return view;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                Log.i("MAP", "Place: " + place.getName());
                goToLocation(place.getLatLng().latitude, place.getLatLng().longitude);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Toast.makeText(getActivity(), "Could not fetch places!", Toast.LENGTH_SHORT).show();
                Log.i("MAP", status.getStatusMessage());

            } else if (resultCode == getActivity().RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }





    @Override
                    public void onInfoWindowClick (Marker marker){
                        Intent intent = new Intent(getContext(), DetailInfoActivity.class);
                        intent.putExtra("ADDRESS", addr1);
                        intent.putExtra("SITE_TYPE", siteType1);
                        intent.putExtra("SITE_OWNER", siteOwner1);
                        intent.putExtra("POSTAL_CODE", postalCode1);
                        intent.putExtra("PROVINCE", province1);
                        intent.putExtra("COUNTRY", country1);
                        intent.putExtra("SITE_NUMBER", siteNumber1);
                        intent.putExtra("SITE_PHONE", sitePhone1);
                        intent.putExtra("LEVEL_2_PRICE", level2Price1);
                        intent.putExtra("FAST_DC_PRICE", fastDCPrice1);
                        intent.putExtra("ACCESS_TYPE_TIME", accessTypeTime1);
                        intent.putExtra("USAGE_TYPE", usageType1);
                        intent.putExtra("CAR_DURATION", duration);
                        intent.putExtra("SITE_ID", siteId);
                        intent.putExtra("LATITUDE", String.valueOf(marker.getPosition().latitude));
                        intent.putExtra("LONGITUDE", String.valueOf(marker.getPosition().longitude));
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_up, R.anim.empty);
                    }

    private void setupMap() {


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            gpsTracker = new GPSTracker(getActivity());
            if (gpsTracker.canGetLoaction()) {
                lon = gpsTracker.getLongitide();
                latt = gpsTracker.getLattitude();
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                System.out.println("PERMISSION GRANTED.. animating camera now..");
                mMap.moveCamera(CameraUpdateFactory.zoomTo(17.0f));

                goToLocation(latt, lon);



            }

            setupMapAgain();
        } else {
            // Show rationale and request permission.
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        }


/*
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            System.out.println("Permission was not granted");




        } else {
            gpsTracker = new GPSTracker(getActivity());
            if (gpsTracker.canGetLoaction()) {
                lon = gpsTracker.getLongitide();
                latt = gpsTracker.getLattitude();

                mMap.setMyLocationEnabled(true);
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                System.out.println("PERMISSION GRANTED.. animating camera now..");
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

                goToLocation(latt, lon);



            }
        }

        */





    }

    public void setupMapAgain(){
        clusterManager = new ClusterManager<>(getActivity(), mMap);
        mMap.setOnCameraChangeListener(clusterManager);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyMapItem>() {
            @Override
            public boolean onClusterItemClick(MyMapItem myMapItem) {
                clickClusterItem = myMapItem;
                return false;
            }
        });
        clusterManager.setRenderer(new OwnIconRendered(getContext(), mMap, clusterManager));
        clusterManager.getMarkerCollection().setOnInfoWindowAdapter(new MyCustomAdapterForItems());

        /*
        for(int i = 0; i < 2; i++) {
            addItems(latArray[i],lngArray[i],markerId[i]);
        }
        */

        btnSearch = (ImageButton) view.findViewById(R.id.btnSearch);

        for (CsModel csModel: SplashActivity.csModels){
            addItems(Double.parseDouble(csModel.getLatLocation()), Double.parseDouble(csModel.getLongLocation()), csModel.getId());
        }

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .build(getActivity());
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Google Play services not available!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mapSetup = true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    mMap.setMyLocationEnabled(true);
                    gpsTracker = new GPSTracker(getActivity());
                    if (gpsTracker.canGetLoaction()) {
                        lon = gpsTracker.getLongitide();
                        latt = gpsTracker.getLattitude();
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        System.out.println("PERMISSION GRANTED.. animating camera now..");
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(17.0f));

                        goToLocation(latt, lon);



                    }

                    setupMapAgain();




                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {




                    mMap.setMyLocationEnabled(true);
                    gpsTracker = new GPSTracker(getActivity());
                    if (gpsTracker.canGetLoaction()) {
                        lon = gpsTracker.getLongitide();
                        latt = gpsTracker.getLattitude();
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        System.out.println("PERMISSION GRANTED.. animating camera now..");
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(17.0f));

                        goToLocation(latt, lon);



                    }

                    setupMapAgain();


                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void addItems ( double latOffset, double lngOffset, String id){
                    MyMapItem myMapItem = new MyMapItem(latOffset, lngOffset, id);
                    clusterManager.addItem(myMapItem);
                    clusterManager.cluster();
                }
                private void goToLocation ( double lat, double lon){
                    LatLng setlatLng = new LatLng(lat, lon);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(setlatLng, 13.0f));
                    //mMap.addMarker(new MarkerOptions()
                      //      .position(setlatLng)
                        //    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                }
                private void getDataForChargingSite (CsModel csSiteModel){
                    siteId = csSiteModel.getId();
                    addr1 = csSiteModel.getAddress1() + "\n" + csSiteModel.getCity() + "\n" + csSiteModel.getProvince();
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

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "Connection to Places API failed!", Toast.LENGTH_SHORT).show();
    }


    public class MyCustomAdapterForItems implements GoogleMap.InfoWindowAdapter {

                    private final View infoWindowView;

                    MyCustomAdapterForItems() {
                        infoWindowView = getActivity().getLayoutInflater().inflate(
                                R.layout.info_window_layout, null);


                    }

                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        selectedMarker = marker;

                        ImageView imageView = (ImageView) infoWindowView.findViewById(R.id.logocarimg);
                        durationTextView = (TextView) infoWindowView.findViewById(R.id.durationtv);
                        ownerName = (TextView) infoWindowView.findViewById(R.id.ownertv);
                        imageView.setImageResource(R.drawable.white_car);


                        if (isDataLoaded == false) {
                            final LatLng destinationLatLng = clickClusterItem.getPosition();
                            RestClientMap.get().getNewDuration(latt + "," + lon,
                                    destinationLatLng.latitude + "," + destinationLatLng.longitude,
                                    new Callback<DurationResult>() {
                                        @Override
                                        public void success(DurationResult result, Response response) {
                                            // success!
                                            Route a = result.getRoutes().get(0);
                                            Legs l = a.getLegs().get(0);
                                            duration = l.getDuration().getText();

                                            durationTextView.setText(duration);
                                            Double latCheck1 = destinationLatLng.latitude;
                                            String latCheck = latCheck1.toString();
                                          //  myDatabase.openDataBase();
                                            csSiteModel = new CsSiteModel();
                                            /*
                                            if (latCheck.equals("45.412459")) {
                                                // Musée canadien de la nature
                                                String markerId = "335";
                                                markerIdSelected = markerId;

                                               // csSiteModel = myDatabase.getCsSiteDetails(markerId);
                                               // getDataForChargingSite(csSiteModel);
                                            } else {
                                                // 800 Kind Edward
                                                String markerId = "500";
                                                markerIdSelected = markerId;
                                                //csSiteModel = myDatabase.getCsSiteDetails(markerId);
                                                //getDataForChargingSite(csSiteModel);
                                            }
                                            */


                                            CsModel csSiteModel = new CsModel();

                                            for (CsModel csModel: SplashActivity.csModels){
                                                if (csModel.getId().equals(clickClusterItem.getId())) {
                                                    csSiteModel = csModel;
                                                    markerIdSelected = String.valueOf(csModel.getId());
                                                    break;
                                                }
                                            }

                                            getDataForChargingSite(csSiteModel);
                                           // myDatabase.close();
                                            isDataLoaded = true;
                                            selectedMarker.showInfoWindow();
                                        }

                                        @Override
                                        public void failure(RetrofitError error) {
                                            System.out.println("fail to load");
                                        }
                                    });


                        } else {
                            isDataLoaded = false;
                        }
                        return infoWindowView;
                    }
                }
            }
