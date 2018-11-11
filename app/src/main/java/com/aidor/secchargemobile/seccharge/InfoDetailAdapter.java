package com.aidor.secchargemobile.seccharge;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.aidor.projects.seccharge.R;


public class InfoDetailAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] preText;
    private final String[] fetchedText;


    public InfoDetailAdapter(Activity context, String[] preText, String[] fetchedText) {
        super(context, R.layout.info_detail_customlist, fetchedText);
        this.context = context;
        this.preText = preText;
        this.fetchedText = fetchedText;

    }
    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.info_detail_customlist, null, true);

        TextView titleText = (TextView) rowView.findViewById(R.id.detailtv);
        TextView showText = (TextView) rowView.findViewById(R.id.fetcheddetailtv);


        titleText.setText(preText[position]);
        showText.setText(fetchedText[position]);




        return rowView;

    };
}
