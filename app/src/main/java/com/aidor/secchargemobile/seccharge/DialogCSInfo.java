package com.aidor.secchargemobile.seccharge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;

import com.aidor.projects.seccharge.R;

/**
 * DIalog Fragment for Charging Site additional details
 */
public class DialogCSInfo extends DialogFragment {
    ListView listCsInfo;



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        String[] fetchedText = DetailInfoActivity.fetchedText;
        String[] preText = {"SiteType", "Site Owner",  "Site Number", "Site Phone", "Level 2 Price", "FastDC Price", "Access Type Time", "Usage Type"
        };



        View view = inflater.inflate(R.layout.dialog_cs_info, null);

        builder.setView(view);

        listCsInfo = (ListView) view.findViewById(R.id.listCsInfo);

        InfoDetailAdapter adapter = new InfoDetailAdapter((Activity) DetailInfoActivity.context, preText, fetchedText);

        listCsInfo.setAdapter(adapter);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // sign in the user ...
                getDialog().dismiss();
            }
        });


        return builder.create();
    }
}
