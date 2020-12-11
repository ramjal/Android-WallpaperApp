package com.example.wallpaperapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "StoreImageExternal";
    private static final String DIR_NAME = "WallPaper";
    private static final int MY_REQEST_CODE = 1234;
    public static final int PRIVATE_REQUEST_ID = 11;

    private ToggleButton btnStartStop;
    private RecyclerView recviewImageList;
    private OutputStream outputStream;
    private ImagesRecViewAdapter recAdapter;
    private AlarmManager alarmManager;
    private Intent alarmIntent;
    private PendingIntent alarmPendingIntent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recviewImageList = findViewById(R.id.recviewImageList);
        btnStartStop = findViewById(R.id.btnStartStop);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmIntent = new Intent(this, AlarmReceiver.class);
        checkToggleButton();
        
        recAdapter = new ImagesRecViewAdapter(this);
        recAdapter.setImagesList(getImageList());
        recviewImageList.setAdapter(recAdapter);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recviewImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        } else {
            recviewImageList.setLayoutManager(new LinearLayoutManager(this));
        }
        //recviewImageList.setLayoutManager(new GridLayoutManager(this, 2));

        btnStartStop.setOnCheckedChangeListener(new btnStartStopChanged());
    }

    public void btnAddImageClicked(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1234);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    addImageFileToAppStorage(data.getData());
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addImageFileToAppStorage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            File dir = getAppSpecificPictureStorageDir();
            if (dir == null || !dir.exists()) {
                Toast.makeText(this, "Cannot find directory: " + dir.getName(), Toast.LENGTH_LONG).show();
                return;
            }

            //Create new instance of a file
            File file = new File(dir, System.currentTimeMillis() + ".jpg");
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            outputStream.flush();
            outputStream.close();
            recAdapter.setImagesList(getImageList());
            Toast.makeText(this, "Image Saved to " + dir.getName(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "Exception in Select Image!");
            e.printStackTrace();
        }
    }

    private ArrayList<ImageModel> getImageList() {
        ArrayList<ImageModel> imageList = new ArrayList<>();
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), DIR_NAME);
        if (dir.exists()) {
            File[] allFiles = dir.listFiles();
            if (allFiles.length == 0) {
                Log.e(LOG_TAG, "No file found in " + dir.getAbsolutePath());
            } else {
                for (File file : allFiles) {
                    imageList.add(new ImageModel(file, file.getName()));
                }
            }
        }
        return imageList;
    }

    @Nullable
    private File getAppSpecificPictureStorageDir() {
        // Get the pictures directory that's inside the app-specific directory on external storage
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), DIR_NAME);
        if (file == null || !file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }

    private void checkToggleButton() {

        if (alarmIntent == null) return;

        PendingIntent pi = PendingIntent.getBroadcast(this,
                PRIVATE_REQUEST_ID, alarmIntent, PendingIntent.FLAG_NO_CREATE);

        btnStartStop.setChecked(pi != null);
    }

    class btnStartStopChanged implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            long repeatInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
            long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;

            if (isChecked) {
                alarmPendingIntent = PendingIntent.getBroadcast(MainActivity.this,
                        PRIVATE_REQUEST_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                if (alarmManager != null && alarmPendingIntent != null) {
                    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime, repeatInterval, alarmPendingIntent);
                    Toast.makeText(MainActivity.this, "Alarm is On", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (alarmManager != null && alarmPendingIntent != null) {
                    alarmManager.cancel(alarmPendingIntent);
                    Toast.makeText(MainActivity.this, "Alarm is Off!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}