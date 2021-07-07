package com.example.wallpaperapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class RestartAlarmsReceiver extends BroadcastReceiver {
    private static String TAG = RestartAlarmsReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "inside RestartAlarmsReceiver.onReceive");

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            //AlarmUtils.startAlarm(context);
            startAlarm(context);
            Log.d(TAG, "Started the alarm");
        } else {
            Log.d(TAG, "Received unexpected intent " + intent.toString());
        }
    }

    private static void startAlarm(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREF_FILE_NAME, MODE_PRIVATE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        int selectedIntervalHour = sharedPreferences.getInt(MainActivity.INTERVAL_HOURS_KEY, 1);
        long repeatInterval = selectedIntervalHour * 3600 * 1000;
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra(MainActivity.IMAGE_PATH_ARRAY, ImageUtils.getImagesNameArray(ImageUtils.getImagesList(context)));
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context,
                MainActivity.PRIVATE_REQUEST_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmManager != null && alarmPendingIntent != null) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime, repeatInterval, alarmPendingIntent);
            //Toast.makeText(context, "WallAlarm is On", Toast.LENGTH_SHORT).show();
        }
    }

}