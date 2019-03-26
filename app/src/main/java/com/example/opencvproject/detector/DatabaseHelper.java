package com.example.opencvproject.detector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "Lift_History";
    private static final String COL0 = "myIndex";
    private static final String COL1 = "Date";
    private static final String COL2 = "Variance";
    private static final String COL3 = "IMGURI";



    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null,1 );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" + COL0 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL1 + " TEXT, " + COL2 + " TEXT, " + COL3 + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String date, String var, String uri)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, date);
        contentValues.put(COL2, var);
        contentValues.put(COL3, uri);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if(result == -1) {
            return false;
        }
        else{
            return true;
        }
    }

    public boolean deleteData(int index)
    {
        String stringIndex = Integer.toString(index);
        SQLiteDatabase db = this.getWritableDatabase();

        int numdeleted = db.delete(TABLE_NAME, "COL0 = ?", new String[]{stringIndex});

        if (numdeleted==0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }


    public Cursor getData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }
}
