package com.aidor.secchargemobile.seccharge;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aidor.projects.seccharge.R;

import java.util.ArrayList;

/**
 * Created by sahilsiwach on 11/5/2015.
 */
public class ShowEvListViewAdapter extends BaseAdapter implements deleteCallback {

    ArrayList<ShowEVModel> arraylist;
    Context context;
    View view;
    ShowEvFragment frag;
    deleteCallback mDeleteCallback;


    public ShowEvListViewAdapter(ShowEvFragment frag, Context context, ArrayList<ShowEVModel> arrayList) {
        this.frag = frag;
        this.arraylist = arrayList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return arraylist.size();
    }

    @Override
    public Object getItem(int position) {
        return arraylist.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            //getting position of elements from model
            ShowEVModel model = this.arraylist.get(i);

            view = layoutInflater.inflate(R.layout.show_ev_row_item, null);
            TextView txtCarYear = (TextView) view.findViewById(R.id.textVcarYear);
            TextView txtCarMake = (TextView) view.findViewById(R.id.textVcarMake);
            TextView txtCarModel = (TextView) view.findViewById(R.id.textVcarModel);
            ImageView imgEditVehicle = (ImageView) view.findViewById(R.id.imageViewEditVehicle);
            ImageView imgDeleteVehicle = (ImageView) view.findViewById(R.id.imageViewDeleteVehicle);

            imgDeleteVehicle.setTag(this.arraylist.get(i).getVehicle_ID() + "," + i);
            imgDeleteVehicle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (frag != null) {
                        // ShowEVModel.getInstance().setVehicle_ID(view.getTag().toString());

                        AlertDialog.Builder ad = new AlertDialog.Builder(context);
                        ad.setMessage("Are you sure you want to delete this vehicle?");
                        ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteRowItem(view.getTag().toString());
                            }
                        });
                        ad.setNegativeButton("No", null);
                        ad.setCancelable(true);
                        ad.show();


//                        mDeleteCallback.deleteItem(pos);
//
//                        arraylist.remove(pos);
//                        notifyDataSetChanged();
                    }
                }
            });


            //settinh the data in list using model
            txtCarYear.setText(model.getCar_Year());
            txtCarMake.setText(model.getCar_Make());
            txtCarModel.setText(model.getCar_Model());

            //get vehicle id on clicking imageview using setTag from arraylist
            imgEditVehicle.setTag(this.arraylist.get(i).getVehicle_ID());
            imgEditVehicle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //getting vID from model using instance
                    ShowEVModel.getInstance().setVehicle_ID(view.getTag().toString());
                    if (frag != null) {
                        frag.startEditVehicle();
                    }
                }
            });

        } else {
            view = (View) convertView;
        }
        return view;
    }

    public void deleteRowItem(String pos) {
        String vehicleId = pos.split(",")[0];
        String listRowId = pos.split(",")[1];
        Log.d("SET_POS:", String.valueOf(listRowId));
//        vehicleDeleteIDModel.setListRowId(listRowId);
//        int temp = Integer.parseInt(vehicleDeleteIDModel.getListRowId());
//        Log.d("SET_POS_SET:", String.valueOf(temp));
        VehicleDeleteIDModel.getInstance().setVID(vehicleId);
        VehicleDeleteIDModel.getInstance().setListRowId(listRowId);
        frag.deleteVehicle(this);
    }

    public void deleteNowItem() {
//        int list_RowId = Integer.parseInt(VehicleDeleteIDModel.getInstance().getListRowId());
//        frag.deleteVehicle();

//        Log.d("GET_POS:", String.valueOf(list_RowId));
//        int pos = Integer.parseInt(VehicleDeleteIDModel.getInstance().getVID());
//        this.arraylist.remove(arraylist.get(0));
//        ArrayList<ShowEVModel> tempArrayList = this.arraylist;
//        notifyDataSetInvalidated();
//        this.arraylist = tempArrayList;
//        Log.d("arrayList: ", String.valueOf(arraylist));

//        notifyDataSetChanged();
    }

    @Override
    public void deleteItem(ArrayList<ShowEVModel> showEVModelList) {
        this.arraylist = showEVModelList;
        for (ShowEVModel showEvModel:arraylist
             ) {
            Log.i("UPDATED_LIST", showEvModel.getCar_Model());
        }
        notifyDataSetChanged();
    }


    public void resetAdapterData(ArrayList<ShowEVModel> list) {
        this.arraylist = list;
        notifyDataSetChanged();
    }
}
