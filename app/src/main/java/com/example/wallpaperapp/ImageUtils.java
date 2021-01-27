package com.example.wallpaperapp;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.app.WallpaperManager.FLAG_LOCK;

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

    public static void setWallPaper(Bitmap imageBitmap, Context context,  Rect visibleRect) {
        try {
            WallpaperManager myWallpaperManager = WallpaperManager.getInstance(context);
            if (myWallpaperManager.setBitmap(imageBitmap, visibleRect, false, FLAG_LOCK) > 0) {
                //Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * Helper method to get the bounds of image inside the imageView.
     *
     * @param imageView the imageView.
     * @return bounding rectangle of the image.
     */
    public static RectF getImageBounds(ImageView imageView) {
        RectF bounds = new RectF();
        Drawable drawable = imageView.getDrawable();
        if (drawable != null) {
            imageView.getImageMatrix().mapRect(bounds, new RectF(drawable.getBounds()));
        }
        return bounds;
    }

}
