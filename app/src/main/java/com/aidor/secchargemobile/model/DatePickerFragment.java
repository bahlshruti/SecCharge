package com.aidor.secchargemobile.model;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.aidor.secchargemobile.seccharge.ReserveMainActivity;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment
        {
            private long serverDateTime;



            @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker

                SimpleDateFormat sdfDateServer = new SimpleDateFormat("yyyy-MM-dd");
                Date dateServer = null;
                try {
                    dateServer = sdfDateServer.parse(ReserveMainActivity.serverDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //System.out.println("DATEPICKER SERVERTIME : " + dateServer.toString());

                serverDateTime = dateServer.getTime() - 1000;
                Calendar c = Calendar.getInstance();
                c.setTime(dateServer);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (ReserveMainActivity)getActivity(), year, month, day);
        datePickerDialog.getDatePicker().setMinDate(serverDateTime);
        // Create a new instance of DatePickerDialog and return it
        return datePickerDialog;
    }


}