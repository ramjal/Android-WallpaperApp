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
import java.util.Locale;

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
        long intervalMillis = selectedIntervalHour * 60 * 60 * 1000;
        int startTimeMilli = getStartTimeInMillis(startHour, startMinute);
        long triggerAtMillis = SystemClock.elapsedRealtime() + startTimeMilli;

        String message = String.format("StartHour = %d - StartMinute = %d - startTimeMilli = %d", startHour, startMinute, startTimeMilli);
        Log.d(TAG, message);

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra(IMAGE_PATH_ARRAY, ImageUtils.getImagesNameArray(ImageUtils.getImagesList(context)));
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context,
                PRIVATE_REQUEST_ID, alarmIntent, FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null && alarmPendingIntent != null) {
            alarmManager.setInexactRepeating(ELAPSED_REALTIME_WAKEUP, triggerAtMillis, intervalMillis, alarmPendingIntent);
            Log.d(TAG, "Started the Wallpaper alarm");
            Toast.makeText(context, "Wallpaper is On!", Toast.LENGTH_SHORT).show();
        }
    }

    private static int getStartTimeInMillis(int startHour, int startMinute) {
        Calendar cal1 = Calendar.getInstance(); // Now
        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.HOUR_OF_DAY, startHour);
        cal2.set(Calendar.MINUTE, startMinute);

        long diff = cal1.getTimeInMillis() - cal2.getTimeInMillis();
        int millis = (int) Math.abs(diff);

        if (diff > 0)
        {
            millis = (24 * 60 * 60 * 1000) - millis;
            Log.d(TAG, "Tomorrow in " + millis + " milliseconds ");
        } else {
            Log.d(TAG, "Today in " + millis + " milliseconds ");
        }

        return millis;
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
