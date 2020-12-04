package com.example.wallpaperapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "StoreImageExternal";
    private static final String DIR_NAME = "WallPaper";
    private static final int MY_REQEST_CODE = 1234;

    RecyclerView recviewImageList;
    OutputStream outputStream;
    ImagesRecViewAdapter recAdapter;
    ArrayList<String> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recviewImageList = findViewById(R.id.recviewImageList);
        recAdapter = new ImagesRecViewAdapter(this);
        recAdapter.setImages(getImageList());
        recviewImageList.setAdapter(recAdapter);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recviewImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        } else {
            recviewImageList.setLayoutManager(new LinearLayoutManager(this));
        }

        //recviewImageList.setLayoutManager(new GridLayoutManager(this, 2));
    }

    public void btnAddImageClicked(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1234);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    addImageFileToAppStorage(data.getData());
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addImageFileToAppStorage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            File dir = getAppSpecificPictureStorageDir();
            if (dir == null || !dir.exists()) {
                Toast.makeText(this, "Cannot find directory: " + dir.getName(), Toast.LENGTH_LONG).show();
                return;
            }

            //Create new instance of a file
            File file = new File(dir, System.currentTimeMillis() + ".jpg");
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            outputStream.flush();
            outputStream.close();
            recAdapter.setImages(getImageList());
            Toast.makeText(this, "Image Saved to " + dir.getName(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "Exception in Select Image!");
            e.printStackTrace();
        }
    }

    private ArrayList<ImageModel> getImageList() {
        ArrayList<ImageModel> imageList = new ArrayList<>();
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), DIR_NAME);
        if (dir.exists()) {
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
    private File getAppSpecificPictureStorageDir() {
        // Get the pictures directory that's inside the app-specific directory on external storage
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), DIR_NAME);
        if (file == null || !file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }

}