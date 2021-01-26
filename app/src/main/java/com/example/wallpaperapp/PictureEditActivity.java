package com.example.wallpaperapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class PictureEditActivity extends AppCompatActivity {
    private final String DEBUG_TAG = PictureEditActivity.class.getSimpleName();
    private final Float MIN_SIZE = 0.5F;
    private final Float MAX_SIZE = 2f;
    private ImageView imgView2Edit;
    private Matrix mMatrix = new Matrix();
    Float currentScale = 1.5f;
    Float currentX = 0f;
    Float currentY = 0f;
    ScaleGestureDetector mScaleGestureDetector;
    GestureDetector mGestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_edit);

        //Either call the removeStatusAndNavBar()
        // or add android:theme="@style/Theme.TranslucentBars" in the Manifest file

        //removeStatusAndNavBar();
        clearTitle();
        setUpImageAndGestureDetectors();
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
        int id = item.getItemId();

        switch (id) {
            case R.id.action_set_2:
                String message = String.format("x=%f, y=%f, scale=%f", currentX, currentY, currentScale);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                return true;
            default:
                // Do nothing
        }
        return super.onOptionsItemSelected(item);
    }

    // Add the bitmap to the ImageView and setup the needed TouchListener for the image
    private void setUpImageAndGestureDetectors() {
        imgView2Edit = findViewById(R.id.imgView2Edit);

        //Get the file path to the image
        String filePath = getIntent().getStringExtra("FILE_PATH");

        //Set image bitmap into the ImageView
        Glide.with(this)
                .load(filePath)
                .centerCrop()
                .into(imgView2Edit);

        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        mGestureDetector = new GestureDetector(this, new ScrollListener());
        imgView2Edit.setOnTouchListener(new ImageOnTouchListener());
        mMatrix.setScale(currentScale, currentScale);
        imgView2Edit.setImageMatrix(mMatrix);
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
            currentScale = currentScale * detector.getScaleFactor();
            currentScale = Math.max(MIN_SIZE, Math.min(currentScale, MAX_SIZE));
            mMatrix.setScale(currentScale, currentScale, detector.getFocusX(), detector.getFocusY());
            mMatrix.postTranslate(-currentX, -currentY);
            imgView2Edit.setImageMatrix(mMatrix);
            return true;
        }
    }

    private class ScrollListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Log.d(DEBUG_TAG,String.format("onScroll %f, %f", distanceX, distanceY));
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
//            int action = event.getActionMasked();
//            int x = Math.round(event.getRawX());
//            int y = Math.round(event.getRawY());
//            switch (action) {
//                case (MotionEvent.ACTION_DOWN):
//                    Log.d(DEBUG_TAG,"Action was DOWN");
//                    return true;
//                case (MotionEvent.ACTION_MOVE):
//                    Log.d(DEBUG_TAG, String.format("MOVE: %d, %d", x, y));
//                    return true;
//                case (MotionEvent.ACTION_UP):
//                    Log.d(DEBUG_TAG,"Action was UP");
//                    return true;
//                case (MotionEvent.ACTION_CANCEL):
//                    Log.d(DEBUG_TAG,"Action was CANCEL");
//                    return true;
//                case (MotionEvent.ACTION_OUTSIDE):
//                    Log.d(DEBUG_TAG,"Movement occurred outside bounds of current screen element");
//                    return true;
//                default:
//                    return true;
//            }
            return true;
        }
    }
    //endregion
}