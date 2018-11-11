package com.aidor.secchargemobile.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.aidor.secchargemobile.custom.SecCharge;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "seccharge";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table place_history" +
                "(id integer primary key, place text UNIQUE)");
        System.out.print("Database Created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public boolean insertData(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("place", name);
            db.insert("place_history", null, contentValues);
        } catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }
    public ArrayList<String> retriveData() {
        ArrayList<String> place = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.rawQuery("select place from place_history", null);
        if (cur != null && cur.moveToFirst()) {
            while(cur.isAfterLast() == false) {
                place.add(cur.getString(0));
                cur.moveToNext();
            }
            return place;
        } else {
            place.add("no_data");
            return place;
        }
    }
}
