package com.example.wallpaperapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int PRIVATE_REQUEST_ID = 11;
    public static final String INTERVAL_HOURS = "interval_hours";
    public static final String ALARM_ON_OFF = "alarm_on_off";
    public static final String IMAGE_PATH_ARRAY = "images_path_array";
    public static final String IMAGE_INDEX = "images_index";
    public static final String LAST_ALARM = "last_alarm";
    public static final String SHARED_PREF_FILE_NAME = "com.example.wallpaperapp";

    private SharedPreferences sharedPreferences;

    private Context appContext;
    private Spinner spnInerval;
    private TextView txtIndex;
    private TextView txtTime;
    private TextView textLabel;
    private SwitchCompat btnStartStop;
    private int selectedIntervalHour;
    private int pictureIndex;
    private List<String> intervalsText;
    private List<Integer> intervalsNumber;
    private boolean isAlarmAlreadySet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context;
        appContext = this.getApplication();
        pictureIndex = 0;

        //Set internal variables for Views
        txtIndex = findViewById(R.id.txtIndex);
        txtTime = findViewById(R.id.txtTime);
        textLabel = findViewById(R.id.textLabel);
        spnInerval = findViewById(R.id.spnInerval);
        btnStartStop = findViewById(R.id.btnStartStop);

        //Set event handlers
        spnInerval.setOnItemSelectedListener(new spnInervalOnItemSelected());
        btnStartStop.setOnCheckedChangeListener(new btnStartStopChanged());

        //Get the shared preferences for reading app saved data
        sharedPreferences = getSharedPreferences(SHARED_PREF_FILE_NAME, MODE_PRIVATE);

        setSpinnerData();
        initializeData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SaveSharedData();
    }

    //Create shared object and save the needed data
    private void SaveSharedData() {
        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putInt(INTERVAL_HOURS, selectedIntervalHour);
        boolean isOn = btnStartStop.isChecked();
        preferencesEditor.putBoolean(ALARM_ON_OFF, isOn);
        preferencesEditor.apply();
        Log.d(TAG, "sharedPreferences.ALARM_ON_OFF is: " + isOn);
    }

    private void initializeData() {
        //Here we pass appContext instead of this just to have less memory leak application context is smaller than the activity context
        selectedIntervalHour = sharedPreferences.getInt(INTERVAL_HOURS, 1);
        boolean isOn = sharedPreferences.getBoolean(ALARM_ON_OFF, false);
        Log.d(TAG, "sharedPreferences.ALARM_ON_OFF is: " + isOn);
        Integer position = intervalsNumber.indexOf(selectedIntervalHour);
        spnInerval.setSelection(position);
        if (AlarmUtils.alarmIsSet(appContext)) {
            isAlarmAlreadySet = true;
            txtTime.setText(sharedPreferences.getString(LAST_ALARM, "Unknown"));
            pictureIndex = sharedPreferences.getInt(IMAGE_INDEX, 0);
            txtIndex.setText(String.valueOf(pictureIndex));
            spnInerval.setEnabled(false);
        } else {
            isAlarmAlreadySet = false;
            spnInerval.setEnabled(true);
        }
        btnStartStop.setChecked(isAlarmAlreadySet);
    }

    private void setSpinnerData() {
        intervalsText = Arrays.asList("1 hour", "2 hours", "6 hours", "12 hours", "day", "week");
        intervalsNumber = Arrays.asList(1, 2, 6, 12, 24, 24 * 7);

        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(
                appContext, R.layout.spinner_item, intervalsText);
//
//        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(
//                appContext, android.R.layout.simple_spinner_dropdown_item, intervalsText);
        //simple_spinner_item
        //simple_dropdown_item_1line
        //simple_selectable_list_item

        spnInerval.setAdapter(intervalAdapter);
    }

    //region Event Handlers

    //btnStartStopChanged event handler
    class btnStartStopChanged implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                spnInerval.setEnabled(false);
                if (!isAlarmAlreadySet) {
                    int selectedIntervalHour = sharedPreferences.getInt(INTERVAL_HOURS, 1);
                    AlarmUtils.startAlarm(appContext, selectedIntervalHour);
                }
            } else {
                spnInerval.setEnabled(true);
                AlarmUtils.stopAlarm(appContext);
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
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    public void btnImagesClicked(View view) {
        Intent intent = new Intent(appContext, PictureActivity.class);
        startActivity(intent);
    }

    //endregion

}