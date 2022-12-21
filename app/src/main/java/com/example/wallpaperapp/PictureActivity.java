package com.example.wallpaperapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import org.parceler.Parcels;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PictureActivity extends AppCompatActivity implements ImagesRecViewAdapter.OnPictureClickListener {

    private static final String LOG_TAG = PictureActivity.class.getSimpleName();
    private static final int ADD_IMAGE_REQUEST = 1;
    private static final int EDIT_IMAGE_REQUEST = 2;
    public static final String IMAGE_MODEL = "wallpaperapp.IMAGE_MODEL";
    public static final String DELETE_MESSAGE = "wallpaperapp.DELETE_MESSAGE";

    private ImagesRecViewAdapter recviewAdapter;
    private int lastPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        lastPosition = -1;
        RecyclerView recviewImageList = findViewById(R.id.recviewImageList);
        recviewAdapter = new ImagesRecViewAdapter(this, this);
        recviewAdapter.setImagesList(ImageUtils.getImagesList(this));
        recviewImageList.setAdapter(recviewAdapter);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recviewImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        } else {
            recviewImageList.setLayoutManager(new LinearLayoutManager(this));
            //recviewImageList.setLayoutManager(new GridLayoutManager(this, 2));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            //do something
            return true;
        } else if (id == R.id.action_info) {
            //do something
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void btnAddImageClicked(View view) {
        Intent intent = new Intent();
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        //intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true); //Don't show external storage
        intent.setAction(Intent.ACTION_PICK);
        //intent.setAction(Intent.ACTION_GET_CONTENT); //Use this to show Google Drive, Downloads and others
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), ADD_IMAGE_REQUEST);
    }

    //implemented ImagesRecViewAdapter.OnPictureClickListener
    @Override
    public void onPictureClick(int position) {
        Intent intent = new Intent(this, PictureEditActivity.class);
        ImageModel image = ImageUtils.getImagesList(this).get(position);
        lastPosition = position;
        intent.putExtra(IMAGE_MODEL, Parcels.wrap(image));
        startActivityForResult(intent, EDIT_IMAGE_REQUEST);
    }

    //implemented ImagesRecViewAdapter.OnPictureClickListener
    @Override
    public boolean onPictureLongClick(int position) {
        Toast.makeText(this, "Long clicked!", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_IMAGE_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        if(null != data.getClipData()) { // checking multiple selection or not
                            for(int i = 0; i < data.getClipData().getItemCount(); i++) {
                                Uri uri = data.getClipData().getItemAt(i).getUri();
                                addImageFileToAppStorage(uri);
                            }
                        } else {
                            Uri uri = data.getData();
                            addImageFileToAppStorage(uri);
                        }
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
                }
                break;
            case EDIT_IMAGE_REQUEST:
                //Toast.makeText(this, String.format("Form Edit - %d", resultCode) , Toast.LENGTH_SHORT).show();
                if (resultCode == RESULT_OK) {
                    String message = data.getStringExtra(DELETE_MESSAGE);
                    if (message != null && lastPosition >= 0) {
                        recviewAdapter.getImagesList().remove(lastPosition);
                        recviewAdapter.notifyItemRemoved(lastPosition);
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        lastPosition = -1;
                    }
                }
                break;
            default:
                // Do nothing
        }
    }

    private void addImageFileToAppStorage(Uri uri) {
        try {
            Bitmap bitmap = getCapturedImage(uri);

            File dir = ImageUtils.getAppSpecificPictureStorageDir(this);
            if (dir == null || !dir.exists()) {
                assert dir != null;
                Toast.makeText(this, "Cannot find directory: " + dir.getName(), Toast.LENGTH_LONG).show();
                return;
            }
            //Create new instance of a file
            File file = new File(dir, System.currentTimeMillis() + ".jpg");
            OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            outputStream.flush();
            outputStream.close();
            recviewAdapter.setImagesList(ImageUtils.getImagesList(this));
            Toast.makeText(this, "Image Saved to " + dir.getName(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "Exception in Select Image!");
            e.printStackTrace();
        }
    }

    private Bitmap getCapturedImage(Uri imageUri)  {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= 29) {
            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
            try {
                bitmap = ImageDecoder.decodeBitmap(source);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }


}