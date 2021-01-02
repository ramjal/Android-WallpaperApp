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
import androidx.appcompat.widget.SwitchCompat;
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
    private static final int PRIVATE_REQUEST_ID = 11;
    private static final String INTERVAL_HOURS_KEY = "interval_hours";
    public static final String IMAGE_ARRAY_KEY = "images_name_array";
    public static final String IMAGE_INDEX = "images_index";

    private SharedPreferences mPreferences;
    private String SHARED_PREF_FILE_NAME = "com.example.wallpaperapp";

    private Spinner spnInerval;
    private TextView txtIndex;
    private TextView textLabel;
    private SwitchCompat btnStartStop;
    private AlarmManager alarmManager;
    private Intent alarmIntent;
    private PendingIntent alarmPendingIntent;
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
        textLabel = findViewById(R.id.textLabel);
        spnInerval = findViewById(R.id.spnInerval);
        btnStartStop = findViewById(R.id.btnStartStop);

        //Set event handlers
        spnInerval.setOnItemSelectedListener(new spnInervalOnItemSelected());
        btnStartStop.setOnCheckedChangeListener(new btnStartStopChanged());

        //Get the shared preferences for reading app saved data
        mPreferences = getSharedPreferences(SHARED_PREF_FILE_NAME, MODE_PRIVATE);

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

    //Create shared object and save the needed data
    private void SaveSharedData() {
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(IMAGE_ARRAY_KEY, selectedIntervalHour);
        preferencesEditor.apply();
    }

    private String[] getImagesNameArray(ArrayList<ImageModel> imageList) {
        String array[] = new String[imageList.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = imageList.get(i).getFile().getAbsolutePath();
        }
        return array;
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
            spnInerval.setEnabled(false);
        } else {
            isAlarmAlreadySet = false;
            spnInerval.setEnabled(true);
        }
        btnStartStop.setChecked(isAlarmAlreadySet);
    }

    private void setSpinnerData() {
        intervalsText = Arrays.asList("1 hour", "2 hours", "6 hours", "12 hours", "day", "week");
        intervalsNumber = Arrays.asList(1, 2, 6, 12, 24, 24*7);

        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(
                                    this, R.layout.spinner_item, intervalsText);
//
//        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(
//                this, android.R.layout.simple_spinner_dropdown_item, intervalsText);
                                             //simple_spinner_item
                                             //simple_dropdown_item_1line
                                             //simple_selectable_list_item

        spnInerval.setAdapter(intervalAdapter);
    }

    private void startAlarm() {
        if (isAlarmAlreadySet) return;
        //long repeatInterval = 5000;
        long repeatInterval = selectedIntervalHour * 3600 * 1000; //AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
        alarmIntent.putExtra(IMAGE_ARRAY_KEY , getImagesNameArray(ImageUtils.getImagesList(this)));
        alarmPendingIntent = PendingIntent.getBroadcast(MainActivity.this,
                PRIVATE_REQUEST_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmManager != null && alarmPendingIntent != null) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime, repeatInterval, alarmPendingIntent);
            Toast.makeText(MainActivity.this, "Alarm is On", Toast.LENGTH_SHORT).show();
            spnInerval.setEnabled(false);
        }
    }

    private void stopAlarm() {
        if (alarmManager != null && alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent);
            alarmPendingIntent.cancel();
            Toast.makeText(MainActivity.this, "Alarm is Off!", Toast.LENGTH_SHORT).show();
            spnInerval.setEnabled(true);
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

    public void btnImagesClicked(View view) {
        Intent intent = new Intent(this, PictureActivity.class);
        startActivity(intent);
    }

    //endregion

}