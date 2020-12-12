package com.example.wallpaperapp;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static android.app.WallpaperManager.FLAG_LOCK;

public class AlarmReceiver extends BroadcastReceiver {
    String[] imagesNameList;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {
        imagesNameList = intent.getStringArrayExtra("ImagesNameArray");
        setWallpaper(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setWallpaper(Context context) {
        if (imagesNameList == null || imagesNameList.length == 0) {
            Log.e(MainActivity.LOG_TAG, "Image list is empty!");
            return;
        }
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        //Get a random index of the image list
        int index = new Random().nextInt(imagesNameList.length);
        String message = String.format("Wallpaper set with image index=%d at %s", index, time);
        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(context);
        try {
            Bitmap imgBitmap = BitmapFactory.decodeFile(imagesNameList[index]);
            if (myWallpaperManager.setBitmap(imgBitmap, null, false, FLAG_LOCK) > 0) {
                Log.i(MainActivity.LOG_TAG, message);
            }
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, e.getMessage());
            e.printStackTrace();
        }
    }
}