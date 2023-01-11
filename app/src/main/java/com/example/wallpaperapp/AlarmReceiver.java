package com.example.wallpaperapp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = AlarmReceiver.class.getSimpleName();
    private String[] imagesPathList;

    private int pictureIndex;
    private SharedPreferences mPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        mPreferences = context.getSharedPreferences(MainActivity.SHARED_PREF_FILE_NAME, MODE_PRIVATE);

        if (mPreferences != null) {
            pictureIndex = mPreferences.getInt(MainActivity.IMAGE_INDEX, 0);
            imagesPathList = intent.getStringArrayExtra(MainActivity.IMAGE_PATH_ARRAY);
            setWallpaper(context);
        }
    }

    private void setWallpaper(Context context) {
        if (imagesPathList == null || imagesPathList.length == 0) {
            Log.e(LOG_TAG, "Image list is empty!");
            return;
        }

        //Get a random index of the image list
        //int index = new Random().nextInt(imagesNameList.length);

        try {
            if (pictureIndex >= imagesPathList.length) {
                pictureIndex = 0;
            }

            if (ImageUtils.setWallPaper(imagesPathList[pictureIndex], context)) {
                LogSuccessMessage();
                SaveNewPictureIndex();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void LogSuccessMessage() {
        String time = new SimpleDateFormat("HH:mm", Locale.CANADA).format(new Date());
        String message = String.format(Locale.CANADA, "Wallpaper set with image index=%d at %s", pictureIndex, time);
        Log.i(LOG_TAG, message);
    }

    private void SaveNewPictureIndex() {
        pictureIndex++;
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(MainActivity.IMAGE_INDEX, pictureIndex);
        String time = new SimpleDateFormat("HH:mm", Locale.CANADA).format(new Date());
        preferencesEditor.putString(MainActivity.LAST_ALARM, time);
        preferencesEditor.apply();
    }

}