package com.example.wallpaperapp;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;

public class ImageUtils {
    private static final String LOG_TAG = ImageUtils.class.getSimpleName();

    public static ArrayList<ImageModel> getImagesList(Context context) {
        ArrayList<ImageModel> imageList = new ArrayList<>();
        File dir = getAppSpecificPictureStorageDir(context);
        if (dir != null && dir.exists()) {
            File[] allFiles = dir.listFiles();
            if (allFiles.length == 0) {
                Log.e(LOG_TAG, "No file found in " + dir.getAbsolutePath());
            } else {
                for (File file : allFiles) {
                    imageList.add(new ImageModel(file, file.getName()));
                }
            }
        }
        return imageList;
    }

    @Nullable
    public static File getAppSpecificPictureStorageDir(Context context) {
        //Get the pictures directory that's inside the app-specific directory on external storage
        String appImagesPath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

        if(appImagesPath == null || appImagesPath.trim().isEmpty()) {
            Log.e(LOG_TAG, "appImagesPath is null or empty");
            return null;
        }

        File dir = new File(appImagesPath);
        if (dir != null && dir.exists()) {
            Log.i(LOG_TAG, String.format("Directory already exists - %s", appImagesPath));
        } else if (!dir.mkdirs()) {
            Log.e(LOG_TAG, String.format("File.mkdirs() returns false - %s", appImagesPath));
        } else {
            Log.i(LOG_TAG, String.format("New directory created - %s", appImagesPath));
        }
        return dir;
    }

}
