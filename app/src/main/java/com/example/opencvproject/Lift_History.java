package com.example.opencvproject;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;





import com.example.opencvproject.detector.DatabaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Lift_History extends AppCompatActivity {



    DatabaseHelper mDatabaseHelper = new DatabaseHelper(this);
       private TableLayout table;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lift__history);
        table = findViewById(R.id.historyTable);
        createTable();


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


    private void createTable()
    {
        final Cursor res = mDatabaseHelper.getData();

        while (res.moveToNext())
        {
            TableRow tr = new TableRow(this);

            TextView tview1 = new TextView(this);
            TextView tview2 = new TextView(this);
            Button b1 = new Button(this);

            tview1.setText(res.getString(1));
            tview2.setText(res.getString(2));
            b1.setText("View Image");

            final String uriString = res.getString(3);
            final Uri uri = Uri.parse(uriString);

            b1.setOnClickListener( new View.OnClickListener()
                                   {
                                       @Override
                                       public void onClick(View view)
                                       {
//                                            Intent intent = new Intent();
//                                            intent.setAction(Intent.ACTION_VIEW);
//                                            intent.setDataAndType(uri, "image/*");
//                                            startActivity(intent);
                                           final Intent intent = new Intent(Lift_History.this,ImageReview.class);
                                           intent.putExtra(ImageReview.EXTRA_PHOTO_URI, uri);
//                                           intent.putExtra(ImageReview.EXTRA_PHOTO_DATA_PATH,
//                                                   photoPath);
                                           startActivity(intent);
                                       }
                                   }
            );

            tr.addView(tview1);
            tr.addView(tview2);
            tr.addView(b1);

            table.addView(tr);
        }

    }

    public void saveCSV(View view)
    {
        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        final String appName = getString(R.string.app_name);




        Cursor res = mDatabaseHelper.getData();
        if (res.getCount() == 0) {
            toastMessage("Database Empty");
            return;
        } else {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Index, Date, Deviation \n");
            while (res.moveToNext())
            {
                buffer.append(res.getString(0) + ",");
                buffer.append(res.getString(1) + ",");
                buffer.append(res.getString(2) + "\n");



            }
            //showMessage("Data", buffer.toString());
            final File path =
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOCUMENTS );

            if(!path.exists())
            {
                path.mkdirs();
            }
            String filename = "Lift_History" + date + ".csv";
            final File file  = new File(path, filename);


            try
            {
                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWrite = new OutputStreamWriter(fOut);
                myOutWrite.append(buffer);

                myOutWrite.close();

                fOut.flush();
                fOut.close();

                toastMessage("Written To Documents Folder");
            }

            catch(IOException e)
            {
                Log.e("Exception", "WriteFailed");
            }

        }

    }
}
