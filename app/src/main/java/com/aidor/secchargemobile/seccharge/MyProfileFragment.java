package com.aidor.secchargemobile.seccharge;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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

public class MyProfileFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    View view;
    LayoutInflater layoutInflater;
    Context context;
    Spinner spinnerCountry;
    Spinner spinnerProvience;
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


    ConstantModel constant = new ConstantModel(getActivity());
    ArrayAdapter<String> countryAdapter;
    ArrayAdapter<String> provinceAdapter;
    int selectedCountryPosition;
    int SelectedProvincePosition;
    String selectedCountry;
    String selectedProvince;
    public static ArrayList<String> provinceArrayList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_my_profile, null, false);

        //Toolbar toolbar = (Toolbar) ((AppCompatActivity) getActivity()).findViewById(R.id.toolbar_normal);
        //toolbar.findViewById(R.id.searchtoolbar).setVisibility(View.INVISIBLE);

        pref = this.getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userID = pref.getString(UserID, "");
        Log.d("UserID Profile", userID);

        //FUNCTION TO INITIALIZE ALL COMPONENET
        initComponent();


        //SETTING COUNTRY ARRAY TO COUNTRY SPINNER USING ARRAY ADAPTER
        countryAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, constant.setCountriesArray());
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


        return view;
    }

    private void initComponent() {

        ed_Fname = (EditText) view.findViewById(R.id.etfirstname);
        ed_Lname = (EditText) view.findViewById(R.id.etlastname);
        ed_Email = (EditText) view.findViewById(R.id.etemailProfile);
        ed_Address1 = (EditText) view.findViewById(R.id.etaddress1);
        ed_Address2 = (EditText) view.findViewById(R.id.etaddress2);
        ed_postalCode = (EditText) view.findViewById(R.id.etpostalcode2);
        ed_phone = (EditText) view.findViewById(R.id.etphone);
        ed_city = (EditText) view.findViewById(R.id.etcity);
        btn_UpdateProfile = (Button) view.findViewById(R.id.btnprofile);

        spinnerCountry = (Spinner) view.findViewById(R.id.spinnerCountry);
        spinnerProvience = (Spinner) view.findViewById(R.id.spinnerProvince);

    }

    // null validation  of all the fields
    public boolean validate() {
        boolean b = false;
        if (ed_phone.getText().length() != 10) {
            Toast.makeText(getActivity(), "Please Enter 10 digit Phone Numbber ", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(ed_phone.getText().toString())) {
            Toast.makeText(getActivity(), "Enter Phone no", Toast.LENGTH_SHORT).show();
            b = false;
        } else if (TextUtils.isEmpty(ed_Address1.getText().toString())) {
            Toast.makeText(getActivity(), "Enter Address1", Toast.LENGTH_SHORT).show();
            b = false;
        } else if (TextUtils.isEmpty(ed_Lname.getText().toString())) {
            Toast.makeText(getActivity(), "Enter Last Name", Toast.LENGTH_SHORT).show();
            b = false;
        } else if (TextUtils.isEmpty(ed_Address2.getText().toString())) {
            Toast.makeText(getActivity(), "Enter Address2", Toast.LENGTH_SHORT).show();
            b = false;
        } else if (TextUtils.isEmpty(ed_Fname.getText().toString())) {
            Toast.makeText(getActivity(), "Enter First Name", Toast.LENGTH_SHORT).show();
            b = false;
        } else if (TextUtils.isEmpty(ed_phone.getText().toString())) {
            Toast.makeText(getActivity(), "Enter Phone", Toast.LENGTH_SHORT).show();
            b = false;
        } else if (TextUtils.isEmpty(ed_Email.getText().toString())) {
            Toast.makeText(getActivity(), "Enter Email", Toast.LENGTH_SHORT).show();
            b = false;
        } else if (TextUtils.isEmpty(ed_postalCode.getText().toString())) {
            Toast.makeText(getActivity(), "Enter Postal Code", Toast.LENGTH_SHORT).show();
            b = false;
        } else if (TextUtils.isEmpty(ed_city.getText().toString())) {
            Toast.makeText(getActivity(), "Enter City", Toast.LENGTH_SHORT).show();
            b = false;
        } else {
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
//                selectedCountry = selectedCountry.replace(" ", "");
                if (checkCall == 0) {
                    setProvinceAdapter();
                    setProvinceArrayFromString(selectedCountryPosition);
                }
                if (checkCall > 2) {
                    setProvinceArrayFromString(selectedCountryPosition);
                }
                spinnerProvience.setOnItemSelectedListener(this);

                Toast.makeText(getActivity(), "Selected Country: " + selectedCountry, Toast.LENGTH_LONG).show();
                Log.d("Selected Country :", selectedCountry);
                Log.d("Check : ", String.valueOf(checkCall));
                break;

            case R.id.spinnerProvince:
                selectedProvince = String.valueOf(parent.getSelectedItem());
//                selectedProvince = selectedProvince.replace(" ","");
                Log.d("Selected Province :", selectedProvince);
                Toast.makeText(getActivity(), "Selected province: " + selectedProvince, Toast.LENGTH_LONG).show();
                Log.d("Check : ", String.valueOf(checkCall));
                break;
        }

    }

    private void setProvinceAdapter() {
        provinceAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, provinceArrayList);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvience.setAdapter(provinceAdapter);
        provinceAdapter.notifyDataSetChanged();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    private void setProvinceArrayFromString(int selectedCountryPosition) {

        String province = String.valueOf(constant.setProvinceArray()[selectedCountryPosition]);
        StringTokenizer st = new StringTokenizer(province, "|");
        provinceArrayList = new ArrayList<>();
        while (st.hasMoreTokens()) {
            provinceArrayList.add(st.nextToken());
        }
        setProvinceAdapter();
    }


    private class GetProfileData{
        String newId;
        public GetProfileData(String userid){
            this.newId = userid;

            RestClient.get().getProfileData(newId, new Callback<ProfileResponse>() {

                @Override
                public void success(ProfileResponse profileResponse, Response response) {
                    ed_Fname.setText(profileResponse.getFIRSTNAME());
                    ed_Lname.setText(profileResponse.getLASTNAME());
                    ed_Email.setText(profileResponse.getEMAIL());
                    ed_Address1.setText(profileResponse.getADDRESS1());
                    ed_Address2.setText(profileResponse.getADDRESS2());
                    ed_postalCode.setText(profileResponse.getPOSTALCODE());
                    ed_city.setText(profileResponse.getCITY());
                    ed_phone.setText(profileResponse.getCELLPHONE());
                    String userSelectedCountry = profileResponse.getCOUNTRY();
                    String userSelectedProvince = profileResponse.getPROVINCE();
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getContext(), "Profile Updation Failed", Toast.LENGTH_LONG).show();
                    System.out.println("Profile Updation Failed");
                }
            });


        }

    }

    private void setSpinnerWithDynamicValues(String userSelectedCountry, String userSelectedProvince) {

        int selectedCountryIndex = Arrays.asList(constant.countriesArray).indexOf(userSelectedCountry);
        spinnerCountry.setSelection(selectedCountryIndex);

        setProvinceArrayFromString(selectedCountryIndex);

        int selectedProvinceIndex = provinceArrayList.indexOf(userSelectedProvince);
        spinnerProvience.setSelection(selectedProvinceIndex);
        provinceAdapter.notifyDataSetChanged();

    }

    private class UpdateProfileData extends AsyncTask<String, Void, String> {

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
            if (s.equals("updated successfully")) {
                Toast.makeText(getActivity(), "Profile Updated..", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Error in Profile Updation..", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
