package com.aidor.secchargemobile.database;

        import android.database.sqlite.SQLiteOpenHelper;
        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.SQLException;
        import android.database.sqlite.SQLiteDatabase;

import com.aidor.secchargemobile.model.CsSiteModel;


public class SqlLiteDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

// Database Name

    private static final String DATABASE_NAME = "seccharge_csSite.db";

    private static final String DB_PATH_SUFFIX = "/databases/";

    static Context ctx;

    public SqlLiteDbHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        ctx = context;

    }



// Getting single contact

    public CsSiteModel getCsSiteDetails(String id) {

        SQLiteDatabase db = this.getReadableDatabase();



        Cursor cursor = db.rawQuery("SELECT * FROM cs_site WHERE ID='"+id+"'", null);

        if (cursor != null && cursor.getCount()>0 && cursor.moveToFirst()){

            CsSiteModel cont = new CsSiteModel(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)
                    , cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9), cursor.getString(10), cursor.getString(11)
                    , cursor.getString(12), cursor.getString(13), cursor.getString(14));

// return contact

            cursor.close();

            db.close();

            return cont;

        }

        return null;

    }



    public void CopyDataBaseFromAsset() throws IOException{

        InputStream myInput = ctx.getAssets().open(DATABASE_NAME);



// Path to the just created empty db

        String outFileName = getDatabasePath();


// if the path doesn't exist first, create it

        File f = new File(ctx.getApplicationInfo().dataDir + DB_PATH_SUFFIX);

        if (!f.exists())

        f.mkdir();

// Open the empty db as the output stream

        OutputStream myOutput = new FileOutputStream(outFileName);



// transfer bytes from the inputfile to the outputfile

        byte[] buffer = new byte[1024];

        int length;

        while ((length = myInput.read(buffer)) > 0) {

            myOutput.write(buffer, 0, length);

        }

// Close the streams

        myOutput.flush();

        myOutput.close();

        myInput.close();


        }

    private static String getDatabasePath() {

        return ctx.getApplicationInfo().dataDir + DB_PATH_SUFFIX

                + DATABASE_NAME;

    }

    public SQLiteDatabase openDataBase() throws SQLException{

        File dbFile = ctx.getDatabasePath(DATABASE_NAME);

        if (!dbFile.exists()) {

            try {

                CopyDataBaseFromAsset();

                System.out.println("Copying sucess from Assets folder");

            } catch (IOException e) {

                throw new RuntimeException("Error creating source database", e);

            }

        }

        return SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);

    }

    @Override

    public void onCreate(SQLiteDatabase db) {

    }


    @Override

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

// TODO Auto-generated method stub

    }
}
