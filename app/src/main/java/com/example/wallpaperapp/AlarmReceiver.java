package com.example.wallpaperapp;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.WallpaperManager.FLAG_LOCK;
import static android.content.Context.MODE_PRIVATE;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = AlarmReceiver.class.getSimpleName();
    private String[] imagesPathList;

    private static final String SHARED_PREF_FILE_NAME = "com.example.wallpaperapp";
    private int pictureIndex;
    private SharedPreferences mPreferences;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {
        mPreferences = context.getSharedPreferences(SHARED_PREF_FILE_NAME, MODE_PRIVATE);

        if (mPreferences != null) {
            pictureIndex = mPreferences.getInt(MainActivity.IMAGE_INDEX, 0);
            imagesPathList = intent.getStringArrayExtra(MainActivity.IMAGE_PATH_ARRAY);
            setWallpaper(context);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setWallpaper(Context context) {
        if (imagesPathList == null || imagesPathList.length == 0) {
            Log.e(LOG_TAG, "Image list is empty!");
            return;
        }

        String time = new SimpleDateFormat("HH:mm").format(new Date());
        //Get a random index of the image list
        //int index = new Random().nextInt(imagesNameList.length);

        String message = String.format("Wallpaper set with image index=%d at %s", pictureIndex, time);
        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(context);
        try {
            if (pictureIndex >= imagesPathList.length) {
                pictureIndex = 0;
            }
            String imagePath = imagesPathList[pictureIndex];
            File imageFile = new File(imagePath);
            String rectStr = mPreferences.getString(imageFile.getName(), null);
            Rect imageRect = null;
            if (rectStr != null) {
                String[] arrayRect = rectStr.split(",");
                if (arrayRect.length == 4) {
                    imageRect = new Rect(Integer.parseInt(arrayRect[0]),
                                            Integer.parseInt(arrayRect[1]),
                                            Integer.parseInt(arrayRect[2]),
                                            Integer.parseInt(arrayRect[3]));
                }
            }

            Bitmap imgBitmap = BitmapFactory.decodeFile(imagePath);
            if (ImageUtils.setWallPaper(imgBitmap, context, imageRect)) {
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