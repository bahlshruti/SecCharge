package com.aidor.secchargemobile.seccharge;

/**
 * Created by ritesh on 07/11/2015.
 */

import android.app.ProgressDialog;
import android.content.Context;

public class Spinner {


    ProgressDialog progressDialog;
    Context context;

    public Spinner(Context context) {

        this.context = context;
    }

    public void showDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait..");
        progressDialog.show();
        progressDialog.setIndeterminate(true);
    }

    public void hideDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.hide();
    }


}
