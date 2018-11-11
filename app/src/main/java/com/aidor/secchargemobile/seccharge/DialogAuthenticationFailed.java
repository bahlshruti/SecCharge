package com.aidor.secchargemobile.seccharge;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


/**
 * Created by sahajarora1286 on 2016-02-25.
 */
public class DialogAuthenticationFailed extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Authentication Failed");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // do what has to be done when user clicks on OK
                getDialog().dismiss();
            }
        });


        return builder.create();
    }
}
