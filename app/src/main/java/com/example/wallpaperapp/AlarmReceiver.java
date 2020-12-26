package com.example.wallpaperapp;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static android.app.WallpaperManager.FLAG_LOCK;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = AlarmReceiver.class.getSimpleName();
    private String[] imagesNameList;

    private static final String SHARED_PREF_FILE_NAME = "com.example.wallpaperapp";
    private int pictureIndex;
    private SharedPreferences mPreferences;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {
        mPreferences = context.getSharedPreferences(SHARED_PREF_FILE_NAME, context.MODE_PRIVATE);

        if (mPreferences != null) {
            pictureIndex = mPreferences.getInt(MainActivity.IMAGE_INDEX, 0);
            imagesNameList = intent.getStringArrayExtra(MainActivity.IMAGE_ARRAY_KEY);
            setWallpaper(context);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setWallpaper(Context context) {
        if (imagesNameList == null || imagesNameList.length == 0) {
            Log.e(LOG_TAG, "Image list is empty!");
            return;
        }

        String time = new SimpleDateFormat("HH:mm").format(new Date());
        //Get a random index of the image list
        //int index = new Random().nextInt(imagesNameList.length);

        String message = String.format("Wallpaper set with image index=%d at %s", pictureIndex, time);
        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(context);
        try {
            if (pictureIndex >= imagesNameList.length) {
                pictureIndex = 0;
            }
            Bitmap imgBitmap = BitmapFactory.decodeFile(imagesNameList[pictureIndex]);
            if (myWallpaperManager.setBitmap(imgBitmap, null, false, FLAG_LOCK) > 0) {
                Log.i(LOG_TAG, message);
                pictureIndex++;
                SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                preferencesEditor.putInt(MainActivity.IMAGE_INDEX, pictureIndex);
                preferencesEditor.apply();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }
    }

}