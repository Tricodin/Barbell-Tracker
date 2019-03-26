package com.example.opencvproject;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.example.opencvproject.detector.DatabaseHelper;

public class Lift_History extends AppCompatActivity {

    DatabaseHelper mDatabaseHelper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lift__history);


    }

    public void showData(View v) {
        Cursor res = mDatabaseHelper.getData();
        if (res.getCount() == 0) {
            toastMessage("Database Empty");
            return;
        } else {
            StringBuffer buffer = new StringBuffer();
            while (res.moveToNext())
            {
                buffer.append("Index :"+ res.getString(0) + "\n");
                buffer.append("Date :"+ res.getString(1) + "\n");
                buffer.append("Deviation :"+ res.getString(2) + "\n");
                buffer.append("Uri :"+ res.getString(3) + "\n\n");


            }
            showMessage("Data", buffer.toString());
        }
    }

    public void showMessage(String title, String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }
    private void toastMessage(String message)
    {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

}
