package com.aidor.secchargemobile.seccharge;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.custom.SecCharge;
import com.aidor.secchargemobile.model.ProfileResponse;

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
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Path;

public class MyProfileActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {
    private SecCharge myApp;
    Context context;
    Spinner spinnerCountry;
    Spinner spinnerProvince;
    EditText ed_Fname;
    EditText ed_Lname;
    EditText ed_Email;
    EditText ed_Address1;
    EditText ed_Address2;
    EditText ed_postalCode;
    EditText ed_phone;
    EditText ed_city;
    EditText gender;
    Button btn_UpdateProfile;
    public int positionProvince;
    public static String country;
    public static String provience;

    String userID;
    String str_fname;
    String str_lname;
    String str_email;
    String str_address1;
    String str_address2;
    String str_postalCode;
    String str_phone;
    String str_city = "ottawa";
    int checkCall = 0;

    SharedPreferences pref;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String UserID = "UserID";


    ConstantModel constant = new ConstantModel(this);
    ArrayAdapter<String> countryAdapter;
    ArrayAdapter<String> provinceAdapter;
    int selectedCountryPosition;
    int SelectedProvincePosition;
    String selectedCountry;
    String selectedProvince;

    ProgressDialog pDialog, pDialog2;
    public static ArrayList<String> provinceArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApp = (SecCharge) this.getApplicationContext();
        checkNetwork();
        setContentView(R.layout.activity_my_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_normal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        pref = this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userID = pref.getString(UserID, "");
        Log.d("UserID Profile", userID);

        pDialog = new ProgressDialog(this);
        pDialog2 = new ProgressDialog(this);

        positionProvince = 0;
        //FUNCTION TO INITIALIZE ALL COMPONENET
        initComponent();



        //SETTING COUNTRY ARRAY TO COUNTRY SPINNER USING ARRAY ADAPTER
        countryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, constant.setCountriesArray());
        spinnerCountry.setAdapter(countryAdapter);
        spinnerCountry.setOnItemSelectedListener(this);


        getData();

        GetProfileData getProfileData = new GetProfileData(userID);

        btn_UpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    getData();
                    //WEBSERVICE FOR UPDATINFG USER DAta
                    UpdateProfileData updateProfileData = new UpdateProfileData();
                    updateProfileData.execute();
                }
            }
        });

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
            startActivity(new Intent(MyProfileActivity.this, NoInternetActivity.class));
            finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void initComponent() {

        ed_Fname = (EditText) findViewById(R.id.etfirstname);
        if (ed_Fname.getText().length()==0){
            ed_Fname.setHint("First Name");
        }
        ed_Lname = (EditText) findViewById(R.id.etlastname);
        if (ed_Lname.getText().length()==0){
            ed_Lname.setHint("Last Name");
        }
        ed_Email = (EditText) findViewById(R.id.etemailProfile);
        ed_Address1 = (EditText) findViewById(R.id.etaddress1);
        ed_Address2 = (EditText) findViewById(R.id.etaddress2);
        ed_postalCode = (EditText) findViewById(R.id.etpostalcode2);
        ed_phone = (EditText) findViewById(R.id.etphone);
        ed_city = (EditText) findViewById(R.id.etcity);
        btn_UpdateProfile = (Button) findViewById(R.id.btnprofile);

        spinnerCountry = (Spinner) findViewById(R.id.spinnerCountry);
        spinnerProvince = (Spinner) findViewById(R.id.spinnerProvince);


        spinnerProvince.setOnItemSelectedListener(this);


    }



    private boolean validateName(String name) {

        if (name.length()<3) return false;
        System.out.println("Length of name is more than 3");
        System.out.println(name);
        for (int i=1; i<name.length(); i++){
            System.out.println("Checking Character in Name: " + name.charAt(i));
            if (!Character.isLetter(name.charAt(i))){
                System.out.println("returning false");
                return false;
            }
        }

        return true;


    }

    // null validation  of all the fields
    public boolean validate() {
        boolean b = false;
        if (!validateName(ed_Fname.getText().toString())){
            ed_Fname.setError("First Name can only contain letters (minimum 3).");
        } else if (!validateName(ed_Lname.getText().toString())){
            ed_Lname.setError("Last Name can only contain letters (minimum 3).");
        }
        else if (ed_phone.getText().length() != 10) {
            Toast.makeText(getApplicationContext(), "Please Enter 10 digit Phone Number ", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(ed_phone.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Please Enter Phone Number", Toast.LENGTH_SHORT).show();
            b = false;
        } else if (TextUtils.isEmpty(ed_Address1.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Please Enter Address", Toast.LENGTH_SHORT).show();
            b = false;
        } else if (TextUtils.isEmpty(ed_Lname.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Enter Last Name", Toast.LENGTH_SHORT).show();
            b = false;
        }
        /* else if (TextUtils.isEmpty(ed_Address2.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Enter Address2", Toast.LENGTH_SHORT).show();
            b = false;

            */
        else if (TextUtils.isEmpty(ed_Fname.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Please Enter First Name", Toast.LENGTH_SHORT).show();
            b = false;
        } else if (TextUtils.isEmpty(ed_phone.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Please Enter Phone Number", Toast.LENGTH_SHORT).show();
            b = false;
        } else if (TextUtils.isEmpty(ed_Email.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Please Enter Email address", Toast.LENGTH_SHORT).show();
            b = false;
        } else if (TextUtils.isEmpty(ed_postalCode.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Please Enter Postal Code", Toast.LENGTH_SHORT).show();
            b = false;
        } else if (TextUtils.isEmpty(ed_city.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Please Enter City", Toast.LENGTH_SHORT).show();
            b = false;
        } else if (spinnerCountry.getSelectedItemPosition()==0 ){
            Toast.makeText(MyProfileActivity.this, "Please Select Country", Toast.LENGTH_SHORT).show();
            b = false;
        }
        else {
            b = true;
        }
        return b;
    }


    private void getData() {
        str_fname = ed_Fname.getText().toString();
        str_lname = ed_Lname.getText().toString();
        str_email = ed_Email.getText().toString();
        str_address1 = ed_Address1.getText().toString();
        str_address2 = ed_Address2.getText().toString();
        str_postalCode = ed_postalCode.getText().toString();
        str_phone = ed_phone.getText().toString();
        str_city = ed_city.getText().toString();
        Log.d("Data Saved", "fname" + str_fname + "lname" + str_lname + "email" + str_email + "address1" + str_address1 + "address2" + str_address2 + "postal code" + str_postalCode + "phone" + str_phone + "city" + str_city + "gender");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {

        switch (parent.getId()) {
            case R.id.spinnerCountry:
                checkCall = checkCall + 1;
                selectedCountryPosition = parent.getSelectedItemPosition();
                selectedCountry = String.valueOf(parent.getSelectedItem());

                if (!selectedCountry.equals("") && !selectedCountry.equals("Select Country")) {
                    setProvinceArrayFromPosition(selectedCountryPosition);
                }

                /*
//                selectedCountry = selectedCountry.replace(" ", "");
                if (checkCall == 0) {
                   // setProvinceAdapter(provinceArrayList);
                    setProvinceArrayFromPosition(selectedCountryPosition);
                }
                if (checkCall > 2) {
                    setProvinceArrayFromPosition(selectedCountryPosition);
                }
                */

              //  spinnerProvince.setOnItemSelectedListener(this);

               // Toast.makeText(getApplicationContext(), "Selected Country: " + selectedCountry, Toast.LENGTH_LONG).show();
                Log.d("Selected Country :", selectedCountry);
                Log.d("Check : ", String.valueOf(checkCall));
                break;

            case R.id.spinnerProvince:
                selectedProvince = String.valueOf(parent.getSelectedItem());
//                selectedProvince = selectedProvince.replace(" ","");
                Log.d("Selected Province :", selectedProvince);
               // Toast.makeText(getApplicationContext(), "Selected province: " + selectedProvince, Toast.LENGTH_LONG).show();
                Log.d("Check : ", String.valueOf(checkCall));
                break;
        }

    }

    private void setProvinceAdapter(ArrayList<String> updatedProvinceArrayList) {
        provinceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, updatedProvinceArrayList);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);
        spinnerProvince.post(new Runnable() {
            @Override
            public void run() {
                spinnerProvince.setSelection(positionProvince);
            }
        });
        provinceAdapter.notifyDataSetChanged();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    private void setProvinceArrayFromPosition(int selectedCountryPosition) {

        String province = String.valueOf(constant.setProvinceArray()[selectedCountryPosition]);
        StringTokenizer st = new StringTokenizer(province, "|");
        provinceArrayList = new ArrayList<>();
        while (st.hasMoreTokens()) {
            provinceArrayList.add(st.nextToken());
        }
        setProvinceAdapter(provinceArrayList);
    }


    private class GetProfileData{
        String newId;
        public GetProfileData(String userid){
            this.newId = userid;

            pDialog.setMessage("Fetching profile");
            pDialog.setCancelable(false);
            pDialog.show();

            RestClient.get().getProfileData(newId, new Callback<ProfileResponse>() {

                @Override
                public void success(ProfileResponse profileResponse, Response response) {

                    if (profileResponse.getFIRSTNAME().length()==0){
                        ed_Fname.setText("");
                        ed_Fname.setHint("First Name");
                    } else ed_Fname.setText(profileResponse.getFIRSTNAME());


                    if (profileResponse.getLASTNAME().length()==0){
                        ed_Lname.setText("");
                        ed_Lname.setHint("Last Name");
                    } ed_Lname.setText(profileResponse.getLASTNAME());

                    ed_Email.setText(profileResponse.getEMAIL());
                    ed_Address1.setText(profileResponse.getADDRESS1());
                    ed_Address2.setText(profileResponse.getADDRESS2());
                    ed_postalCode.setText(profileResponse.getPOSTALCODE());
                    ed_city.setText(profileResponse.getCITY());
                    if (ed_city.getText().length()==0){
                        ed_city.setHint("City");
                    }
                    ed_phone.setText(profileResponse.getCELLPHONE());
                    String userSelectedCountry = profileResponse.getCOUNTRY();
                    String userSelectedProvince = profileResponse.getPROVINCE();

                    System.out.println("COUNTRY FETCHED FROM SERVER: " + userSelectedCountry);
                    System.out.println("PROVINCE FETCHED FROM SERVER: " + userSelectedProvince);

                    if (userSelectedCountry != null) {
                        if (userSelectedCountry.length() > 0) {
                            System.out.println("COUNTRY IS NOT NULL --->>>> ");
                            int positionCountry = ((ArrayAdapter<String>) spinnerCountry.getAdapter()).getPosition(userSelectedCountry);
                            spinnerCountry.setSelection(positionCountry);

                            String province = String.valueOf(constant.setProvinceArray()[positionCountry]);
                            StringTokenizer st = new StringTokenizer(province, "|");
                            provinceArrayList = new ArrayList<>();
                            while (st.hasMoreTokens()) {
                                provinceArrayList.add(st.nextToken());
                            }
                            setProvinceAdapter(provinceArrayList);

                            if (userSelectedProvince != null) {
                                if (userSelectedProvince.length() > 0) {
                                    System.out.println("PROVINCE IS NOT NULL --->>>> ");
                                    positionProvince = ((ArrayAdapter<String>) spinnerProvince.getAdapter()).getPosition(userSelectedProvince);
                                    System.out.println("SETTING PROVINCE AS SELECTION: " +
                                            ((ArrayAdapter<String>) spinnerProvince.getAdapter()).getItem(positionProvince) + " at position: " + positionProvince);


                                    //updateProvinceAdapter(provinceArrayList, positionProvince);
                                    setProvinceAdapter(provinceArrayList);

                                }
                            } else {
                               // Toast.makeText(getApplicationContext(), "Province fetched from server was null!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        spinnerCountry.setSelection(0);
                        setProvinceArrayFromPosition(0);
                        //Toast.makeText(getApplicationContext(), "Country fetched from server was null!", Toast.LENGTH_SHORT).show();
                        System.out.println("COUNTRY FETCHED FROM SERVER WAS NULL--------------->>>>>>>>>>>>>>");
                    }

                    pDialog.dismiss();



                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getApplicationContext(), "Could not fetch profile from server!", Toast.LENGTH_LONG).show();
                    System.out.println("Profile Updation Failed");
                }
            });


        }

    }

    /*
    private void updateProvinceAdapter(ArrayList<String> updatedProvinceArrayList, final int position) {
        provinceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, updatedProvinceArrayList);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);
        spinnerProvince.post(new Runnable() {
            @Override
            public void run() {
                spinnerProvince.setSelection(position);
            }
        });
       // spinnerProvince.setSelection(position);
        provinceAdapter.notifyDataSetChanged();
    }
    */



    private class UpdateProfileData extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog2.setMessage("Updating");
            pDialog2.setCancelable(false);
            pDialog2.show();

        }

        @Override
        protected String doInBackground(String... strings) {

            String return_text;
            MyURL Url = new MyURL();
            String URL = Url.getUrl();
            try {
                System.out.println("Back_Prov  -->> " + selectedProvince);
                System.out.println("Back_Country -->> " + selectedCountry);

                HttpClient httpClient = new DefaultHttpClient();
                String NewURL = URL + "register/updateprofile";
                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("userid",userID));
                nameValuePairs.add(new BasicNameValuePair("fname",str_fname));
                nameValuePairs.add(new BasicNameValuePair("lname", str_lname));
                nameValuePairs.add(new BasicNameValuePair("email", str_email));
                nameValuePairs.add(new BasicNameValuePair("address1",str_address1));
                nameValuePairs.add(new BasicNameValuePair("address2", str_address2));
                nameValuePairs.add(new BasicNameValuePair("city", str_city));
                nameValuePairs.add(new BasicNameValuePair("postalcode", str_postalCode));
                nameValuePairs.add(new BasicNameValuePair("province", selectedProvince.toString()));
                nameValuePairs.add(new BasicNameValuePair("country", selectedCountry.toString()));
                nameValuePairs.add(new BasicNameValuePair("phone",str_phone));

                System.out.println("URL to execute" + NewURL);
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
                System.out.println("DATA  MY Profile --> " + return_text);
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
            Log.d("profile ", s);

            pDialog2.dismiss();
            if (s.equals("updated successfully")) {
                Toast.makeText(getApplicationContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Could not update profile", Toast.LENGTH_SHORT).show();
            }



        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }

}
