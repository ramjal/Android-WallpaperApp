package com.example.wallpaperapp;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import static android.content.Context.MODE_PRIVATE;

public class ImageUtils {
    private static final String LOG_TAG = ImageUtils.class.getSimpleName();

    public static ArrayList<ImageModel> getImagesList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREF_FILE_NAME, MODE_PRIVATE);
        ArrayList<ImageModel> imageList = new ArrayList<>();
        File dir = getAppSpecificPictureStorageDir(context);
        if (dir != null && dir.exists()) {
            File[] allFiles = dir.listFiles();
            if (allFiles.length == 0) {
                Log.e(LOG_TAG, "No file found in " + dir.getAbsolutePath());
            } else {
                for (File file : allFiles) {
                    String rectStr = sharedPreferences.getString(file.getName(), null);
                    imageList.add(new ImageModel(file, file.getName(), rectStr));
                }
            }
        }
        return imageList;
    }

    static public String[] getImagesNameArray(ArrayList<ImageModel> imageList) {
        String array[] = new String[imageList.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = imageList.get(i).getFile().getAbsolutePath();
        }
        return array;
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

    public static boolean setWallPaper(String imagePath, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREF_FILE_NAME, MODE_PRIVATE);
        Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
        File imageFile = new File(imagePath);
        String rectStr = sharedPreferences.getString(imageFile.getName(), null);
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
        return setWallPaper(imageBitmap, context, imageRect);
    }

    public static boolean setWallPaper(Bitmap imageBitmap, Context context,  Rect visibleRect) {
        boolean bRet = false;
        try {
            WallpaperManager myWallpaperManager = WallpaperManager.getInstance(context);
            bRet = (myWallpaperManager.setBitmap(imageBitmap, visibleRect, false, FLAG_LOCK) > 0);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error: " + e.getMessage());
            e.printStackTrace();
        }
        return bRet;
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

    public static BitmapFactory.Options getImageOptions(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        return options;
    }

}
