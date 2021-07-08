package com.example.wallpaperapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;
import static com.example.wallpaperapp.MainActivity.ALARM_ON_OFF;
import static com.example.wallpaperapp.MainActivity.INTERVAL_HOURS;

public class RestartAlarmsReceiver extends BroadcastReceiver {
    private static String TAG = RestartAlarmsReceiver.class.getSimpleName();
    private static SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREF_FILE_NAME, MODE_PRIVATE);
            if (sharedPreferences == null) return;
            boolean isOn = sharedPreferences.getBoolean(ALARM_ON_OFF, false);
            int selectedIntervalHour = sharedPreferences.getInt(INTERVAL_HOURS, 24);
            Log.d(TAG, "Just booted - sharedPreferences.ALARM_ON_OFF is: " + isOn);
            if (isOn) {
                AlarmUtils.startAlarm(context, selectedIntervalHour);
            }
        } else {
            Log.d(TAG, "Received unexpected intent " + intent.toString());
        }
    }
}