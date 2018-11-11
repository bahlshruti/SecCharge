package com.aidor.secchargemobile.seccharge;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

public class MyEVFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    View view;
    Spinner sp_carModel;
    Spinner sp_carMake;
    Spinner sp_carYear;
    EditText ed_carVIN;
    EditText ed_carPlateNo;
    Button btnREgister;

    String userID;
    public static String str_carModel;
    public static String str_carMake;
    public static String str_carYear;
    String str_carVIN;
    String str_carPlateNo;
    int row_ID;
    String checkLayout;
    SharedPreferences pref;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String UserID = "UserID";

    int id;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_vehicle, null, false);


        sp_carModel = (Spinner) view.findViewById(R.id.spCarModel);
        sp_carMake = (Spinner) view.findViewById(R.id.spCarMake);
        sp_carYear = (Spinner) view.findViewById(R.id.spCarYear);
        ed_carVIN = (EditText) view.findViewById(R.id.etVIN);
        ed_carPlateNo = (EditText) view.findViewById(R.id.etPlateNumber);
        btnREgister = (Button) view.findViewById(R.id.btnRegisterEV);

        sp_carMake.setOnItemSelectedListener(this);
        sp_carModel.setOnItemSelectedListener(this);
        sp_carYear.setOnItemSelectedListener(this);


        // id = ((PassValueActivity) getActivity().getApplication()).getId();

        //  Log.d("row ID:", String.valueOf(id));

        if (savedInstanceState != null) {
            Bundle bundle = this.getArguments();
            row_ID = bundle.getInt("ID");
            checkLayout = bundle.getString("addVehicle");
            Log.d("row Id", String.valueOf(row_ID));
            Log.d("layout check:", String.valueOf(row_ID));

        } else {
            Log.d("No Bundle", "done");
        }
//        row_ID = getArguments().getInt("ID");
//        Log.d("row ID:", String.valueOf(row_ID));


        pref = this.getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userID = pref.getString(UserID, "");

      //  String row_id = pref.getInt("row_ID", 0);

      //  row_id = (ShowEVModel.getInstance().getVehicle_ID());
        String row_id = null;
        if((ShowEVModel.getInstance().getVehicle_ID()) == null){
//            Log.d("NULL row Id",row_id);
        }else{
//            Log.d("check row ID",row_id);
        }
        Log.d("row id ", String.valueOf(row_id));
        btnREgister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
                if(ed_carPlateNo.getText().length() == 7 && ed_carVIN.getText().length() == 17){
                    AddEVWebService addEVWebService = new AddEVWebService();
                    addEVWebService.execute();

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

    private void getData() {
        str_carPlateNo = ed_carPlateNo.getText().toString();
        str_carVIN = ed_carVIN.getText().toString();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {


        //  str_carModel = String.valueOf(parent.getSelectedItem());
        //Toast.makeText(getActivity(), str_carModel, Toast.LENGTH_SHORT).show();

        //  str_carMake = String.valueOf(parent.getSelectedItem());
        // Toast.makeText(getActivity(), str_carMake, Toast.LENGTH_SHORT).show();

        switch (parent.getId()) {
            case R.id.spCarYear:
                str_carYear = String.valueOf(parent.getSelectedItem());
                break;
            case R.id.spCarMake:
                str_carMake = String.valueOf(parent.getSelectedItem());

                if (str_carMake.contentEquals("BMW")) {
                    List<String> list = new ArrayList<String>();
                    list.add("i3");
                    ArrayAdapter<String> bmwAdpter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    bmwAdpter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    bmwAdpter.notifyDataSetChanged();
                    sp_carModel.setAdapter(bmwAdpter);
                }

                if (str_carMake.contentEquals("Chevrolet")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Spark");
                    list.add("Chevrolet_S-10_NiMH");
                    list.add("Chevrolet_S-10_PbA");
                    list.add("Chevrolet_S-10_L/A");
                    list.add("Chevrolet_S-10");
                    ArrayAdapter<String> chevroletAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    chevroletAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    chevroletAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(chevroletAdapter);

                }
                if (str_carMake.contentEquals("Chryslar")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Voyager_EPIC");
                    list.add("Epic_Minivan");
                    ArrayAdapter<String> chryslarAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    chryslarAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    chryslarAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(chryslarAdapter);

                }

                if (str_carMake.contentEquals("Code_Automotive")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Coda");
                    ArrayAdapter<String> codaautomativeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    codaautomativeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    codaautomativeAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(codaautomativeAdapter);

                }

                if (str_carMake.contentEquals("Dodge")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Caravan_EPIC");
                    list.add("Plymouth_TE_Van");
                    ArrayAdapter<String> dodgeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    dodgeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dodgeAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(dodgeAdapter);

                }

                if (str_carMake.contentEquals("Fiat")) {
                    List<String> list = new ArrayList<String>();
                    list.add("500e");
                    ArrayAdapter<String> fiatAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    fiatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    fiatAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(fiatAdapter);

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
                    ArrayAdapter<String> fordAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    fordAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    fordAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(fordAdapter);

                }

                if (str_carMake.contentEquals("General_Motors_EV")) {
                    List<String> list = new ArrayList<String>();
                    list.add("EV1-Lead_Acid");
                    list.add("EV1-NiMH");
                    list.add("EV1-NiMH_(CA,AZ)");
                    list.add("EV1-PbA_(CA, AZ)");
                    ArrayAdapter<String> generalMotorsEVAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    generalMotorsEVAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    generalMotorsEVAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(generalMotorsEVAdapter);

                }

                if (str_carMake.contentEquals("Honda")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Fit");
                    list.add("FIT_EV");
                    list.add("EV_Plus");
                    ArrayAdapter<String> hondaAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    hondaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    hondaAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(hondaAdapter);

                }

                if (str_carMake.contentEquals("Kia")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Soul");
                    ArrayAdapter<String> kiaAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    kiaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    kiaAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(kiaAdapter);

                }


                if (str_carMake.contentEquals("Mercedes-Benz")) {
                    List<String> list = new ArrayList<String>();
                    list.add("B-Class_Electric");
                    ArrayAdapter<String> mercedesAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    mercedesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mercedesAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(mercedesAdapter);

                }


                if (str_carMake.contentEquals("Mitsubishi")) {
                    List<String> list = new ArrayList<String>();
                    list.add("i-MiEV");
                    list.add("Mitsubishi_i");
                    ArrayAdapter<String> mitsubishiAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    mitsubishiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mitsubishiAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(mitsubishiAdapter);

                }

                if (str_carMake.contentEquals("Nissan")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Leaf");
                    list.add("AltraEV");
                    list.add("Hypermini");
                    ArrayAdapter<String> nissanAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    nissanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    nissanAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(nissanAdapter);

                }


                if (str_carMake.contentEquals("Scion")) {
                    List<String> list = new ArrayList<String>();
                    list.add("IQ_EV");
                    ArrayAdapter<String> scionAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    scionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    scionAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(scionAdapter);

                }

                if (str_carMake.contentEquals("Smart")) {
                    List<String> list = new ArrayList<String>();
                    list.add("fortwo");
                    ArrayAdapter<String> smartAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    smartAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    smartAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(smartAdapter);

                }

                if (str_carMake.contentEquals("Solectria")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Citivan");
                    list.add("Flash");
                    list.add("Force");
                    list.add("Force_Nicd_Pba_NiMH");
                    list.add("Force_lead_Acid");
                    ArrayAdapter<String> solectriaAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    solectriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    solectriaAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(solectriaAdapter);

                }

                if (str_carMake.contentEquals("Tesla")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Model_S");
                    ArrayAdapter<String> teslaAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    teslaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    teslaAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(teslaAdapter);

                }

                if (str_carMake.contentEquals("Tesla_Motors")) {
                    List<String> list = new ArrayList<String>();
                    list.add("Model_S");
                    list.add("Roadster_2.5");
                    list.add("Roadster");
                    ArrayAdapter<String> teslaMotorsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    teslaMotorsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    teslaMotorsAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(teslaMotorsAdapter);

                }

                if (str_carMake.contentEquals("'Toyota': ")) {
                    List<String> list = new ArrayList<String>();
                    list.add("RA_4EV");
                    list.add("E-COM");
                    list.add("RAV_4EV_NiMH");
                    list.add("RAV_4EV_Pba");
                    ArrayAdapter<String> toyotaAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    toyotaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    toyotaAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(toyotaAdapter);

                }

                if (str_carMake.contentEquals("Volkswagen")) {
                    List<String> list = new ArrayList<String>();
                    list.add("e-Golf");
                    ArrayAdapter<String> volkswagenAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    volkswagenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    volkswagenAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(volkswagenAdapter);

                }

                if (str_carMake.contentEquals("Wheego_Electric_Cars,_Inc.")) {
                    List<String> list = new ArrayList<String>();
                    list.add("LiFe");
                    ArrayAdapter<String> wheegoElectricCarsIncAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
                    wheegoElectricCarsIncAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    wheegoElectricCarsIncAdapter.notifyDataSetChanged();
                    sp_carModel.setAdapter(wheegoElectricCarsIncAdapter);

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

    private class AddEVWebService extends AsyncTask<String, Void, String> {
      CommonProgressDialog dialog = new CommonProgressDialog(getActivity());
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.showDialog();

        }

        @Override
        protected String doInBackground(String... strings) {
            String return_text;

            MyURL Url = new MyURL();
            String URL = Url.getUrl();

            try {
                System.out.println("Back username EV -->> " + userID);
                System.out.println("Back CARYEAR EV -->> " + str_carYear);
                System.out.println("Back CARMODEL EV -->> " + str_carModel);
                System.out.println("Back CARMAKE EV -->> " + str_carMake);


                HttpClient httpClient = new DefaultHttpClient();


                String NewURL = URL + "/update/vehicle";
                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("userid",userID));
                nameValuePairs.add(new BasicNameValuePair("make",str_carMake));
                nameValuePairs.add(new BasicNameValuePair("model",str_carModel));
                nameValuePairs.add(new BasicNameValuePair("platenumber",str_carPlateNo));
                nameValuePairs.add(new BasicNameValuePair("vehiclevin",str_carVIN));
                nameValuePairs.add(new BasicNameValuePair("year",str_carYear));

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
                System.out.println("DATA Ecom/aidor/projects/seccharge/MyEVFragment.java:454V --> " + return_text);
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
            dialog.hideDialog();

            if (s.equals("added successfully")) {
                Toast.makeText(getActivity(), "Vehicle added successfully.", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            } else {
                Toast.makeText(getActivity(), "Error While Adding Vehicle ", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
