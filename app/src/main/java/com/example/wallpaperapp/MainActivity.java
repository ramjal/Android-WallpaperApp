package com.example.wallpaperapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int PRIVATE_REQUEST_ID = 11;
    private static final String INTERVAL_HOURS_KEY = "interval_hours";
    public static final String IMAGE_PATH_ARRAY = "images_path_array";
    public static final String IMAGE_INDEX = "images_index";
    public static final String IMAGES_RECT_SET = "images_rect_set";

    private SharedPreferences sharedPreferences;
    static public final String SHARED_PREF_FILE_NAME = "com.example.wallpaperapp";

    private Context appContext;
    private Spinner spnInerval;
    private TextView txtIndex;
    private TextView textLabel;
    private SwitchCompat btnStartStop;
    private AlarmManager alarmManager;
    private Intent alarmIntent;
    private PendingIntent alarmPendingIntent;
    //private HashMap<String, Integer> intervalMap;
    private int selectedIntervalHour;
    private int pictureIndex;
    private List<String> intervalsText;
    private List<Integer> intervalsNumber;
    private boolean isAlarmAlreadySet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appContext = this.getApplication();
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
        sharedPreferences = getSharedPreferences(SHARED_PREF_FILE_NAME, MODE_PRIVATE);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //Here we pass appContext instead of this just to have less memory leak application context is smaller than the activity context
        alarmIntent = new Intent(appContext, AlarmReceiver.class);
        setSpinnerData();
        checkToggleButton();
    }

    @Override
    protected void onPause() {
        super.onPause();

        SaveSharedData();
    }

    //Create shared object and save the needed data
    private void SaveSharedData() {
        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putInt(INTERVAL_HOURS_KEY, selectedIntervalHour);
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

        alarmPendingIntent = PendingIntent.getBroadcast(appContext,
                PRIVATE_REQUEST_ID, alarmIntent, PendingIntent.FLAG_NO_CREATE);

        if (alarmPendingIntent != null) {
            isAlarmAlreadySet = true;
            selectedIntervalHour = sharedPreferences.getInt(INTERVAL_HOURS_KEY, 1);
            pictureIndex = sharedPreferences.getInt(IMAGE_INDEX, 0);
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
                                    appContext, R.layout.spinner_item, intervalsText);
//
//        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(
//                appContext, android.R.layout.simple_spinner_dropdown_item, intervalsText);
                                             //simple_spinner_item
                                             //simple_dropdown_item_1line
                                             //simple_selectable_list_item

        spnInerval.setAdapter(intervalAdapter);
        spnInerval.setSelection(4);
    }

    private void startAlarm() {
        if (isAlarmAlreadySet) return;
        long repeatInterval = 30000;
        //long repeatInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        //long repeatInterval = selectedIntervalHour * 3600 * 1000;
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
        alarmIntent.putExtra(IMAGE_PATH_ARRAY, getImagesNameArray(ImageUtils.getImagesList(appContext)));
        alarmPendingIntent = PendingIntent.getBroadcast(appContext,
                PRIVATE_REQUEST_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmManager != null && alarmPendingIntent != null) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime, repeatInterval, alarmPendingIntent);
            Toast.makeText(appContext, "Alarm is On", Toast.LENGTH_SHORT).show();
            spnInerval.setEnabled(false);
        }
    }

    private void stopAlarm() {
        if (alarmManager != null && alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent);
            alarmPendingIntent.cancel();
            Toast.makeText(appContext, "Alarm is Off!", Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(appContext, PictureActivity.class);
        startActivity(intent);
    }

    //endregion

}