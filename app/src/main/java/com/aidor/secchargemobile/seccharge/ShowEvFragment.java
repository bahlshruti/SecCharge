package com.aidor.secchargemobile.seccharge;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ShowEvFragment extends Fragment {

    View view;
    Context context;
    SharedPreferences pref;
    ShowEvFragment frag;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String UserID = "UserID";
    String userID;
    String id;
    int VID;
    String NewVID;

    ArrayList<ShowEVModel> showEVModelList;
    private View.OnClickListener onclicklistener;

    ShowEvListViewAdapter showEvListViewAdapter;
    ListView showEvListview;

    AlertDialog.Builder alertBuilder = null;
    SharedPreferences sharedpreferences;
    private deleteCallback mDeleteCallback;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        checkNetwork();
        view = inflater.inflate(R.layout.fragment_my_ev, container, false);

        context = getActivity().getApplicationContext();
        frag = this;
        pref = this.getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userID = pref.getString(UserID, "");
        if (showEvListview == null) {
            showEvListview = (ListView) view.findViewById(R.id.listViewShowEv);
        }
        pref = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        /*call Webservice to show vehicle*/
        GetVehicleData getVehicleData = new GetVehicleData();
        getVehicleData.execute();

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
    private class GetVehicleData extends AsyncTask<String, Void, String> {
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
                System.out.println("Back username SHow EV -->> " + userID);
                HttpClient httpClient = new DefaultHttpClient();
                String NewURL = URL + "register/vehicledata?userid=" + userID;

                HttpGet httpGet = new HttpGet(NewURL);

//                HttpPost httpPost = new HttpPost("http://.2.12:8011/seccharge/test/register/usernameExists");
//                List<NameValuePair> para = new ArrayList<>();
//                para.add(new BasicNameValuePair("email", sEmail));
//                para.add(new BasicNameValuePair("password", sPass));
                // Encoding POST data
//                httpPost.setEntity(new UrlEncodedFormEntity(para));
                // Making HTTP Request

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
                System.out.println("DATA SHow EV --> " + return_text);
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
            Log.d("SHow EV RES:", s);

            try {
                showEVModelList = new ArrayList<ShowEVModel>();
                final JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    ShowEVModel model = new ShowEVModel();
                    model.setVehicle_ID(jsonArray.getJSONObject(i).getString("ELECTRICAL_VEHICLEID"));
                    model.setCar_Year(jsonArray.getJSONObject(i).getString("YEAR"));
                    model.setCar_Make(jsonArray.getJSONObject(i).getString("MAKE"));
                    model.setCar_Model(jsonArray.getJSONObject(i).getString("MODEL"));
                    showEVModelList.add(model);
                }
                setadapter(showEVModelList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setadapter(ArrayList<ShowEVModel> list) {
        if (showEvListViewAdapter == null) {
            showEvListViewAdapter = new ShowEvListViewAdapter(frag, getActivity(), list);
        }
        showEvListview.setAdapter(showEvListViewAdapter);
    }

    public void startEditVehicle() {
        Edit_Vehicle_Fragment editEvFragment = new Edit_Vehicle_Fragment();
        //showEvFragment.getFragmentManager().beginTransaction().replace(R.id.container,showEvFragment).commit();
        android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                            Bundle bundle = new Bundle();
//                            bundle.putInt("ID", id);
//                            showEvFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.container_myEv, editEvFragment);
        fragmentTransaction.commit();
    }

    public void showAlertDialog(int id, View v) {
        VID = id;
        this.view = v;
        Log.d("VID:", String.valueOf(id));
        alertBuilder = new AlertDialog.Builder(getActivity());
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Delete Vehicle");
        alertBuilder.setMessage("Are You Sure Want to Delete?");
        alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alertBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertBuilder.create();

    }


    public void deleteVehicle(deleteCallback deleteCallback) {
        mDeleteCallback = deleteCallback;
        NewVID = String.valueOf(VehicleDeleteIDModel.getInstance().getVID());
        Log.d("EV ID", NewVID);
        DeleteVehicleWebService deleteVehicleWebService = new DeleteVehicleWebService();
        deleteVehicleWebService.execute();
    }

    private class DeleteVehicleWebService extends AsyncTask<String, Void, String> {
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
                System.out.println("Back_EV-ID delete -->> " + NewVID);
                System.out.println("Back_UserID delete -->> " + userID);


                HttpClient httpClient = new DefaultHttpClient();

                String NewURL = URL + "deleteVehicle";

                Log.d("NEWURL", NewURL);

                HttpPost httpPost = new HttpPost(NewURL);
                List<NameValuePair> para = new ArrayList<>();
                para.add(new BasicNameValuePair("userid", userID));
                para.add(new BasicNameValuePair("id", NewVID));

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
                System.out.println("DATA Delete EV --> " + return_text);
                return return_text;
            } catch (ClientProtocolException | UnsupportedEncodingException clientEx) {
                clientEx.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "false";
        }

        @Override
        public void onPostExecute(String s) {

            super.onPostExecute(s);
            Log.d("POST DELETE RES:", s);
            if (s.equals("deleted successfully")) {

                Toast.makeText(getActivity(), "Vehicle deleted successfully!", Toast.LENGTH_SHORT).show();
                Message msg = new Message();
                msg.arg1 = Integer.parseInt(VehicleDeleteIDModel.getInstance().getListRowId());
                mHandler.sendMessage(msg);

//                getResult(true);

//                        showEVModelList.remove(Integer.parseInt(VehicleDeleteIDModel.getInstance().getListRowId()));
//                        mDeleteCallback.deleteItem(showEVModelList);
//                        Toast.makeText(getActivity(), "Vehicle daleted Position.." + VehicleDeleteIDModel.getInstance().getListRowId(), Toast.LENGTH_SHORT).show();


//                showEvListViewAdapter.arraylist.remove(showEvListViewAdapter.arraylist.get(Integer.parseInt(VehicleDeleteIDModel.getInstance().getListRowId())));
//                showEvListViewAdapter.notifyDataSetChanged();

            } else if (s.equals("false")) {
                Toast.makeText(getActivity(), "Sorry! An error occured while deleting the vehicle. Please try again later.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int position = msg.arg1;
            showEVModelList.remove(position);
            //showEvListViewAdapter.resetAdapterData(showEVModelList);
            showEvListViewAdapter.notifyDataSetChanged();
        }
    };
}
