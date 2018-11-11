package com.aidor.secchargemobile.seccharge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.model.EditVehicleResponse;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Edit_Vehicle_Fragment extends Fragment implements AdapterView.OnItemSelectedListener {

    View view;
    Spinner sp_carModel;
    Spinner sp_carMake;
    Spinner sp_carYear;
    EditText ed_carVIN;
    EditText ed_carPlateNo;
    Button btnUpdateEV;

    String ev_id;
    String userID;
    public static String str_carModel;
    public static String str_carMake;
    public static String str_carYear;
    String str_carVIN;
    String str_carPlateNo;
    String str_VID;
    int row_ID;
    String checkLayout;
    SharedPreferences pref;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String UserID = "UserID";

    ArrayAdapter<String> car_ModelAdapter;
    String[] carYear_array = {"2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016"};
    String[] carMake_array = {"BMW", "Chevrolet", "Chryslar", "Code_Automotive", "Dodge", "Fiat", "Ford", "General_Motors_EV", "Honda", "Kia", "Mercedes-Benz", "Mitsubishi", "Nissan", "Scion", "Smart", "Solectria", "Tesla", "Tesla_Motors", "Toyota", "Volkswagen", "Wheego Electric Cars, Inc."};


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        checkNetwork();
        view = inflater.inflate(R.layout.edit_vehicle, null, false);
//        Toolbar toolbar = (Toolbar) ((AppCompatActivity) getActivity()).findViewById(R.id.toolbar);
//        toolbar.findViewById(R.id.searchtoolbar).setVisibility(View.INVISIBLE);

//        toolbar.setBackground(getResources().getDrawable(R.drawable.sec));
//        toolbar.findViewById(R.id.searchtoolbar).setVisibility(View.VISIBLE);
//        toolbar.setPadding(5,5,5,5);

        sp_carModel = (Spinner) view.findViewById(R.id.spCarModel);
        sp_carMake = (Spinner) view.findViewById(R.id.spCarMake);
        sp_carYear = (Spinner) view.findViewById(R.id.spCarYear);
        ed_carVIN = (EditText) view.findViewById(R.id.etVIN);
        ed_carPlateNo = (EditText) view.findViewById(R.id.etPlateNumber);
        btnUpdateEV = (Button) view.findViewById(R.id.btnUpdateEV);

        sp_carMake.setOnItemSelectedListener(this);
        sp_carModel.setOnItemSelectedListener(this);
        sp_carYear.setOnItemSelectedListener(this);

        pref = this.getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userID = pref.getString(UserID, "");
        Log.d("UserID_EDIT", userID);


        if ((ShowEVModel.getInstance().getVehicle_ID()) == null) {
            str_VID = (ShowEVModel.getInstance().getVehicle_ID());
            Log.d("NULL_Row_Id", str_VID);
        } else {
            str_VID = (ShowEVModel.getInstance().getVehicle_ID());
            Log.d("SHOW_Check_Row_ID", str_VID);
            EditEVWebService addEVWebService = new EditEVWebService(userID, str_VID);
        }
        btnUpdateEV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
                if(ed_carPlateNo.getText().length() == 7 && ed_carVIN.getText().length() == 17){
                    UpdateEv update = new UpdateEv();
                    update.execute();

                }
                else if(ed_carVIN.getText().length() != 17){
                    ed_carVIN.setError("Please Enter 17 digit Vehicle Identification No.");

                }else if(ed_carPlateNo.getText().length() != 7){
                    ed_carPlateNo.setError("Please Enter 7 digit plate No.");

                }

            }
        });


        return view;
    }


    @Override
    public void onResume(){
        super.onResume();
        checkNetwork();
    }

    private void checkNetwork() {
        if (!isNetworkAvailable()){
            Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), NoInternetActivity.class));
            getActivity().finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void getData() {
        str_carPlateNo = ed_carPlateNo.getText().toString();
        str_carVIN = ed_carVIN.getText().toString();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
        switch (parent.getId()) {
            case R.id.spCarYear:
                str_carYear = String.valueOf(parent.getSelectedItem());
                break;
            case R.id.spCarMake:
                str_carMake = String.valueOf(parent.getSelectedItem());

                if (str_carMake.contentEquals("BMW")) {
                    List<String> list = new ArrayList<String>();
                    list.add("i3");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);
                }

                if (str_carMake.contentEquals("Chevrolet")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Chevrolet_S-10_NiMH");
                    list.add("Spark");
                    list.add("Chevrolet_S-10_PbA");
                    list.add("Chevrolet_S-10_L/A");
                    list.add("Chevrolet_S-10");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }
                if (str_carMake.contentEquals("Chryslar")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Voyager_EPIC");
                    list.add("Epic_Minivan");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }

                if (str_carMake.contentEquals("Code_Automotive")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Coda");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }

                if (str_carMake.contentEquals("Dodge")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Caravan_EPIC");
                    list.add("Plymouth_TE_Van");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }

                if (str_carMake.contentEquals("Fiat")) {
                    List<String> list = new ArrayList<String>();
                    list.add("500e");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }

                if (str_carMake.contentEquals("Ford")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Focus_EV");
                    list.add("Focus");
                    list.add("Ford_Azure_Transit_Connect");
                    list.add("TH!NK_City");
                    list.add("Ranger_EV(Lead Acid)");
                    list.add("Ranger_EV-NiMH");
                    list.add("Ranger_EV");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }

                if (str_carMake.contentEquals("General_Motors_EV")) {
                    List<String> list = new ArrayList<String>();
                    list.add("EV1-Lead_Acid");
                    list.add("EV1-NiMH");
                    list.add("EV1-NiMH_(CA,AZ)");
                    list.add("EV1-PbA_(CA, AZ)");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }

                if (str_carMake.contentEquals("Honda")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Fit");
                    list.add("FIT_EV");
                    list.add("EV_Plus");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }

                if (str_carMake.contentEquals("Kia")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Soul");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }


                if (str_carMake.contentEquals("Mercedes-Benz")) {
                    List<String> list = new ArrayList<String>();
                    list.add("B-Class_Electric");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }


                if (str_carMake.contentEquals("Mitsubishi")) {
                    List<String> list = new ArrayList<String>();
                    list.add("i-MiEV");
                    list.add("Mitsubishi_i");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }

                if (str_carMake.contentEquals("Nissan")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Leaf");
                    list.add("AltraEV");
                    list.add("Hypermini");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }


                if (str_carMake.contentEquals("Scion")) {
                    List<String> list = new ArrayList<String>();
                    list.add("IQ_EV");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }

                if (str_carMake.contentEquals("Smart")) {
                    List<String> list = new ArrayList<String>();
                    list.add("fortwo");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }

                if (str_carMake.contentEquals("Solectria")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Citivan");
                    list.add("Flash");
                    list.add("Force");
                    list.add("Force_Nicd_Pba_NiMH");
                    list.add("Force_lead_Acid");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }

                if (str_carMake.contentEquals("Tesla")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Model_S");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }

                if (str_carMake.contentEquals("Tesla_Motors")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Model_S");
                    list.add("Roadster_2.5");
                    list.add("Roadster");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }

                if (str_carMake.contentEquals("Toyota")) {
                    List<String> list = new ArrayList<String>();
                    list.add("RAV_4EV");
                    list.add("E-COM");
                    list.add("RAV_4EV_NiMH");
                    list.add("RAV_4EV_Pba");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }

                if (str_carMake.contentEquals("Volkswagen")) {
                    List<String> list = new ArrayList<String>();
                    list.add("e-Golf");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }

                if (str_carMake.contentEquals("Wheego_Electric_Cars,_Inc.")) {
                    List<String> list = new ArrayList<String>();
                    list.add("LiFe");
                    car_ModelAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    car_ModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    car_ModelAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(car_ModelAdapter);

                }


                break;
            case R.id.spCarModel:
                str_carModel = String.valueOf(parent.getSelectedItem());
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private class EditEVWebService{
        String newid;
        String newVehicleid;
        public EditEVWebService(String userid, String vehicleid){
            this.newid = userid;
            this.newVehicleid = vehicleid;
            RestClient.get().getEditVehicleData(newid, newVehicleid, new Callback<EditVehicleResponse>() {
                @Override
                public void success(EditVehicleResponse editVehicleResponse, Response response) {

                    String Make = editVehicleResponse.getMAKE();
                    String Year = editVehicleResponse.getYEAR();
                    String Model = editVehicleResponse.getMODEL();
                    String VIN = editVehicleResponse.getVEHICLEVIN();
                    String PlateNO = editVehicleResponse.getPLATENUMBER();
                    ev_id = editVehicleResponse.getID().toString();
                    ed_carVIN.setText(VIN);
                    ed_carPlateNo.setText(PlateNO);
                    for (int j = 0; j < carYear_array.length; j++) {

                        if (Year.equals(carYear_array[j])) {

//                                setting spinner data by getting adpater of spinnercar along with item position
//                                 and set current item on selected position.
                            sp_carYear.setSelection((int) (sp_carYear.getAdapter().getItemId(j)), false);
                        }
                    }
                    for (int k = 0; k < carMake_array.length; k++) {
                        if (Make.equals(carMake_array[k])) {
                            sp_carMake.setSelection(k, false);
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getContext(), "Vehicle Update failed", Toast.LENGTH_LONG).show();
                    System.out.println("Vehicle Update failed");
                }
            });
        }
    }

    private class UpdateEv  extends AsyncTask<String,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            String return_text;

            MyURL Url = new MyURL();
            String URL = Url.getUrl();

            try {
                System.out.println("Back username EV -->> " + userID);
                System.out.println("Back YEAR EV -->> " + str_carYear);
                System.out.println("Back MAKE EV -->> " + str_carMake);
                System.out.println("Back MODEL EV -->> " + str_carModel);


                HttpClient httpClient = new DefaultHttpClient();


                String NewURL = URL + "register/editvehicle";
                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("userid",userID));
                nameValuePairs.add(new BasicNameValuePair("make",str_carMake));
                nameValuePairs.add(new BasicNameValuePair("model",str_carModel));
                nameValuePairs.add(new BasicNameValuePair("platenumber",str_carPlateNo));
                nameValuePairs.add(new BasicNameValuePair("vehiclevin",str_carVIN));
                nameValuePairs.add(new BasicNameValuePair("year",str_carYear));
                nameValuePairs.add(new BasicNameValuePair("id", ev_id));


                HttpPost httpPost = new HttpPost(NewURL);
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
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
            Log.d("RES UPDATE EV:",s);
            if (s.equals("updated successfully")) {
                Toast.makeText(getActivity(), "Vehicle Updated Successfully", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            } else {
                Toast.makeText(getActivity(), "Vehicle Updated Successfully ", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
