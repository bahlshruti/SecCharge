package com.aidor.secchargemobile.seccharge;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.model.ServerTimeModel;
import com.aidor.secchargemobile.rest.RestClientReservation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class TimeAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] timeValues;
    private ArrayList<Integer> positionsToDisable;
    public static String serverTime;
    public static boolean result;
    private boolean allDisabled;



    public TimeAdapter(Activity context, String[] timeValues, ArrayList<Integer> positionsToDisable) {
        super(context, R.layout.list_item_time, timeValues);
        this.context = context;
        this.timeValues = timeValues;
        this.positionsToDisable = positionsToDisable;
        allDisabled = false;
        if (positionsToDisable.size() == timeValues.length){
            allDisabled = true;
        }

    }


    @Override
    public boolean isEnabled(final int position){
        if (positionsToDisable.contains(position)){
            return false;
        } else return true;
    }




    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.list_item_time, null, true);


        TextView tvTimeListItem = (TextView) rowView.findViewById(R.id.tvTimeListItem);

        tvTimeListItem.setText(timeValues[position]);

        if (positionsToDisable.contains(position)){
            tvTimeListItem.setBackgroundColor(Color.LTGRAY);
        }

        return rowView;

    };


}
