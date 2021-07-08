package com.example.wallpaperapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import static android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.example.wallpaperapp.MainActivity.ALARM_ON_OFF;
import static com.example.wallpaperapp.MainActivity.IMAGE_PATH_ARRAY;
import static com.example.wallpaperapp.MainActivity.INTERVAL_HOURS;
import static com.example.wallpaperapp.MainActivity.PRIVATE_REQUEST_ID;

public class RestartAlarmsReceiver extends BroadcastReceiver {
    private static String TAG = RestartAlarmsReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            //AlarmUtils.startAlarm(context);
            startAlarm(context);
        } else {
            Log.d(TAG, "Received unexpected intent " + intent.toString());
        }
    }

    private static void startAlarm(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREF_FILE_NAME, MODE_PRIVATE);
        boolean isOn = sharedPreferences.getBoolean(ALARM_ON_OFF, false);
        Log.d(TAG, "sharedPreferences.ALARM_ON_OFF is: " + isOn);
        if (isOn) {
            if (sharedPreferences == null) return;
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            int selectedIntervalHour = sharedPreferences.getInt(INTERVAL_HOURS, 1);
            long repeatInterval = selectedIntervalHour * 3600 * 1000;
            long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            alarmIntent.putExtra(IMAGE_PATH_ARRAY, ImageUtils.getImagesNameArray(ImageUtils.getImagesList(context)));
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context,
                    PRIVATE_REQUEST_ID, alarmIntent, FLAG_UPDATE_CURRENT);
            if (alarmManager != null && alarmPendingIntent != null) {
                alarmManager.setInexactRepeating(ELAPSED_REALTIME_WAKEUP,
                        triggerTime, repeatInterval, alarmPendingIntent);
                Log.d(TAG, "Started Wallpaper Automation");
            }
        }
    }

}