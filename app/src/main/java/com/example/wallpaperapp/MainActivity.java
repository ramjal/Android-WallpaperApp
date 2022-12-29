package com.example.wallpaperapp;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.parceler.Parcels;
import java.util.Arrays;
import java.util.List;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements ImagesRecViewAdapter.OnPictureClickListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int PRIVATE_REQUEST_ID = 11;
    public static final String INTERVAL_HOURS = "interval_hours";
    public static final String ALARM_ON_OFF = "alarm_on_off";
    public static final String IMAGE_PATH_ARRAY = "images_path_array";
    public static final String IMAGE_INDEX = "images_index";
    public static final String LAST_ALARM = "last_alarm";
    public static final String SHARED_PREF_FILE_NAME = "com.example.wallpaperapp";
    public static final String IMAGE_MODEL = "wallpaperapp.IMAGE_MODEL";
    public static final String DELETE_MESSAGE = "wallpaperapp.DELETE_MESSAGE";
    int startHour, startMinute;

    private SharedPreferences sharedPreferences;
    private Context appContext;
    private Spinner spnInerval;
    private TextView txtIndex;
    private TextView txtTime;
    private TextView txtStartTime;
    private SwitchCompat btnStartStop;
    private int selectedIntervalHour;
    private int pictureIndex;
    private List<Integer> intervalsNumber;
    private boolean isAlarmAlreadySet;
    private ImagesRecViewAdapter recviewAdapter;
    private int lastPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appContext = this.getApplication();
        pictureIndex = 0;

        //Set internal variables for Views
        txtIndex = findViewById(R.id.txtIndex);
        txtTime = findViewById(R.id.txtTime);
        txtStartTime = findViewById(R.id.txtStartTime);
        spnInerval = findViewById(R.id.spnInerval);
        btnStartStop = findViewById(R.id.btnStartStop);

        //Set event handlers
        spnInerval.setOnItemSelectedListener(new spnInervalOnItemSelected());
        btnStartStop.setOnCheckedChangeListener(new btnStartStopChanged());

        //Get the shared preferences for reading app saved data
        sharedPreferences = getSharedPreferences(SHARED_PREF_FILE_NAME, MODE_PRIVATE);

        lastPosition = -1;
        RecyclerView recviewImageList = findViewById(R.id.recyclerViewImageList);
        recviewAdapter = new ImagesRecViewAdapter(this, this);
        recviewAdapter.setImagesList(ImageUtils.getImagesList(this));
        recviewImageList.setAdapter(recviewAdapter);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recviewImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        } else {
            //recviewImageList.setLayoutManager(new LinearLayoutManager(this));
            recviewImageList.setLayoutManager(new GridLayoutManager(this, 2));
        }

        setSpinnerData();
        initializeData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_add_image) {
            handleAddImage();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        int position = intervalsNumber.indexOf(selectedIntervalHour);
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
        List<String> intervalsText = Arrays.asList("1 hour", "2 hours", "6 hours", "12 hours", "day", "week");
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


    public void startTimeClicked(View view) {
        //Toast.makeText(this, "Start Time Clicked! " , Toast.LENGTH_LONG).show();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                MainActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        //Initialize hour and minute
                        startHour = hourOfDay;
                        startMinute = minute;
                        //Initialize calendar
                        Calendar calendar = Calendar.getInstance();
                        //Set hour and mintute
                        calendar.set(0, 0, 0, startHour, startMinute);
                        //Set selected time on text view
                        txtStartTime.setText(DateFormat.format("hh:mm aa", calendar));
                    }
                }, 12, 0, false
        );
        //Display previous selected time
        timePickerDialog.updateTime(startHour, startMinute);
        //Show dialog
        timePickerDialog.show();
    }


    public void handleAddImage() {
        //Toast.makeText(appContext, "Add Image Click Not Implemented", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        //intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true); //Don't show external storage
        intent.setAction(Intent.ACTION_PICK);
        //intent.setAction(Intent.ACTION_GET_CONTENT); //Use this to show Google Drive, Downloads and others
        selectPictureResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    ActivityResultLauncher<Intent> selectPictureResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (result.getData() != null) {
                            if(null != result.getData().getClipData()) { // checking multiple selection or not
                                for(int i = 0; i < result.getData().getClipData().getItemCount(); i++) {
                                    Uri uri = result.getData().getClipData().getItemAt(i).getUri();
                                    ImageUtils.addImageFileToAppStorage(appContext, uri, recviewAdapter);
                                }
                            } else {
                                Uri uri = result.getData().getData();
                                ImageUtils.addImageFileToAppStorage(appContext, uri, recviewAdapter);
                            }
                        }
                    }
                }
            });


    //implemented ImagesRecViewAdapter.OnPictureClickListener
    @Override
    public void onPictureClick(int position) {
        Intent intent = new Intent(this, PictureEditActivity.class);
        ImageModel image = ImageUtils.getImagesList(this).get(position);
        lastPosition = position;
        intent.putExtra(IMAGE_MODEL, Parcels.wrap(image));
        pictureEditActivityResultLauncher.launch(intent);
    }


    @Override
    public boolean onPictureLongClick(int position) {
        Toast.makeText(appContext, "Long Click Not Implemented", Toast.LENGTH_SHORT).show();
        return true;
    }


    ActivityResultLauncher<Intent> pictureEditActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        String message = data.getStringExtra(DELETE_MESSAGE);
                        if (message != null && lastPosition >= 0) {
                            recviewAdapter.getImagesList().remove(lastPosition);
                            recviewAdapter.notifyItemRemoved(lastPosition);
                            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
                            lastPosition = -1;
                        }
                    }
                }
            });


}