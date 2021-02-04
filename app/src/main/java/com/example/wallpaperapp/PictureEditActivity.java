package com.example.wallpaperapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.parceler.Parcels;

import java.io.File;


public class PictureEditActivity extends AppCompatActivity {
    private final String DEBUG_TAG = PictureEditActivity.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    private final Float MIN_SIZE = 0.5F;
    private final Float MAX_SIZE = 4f;
    private ImageView imgView2Edit;
    private Matrix mMatrix = new Matrix();
    private String filePath;
    private ImageModel imageModel;
    private Float currentScale = 1f;
    private Float currentX = 0f;
    private Float currentY = 0f;
    private float scale = 1f;
    private RectF rectF;
    private int mDisplayWidth;
    private int mDisplayHeight;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_edit);

        sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREF_FILE_NAME, MODE_PRIVATE);

        //Either call the removeStatusAndNavBar()
        // or add android:theme="@style/Theme.TranslucentBars" in the Manifest file

        setupVariables();
        //removeStatusAndNavBar();
        clearTitle();
        setUpImageAndGestureDetectors();
    }

    private void setupVariables() {
        Point size = new Point();
        getDisplay().getRealSize(size);
        mDisplayWidth = size.x;
        mDisplayHeight = size.y;
        imgView2Edit = findViewById(R.id.imgView2Edit);
        //Get the file path to the image
        //filePath = getIntent().getStringExtra(PictureActivity.FILE_PATH);
        //fileName = getIntent().getStringExtra(PictureActivity.FILE_NAME);

        imageModel = (ImageModel) Parcels.unwrap(getIntent().getParcelableExtra(PictureActivity.IMAGE_MODEL));
        filePath = imageModel.getFile().getAbsolutePath();
    }

    // Add the bitmap to the ImageView and setup the needed TouchListener for the image
    private void setUpImageAndGestureDetectors() {
        //Set image bitmap into the ImageView
        Glide.with(this)
                .load(filePath)
                .into(imgView2Edit);

        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        //mScaleGestureDetector.setQuickScaleEnabled(false);
        mGestureDetector = new GestureDetector(this, new ScrollListener());
        imgView2Edit.setOnTouchListener(new ImageOnTouchListener());
        mMatrix.setScale(currentScale, currentScale);
        mMatrix.postTranslate(-currentX, -currentY);
        imgView2Edit.setImageMatrix(mMatrix);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_2, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        String message;

        Rect rect = new Rect(0,0,0,0);
        rectF = ImageUtils.getImageBounds(imgView2Edit);
        rectF.round(rect);

        BitmapFactory.Options options = ImageUtils.getImageOptions(filePath);

        scale = (rectF.right - rectF.left) / options.outWidth;

        int id = item.getItemId();
        switch (id) {
            case R.id.action_set_2:
//                message = String.format("x=%f, y=%f, scale=%f", currentX, currentY, currentScale);
//                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                setWallPaper();
                return true;
            case R.id.action_info_2:
                message = String.format("currentX=%f, currentY=%f, currentScale%f\nleft=%d, top=%d\nright=%d, bottom=%d\nwidth=%d, height=%d\nscale=%f",
                        currentX, currentY, currentScale, rect.left, rect.top, rect.right, rect.bottom, options.outWidth, options.outHeight, scale);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_delete_2:
                handleDelete();
            default:
                // Do nothing
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleDelete() {
        //Toast.makeText(mainContext, imagesList.get(position).getName() + " Long Pressed!", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder myAlterDialog = new AlertDialog.Builder(this);
        myAlterDialog.setTitle("Delete Image");
        myAlterDialog.setMessage("Are you sure you want to delete this image?");

        // Add the dialog buttons.
        myAlterDialog.setPositiveButton("Yes", new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            imageModel.file.delete();
                            Intent replyIntent = new Intent();
                            replyIntent.putExtra(PictureActivity.MESSAGE_REPLY, imageModel.getName() + " is now deleted!");
                            setResult(RESULT_OK, replyIntent);
                            finish();
                            //Toast.makeText(PictureEditActivity.this, imageModel.getName() + " is now deleted!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(PictureEditActivity.this, "!!!Error - Cannot Delete " + imageModel.getName(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        myAlterDialog.setNegativeButton("No", new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(PictureEditActivity.this, imageModel.getName() + " - Pressed Cancel", Toast.LENGTH_SHORT).show();
                    }
                });

        // Create and show the AlertDialog.
        myAlterDialog.show();
    }

    private void setWallPaper() {
        Bitmap imageBitmap = BitmapFactory.decodeFile(filePath);

        Matrix matrix = imgView2Edit.getImageMatrix();

        int imageWidth = imageBitmap.getWidth();
        int imageHeight = imageBitmap.getHeight();

        int left = Math.max(Math.round(-rectF.left / scale), 0);
        int top = Math.max(Math.round(-rectF.top / scale), 0);

        int right = left + Math.round(mDisplayWidth / scale);
        int bottom = top + Math.round(mDisplayHeight / scale);

//        left = 400;
//        top = 0;
//        right = 600;
//        bottom = 450;

        Rect visibleRect = new Rect(left, top, right, bottom);
        //visibleRect = null;

//        String message = String.format("displayWidth = %d, displayHeight = %d" +
//                                        "\nimageWidth = %d, imageHeight = %d" +
//                                        "\nleft = %d, top = %d, " +
//                                        "\nright = %d, bottom = %d" +
//                                        "\nscale = %f, currentScale = %f",
//                                        mDisplayWidth, mDisplayHeight,
//                                        imageWidth, imageHeight,
//                                        left, top, right, bottom,
//                                        scale, currentScale);
//        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        ImageUtils.setWallPaper(imageBitmap, this, visibleRect);

        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
        String rectStr = String.format("%d,%d,%d,%d", visibleRect.left, visibleRect.top, visibleRect.right, visibleRect.bottom);
        preferencesEditor.putString(imageModel.getName(), rectStr);
        preferencesEditor.apply();
        Toast.makeText(this, "Wallpaper is set!", Toast.LENGTH_LONG).show();
    }

    //Clean up extra bars from the top and the buttom
    private void removeStatusAndNavBar() {
        getSupportActionBar().hide(); //hide the title bar

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );
        }
    }


    //remove the Title from actoin bar
    private void clearTitle() {
        getSupportActionBar().setTitle("");
    }

    //region Inner Classes
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float newFactor = detector.getScaleFactor();
            setCurrentXY();
            if (currentScale * newFactor < 1) {
                newFactor = 1;
            }
            currentScale = currentScale * newFactor;
            currentScale = Math.max(MIN_SIZE, Math.min(currentScale, MAX_SIZE));
            mMatrix.setScale(currentScale, currentScale, detector.getFocusX(), detector.getFocusY());
            mMatrix.postTranslate(-currentX, -currentY);
            imgView2Edit.setImageMatrix(mMatrix);
            return true;
        }

        private void setCurrentXY() {
            rectF = ImageUtils.getImageBounds(imgView2Edit);

            if (rectF.top > 0) {
                currentY += (rectF.top * currentScale);
            }

            if (rectF.left > 0) {
                currentX += (rectF.left * currentScale);
            }

            if (rectF.right < mDisplayWidth) {
                currentX -= ((mDisplayWidth - rectF.right) * currentScale);
            }

            if (rectF.bottom < mDisplayHeight) {
                currentY -= ((mDisplayHeight - rectF.bottom) * currentScale);
            }
        }
    }

    private class ScrollListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            rectF = ImageUtils.getImageBounds(imgView2Edit);

            if (rectF.left > distanceX || rectF.right - distanceX < mDisplayWidth) {
                distanceX = 0;
            }
            if (rectF.top > distanceY || rectF.bottom - distanceY < mDisplayHeight) {
                distanceY = 0;
            }

            currentX += distanceX;
            currentY += distanceY;
            mMatrix.postTranslate(-distanceX, -distanceY);
            imgView2Edit.setImageMatrix(mMatrix);

            return true;
        }

    }

    private class ImageOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mScaleGestureDetector.onTouchEvent(event);
            mGestureDetector.onTouchEvent(event);
            return true;
        }
    }
    //endregion
}