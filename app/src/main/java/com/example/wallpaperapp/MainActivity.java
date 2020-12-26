package com.example.wallpaperapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int MY_REQEST_CODE = 1234;
    private static final int PRIVATE_REQUEST_ID = 11;
    private static final String INTERVAL_HOURS_KEY = "interval_hours";
    public static final String IMAGE_ARRAY_KEY = "images_name_array";
    public static final String IMAGE_INDEX = "images_index";

    private SharedPreferences mPreferences;
    private String SHARED_PREF_FILE_NAME = "com.example.wallpaperapp";

    private Spinner spnInerval;
    private TextView txtIndex;
    private ToggleButton btnStartStop;
    private RecyclerView recviewImageList;
    private OutputStream outputStream;
    private ImagesRecViewAdapter recviewAdapter;
    private AlarmManager alarmManager;
    private Intent alarmIntent;
    private PendingIntent alarmPendingIntent;
    private String appImagesPath;
    private HashMap<String, Integer> intervalMap;
    private int selectedIntervalHour;
    private int pictureIndex;
    private List<String> intervalsText;
    private List<Integer> intervalsNumber;
    private boolean isAlarmAlreadySet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedIntervalHour = 1;
        pictureIndex = 0;

        //Set internal variables for Views
        txtIndex = findViewById(R.id.txtIndex);
        spnInerval = findViewById(R.id.spnInerval);
        recviewImageList = findViewById(R.id.recviewImageList);
        btnStartStop = findViewById(R.id.btnStartStop);

        //Set event handlers
        spnInerval.setOnItemSelectedListener(new spnInervalOnItemSelected());
        btnStartStop.setOnCheckedChangeListener(new btnStartStopChanged());

        //Get the pictures directory that's inside the app-specific directory on external storage
        appImagesPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

        //Get the shared preferences for reading app saved data
        mPreferences = getSharedPreferences(SHARED_PREF_FILE_NAME, MODE_PRIVATE);

        ArrayList<ImageModel> imagesList = getImagesList();
        recviewAdapter = new ImagesRecViewAdapter(this);
        recviewAdapter.setImagesList(imagesList);
        recviewImageList.setAdapter(recviewAdapter);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recviewImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        } else {
            recviewImageList.setLayoutManager(new LinearLayoutManager(this));
        }
        //recviewImageList.setLayoutManager(new GridLayoutManager(this, 2));

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmIntent = new Intent(this, AlarmReceiver.class);
        setSpinnerData();
        checkToggleButton();
    }

    @Override
    protected void onPause() {
        super.onPause();

        SaveSharedData();
//
//        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
//        preferencesEditor.putInt(IMAGE_ARRAY_KEY, selectedIntervalHour);
//        //preferencesEditor.putInt(IMAGE_INDEX, pictureIndex);
//
//        preferencesEditor.apply();
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

    //Create shared object and save the needed data
    private void SaveSharedData() {
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(IMAGE_ARRAY_KEY, selectedIntervalHour);
        preferencesEditor.apply();
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
            recviewAdapter.setImagesList(getImagesList());
            Toast.makeText(this, "Image Saved to " + dir.getName(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "Exception in Select Image!");
            e.printStackTrace();
        }
    }

    private ArrayList<ImageModel> getImagesList() {
        ArrayList<ImageModel> imageList = new ArrayList<>();
        File dir = getAppSpecificPictureStorageDir();
        if (dir != null && dir.exists()) {
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

    private String[] getImagesNameArray(ArrayList<ImageModel> imageList) {
        String array[] = new String[imageList.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = imageList.get(i).getFile().getAbsolutePath();
        }
        return array;
    }

    @Nullable
    private File getAppSpecificPictureStorageDir() {
        if(appImagesPath == null || appImagesPath.trim().isEmpty()) {
            Log.e(LOG_TAG, "appImagesPath is null or empty");
            return null;
        }

        File dir = new File(appImagesPath);
        if (dir != null && dir.exists()) {
            Log.i(LOG_TAG, String.format("Directory already exists - %s", appImagesPath));
        } else if (!dir.mkdirs()) {
            Log.e(LOG_TAG, String.format("File.mkdirs() returns false - %s", appImagesPath));
        } else {
            Log.i(LOG_TAG, String.format("New directory created - %s", appImagesPath));
        }
        return dir;
    }

    private void checkToggleButton() {
        if (alarmIntent == null) return;

        alarmPendingIntent = PendingIntent.getBroadcast(this,
                PRIVATE_REQUEST_ID, alarmIntent, PendingIntent.FLAG_NO_CREATE);

        if (alarmPendingIntent != null) {
            isAlarmAlreadySet = true;
            selectedIntervalHour = mPreferences.getInt(IMAGE_ARRAY_KEY, 1);
            pictureIndex = mPreferences.getInt(IMAGE_INDEX, 0);
            txtIndex.setText(String.valueOf(pictureIndex));

            Integer position = intervalsNumber.indexOf(selectedIntervalHour);
            spnInerval.setSelection(position);
        } else {
            isAlarmAlreadySet = false;
        }
        btnStartStop.setChecked(isAlarmAlreadySet);
    }

    private void setSpinnerData() {
        intervalsText = Arrays.asList("1 hour", "2 hours", "6 hours", "12 hours", "day", "week");
        intervalsNumber = Arrays.asList(1, 2, 6, 12, 24, 24*7);

        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(
                                    this, R.layout.spinner_item, intervalsText);

//        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(
//                this, android.R.layout.simple_spinner_dropdown_item, intervalsText);
                                             //simple_spinner_item
                                             //simple_dropdown_item_1line
                                             //simple_selectable_list_item

        spnInerval.setAdapter(intervalAdapter);
    }

    private void startAlarm() {
        if (isAlarmAlreadySet) return;
        long repeatInterval = 5000;//selectedIntervalHour * 3600 * 1000; //AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
        alarmIntent.putExtra(IMAGE_ARRAY_KEY , getImagesNameArray(getImagesList()));
        alarmPendingIntent = PendingIntent.getBroadcast(MainActivity.this,
                PRIVATE_REQUEST_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmManager != null && alarmPendingIntent != null) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime, repeatInterval, alarmPendingIntent);
            Toast.makeText(MainActivity.this, "Alarm is On", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAlarm() {
        if (alarmManager != null && alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent);
            alarmPendingIntent.cancel();
            Toast.makeText(MainActivity.this, "Alarm is Off!", Toast.LENGTH_SHORT).show();
        }
    }

    //region Event Handlers

    //btnStartStopChanged event handler
    class btnStartStopChanged implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                startAlarm();
            } else {
                stopAlarm();
            }
        }
    }

    //spinnerInerval OnItemSelected event handler
    class spnInervalOnItemSelected implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedIntervalHour = intervalsNumber.get(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    //endregion

}