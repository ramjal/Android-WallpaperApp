package com.example.wallpaperapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import static android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP;
import static android.content.Context.MODE_PRIVATE;
import static com.example.wallpaperapp.MainActivity.INTERVAL_HOURS_KEY;
import static com.example.wallpaperapp.MainActivity.PRIVATE_REQUEST_ID;
import static com.example.wallpaperapp.MainActivity.SHARED_PREF_FILE_NAME;

public class AlarmUtils {
    private static final String TAG = AlarmUtils.class.getSimpleName();

    public static boolean alarmIsSet(Context context) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, PRIVATE_REQUEST_ID,
                alarmIntent, PendingIntent.FLAG_NO_CREATE);
        return (alarmPendingIntent != null);
    }

    public static void startAlarm(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_FILE_NAME, MODE_PRIVATE);
        int selectedIntervalHour = sharedPreferences.getInt(INTERVAL_HOURS_KEY, 1);
        long repeatInterval = selectedIntervalHour * 3600 * 1000;
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra(MainActivity.IMAGE_PATH_ARRAY,
                ImageUtils.getImagesNameArray(ImageUtils.getImagesList(context)));
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context,
                PRIVATE_REQUEST_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmManager != null && alarmPendingIntent != null) {
            alarmManager.setInexactRepeating(ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval,
                    alarmPendingIntent);
            Toast.makeText(context, "Alarm is On", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Started the alarm");
        }
    }

    public static void stopAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, PRIVATE_REQUEST_ID,
                alarmIntent, PendingIntent.FLAG_NO_CREATE);
        if (alarmManager != null && alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent);
            alarmPendingIntent.cancel();
            Toast.makeText(context, "Alarm is Off!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Stopped the alarm");
        }
    }

}
