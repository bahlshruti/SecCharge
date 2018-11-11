package com.aidor.secchargemobile.seccharge;

import android.app.ProgressDialog;
import android.content.Context;

public class CommonProgressDialog {


    Context context;

    ProgressDialog progressDialog ;

    public CommonProgressDialog(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
    }

    public void showDialog() {
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait..");
        progressDialog.show();
        progressDialog.setIndeterminate(true);
    }

    public void hideDialog() {
        progressDialog.hide();
    }

}
