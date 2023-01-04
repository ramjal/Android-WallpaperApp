package com.example.wallpaperapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import static android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.example.wallpaperapp.MainActivity.IMAGE_PATH_ARRAY;
import static com.example.wallpaperapp.MainActivity.PRIVATE_REQUEST_ID;

import java.util.Calendar;

public class AlarmUtils {
    private static final String TAG = AlarmUtils.class.getSimpleName();

    public static boolean alarmIsSet(Context context) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, PRIVATE_REQUEST_ID,
                alarmIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        return (alarmPendingIntent != null);
    }

    public static void startAlarm(Context context, int selectedIntervalHour, int startHour, int startMinute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        long repeatInterval = selectedIntervalHour * 3600 * 1000;
        repeatInterval =  60 * 1000;
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra(IMAGE_PATH_ARRAY,
                ImageUtils.getImagesNameArray(ImageUtils.getImagesList(context)));
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context,
                PRIVATE_REQUEST_ID, alarmIntent, FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null && alarmPendingIntent != null) {
            alarmManager.setInexactRepeating(ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval,
                    alarmPendingIntent);
            Log.d(TAG, "Started the Wallpaper alarm");
            Toast.makeText(context, "Wallpaper is On!", Toast.LENGTH_SHORT).show();
        }

        Calendar cal1 = Calendar.getInstance();

        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.HOUR, startHour);
        cal2.set(Calendar.MINUTE, startMinute);

        int ret = cal1.compareTo(cal2);

        long cal1Milli = cal1.getTimeInMillis();
        long cal2Milli = cal2.getTimeInMillis();

        long dif = cal1Milli - cal2Milli;

    }

    public static void stopAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, PRIVATE_REQUEST_ID,
                alarmIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null && alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent);
            alarmPendingIntent.cancel();
            Log.d(TAG, "Stopped the Wallpaper alarm");
            Toast.makeText(context, "Wallpaper is Off!", Toast.LENGTH_SHORT).show();
        }
    }

}
