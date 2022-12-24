package com.example.wallpaperapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.parceler.Parcels;

import java.util.Locale;
import java.util.Objects;


public class PictureEditActivity extends AppCompatActivity {
    private final String DEBUG_TAG = PictureEditActivity.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    private ImageView imgView2Edit;
    private Matrix matrix;
    private String filePath;
    private ImageModel imageModel;
    private Float currentScale = 1f;
    private Float currentX = 0f;
    private Float currentY = 0f;
    private float scale = 1f;
    private RectF rectF;
    private int displayWidth;
    private int displayHeight;
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
        Point size = ImageUtils.GetDisplaySize(this);
        displayWidth = size.x;
        displayHeight = size.y;

        imgView2Edit = findViewById(R.id.imgView2Edit);
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

        matrix = createImageMatix(imgView2Edit, imageModel);
        //matrix.setScale(currentScale, currentScale);
        //matrix.postTranslate(-currentX, -currentY);
        imgView2Edit.setImageMatrix(matrix);
    }


    private Matrix createImageMatix(ImageView imageView, ImageModel image) {
        Matrix matrix = null;

        String imagePath = image.getFile().getAbsolutePath();
        //image.rectStr = sharedPreferences.getString(image.getName(), null);
        if (image.rectStr != null) {
            BitmapFactory.Options options = ImageUtils.getImageOptions(imagePath);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int imageViewWidth = displayWidth;//displayMetrics.widthPixels;
            int imageViewHeight = displayHeight;//displayMetrics.heightPixels;
            float aspectImageView = (float) imageViewWidth / imageViewHeight;
            float aspectPicture = (float) options.outWidth / options.outHeight;
            float scale = 1f;

            if (aspectImageView > aspectPicture) {
                scale = (float) imageViewWidth / options.outWidth;
            } else {
                scale = (float) imageViewHeight / options.outHeight;
            }

            String[] arrayRect = image.rectStr.split(",");
            if (arrayRect.length == 4) {
                //RectF imageRect = new RectF(274, 318, 419, 624);
                RectF imageRect = new RectF(Integer.parseInt(arrayRect[0]) * scale,
                        Integer.parseInt(arrayRect[1]) * scale,
                        Integer.parseInt(arrayRect[2]) * scale,
                        Integer.parseInt(arrayRect[3]) * scale);
                RectF viewRect = new RectF(0, 0, imageViewWidth, imageViewHeight);
                matrix = new Matrix();
                boolean result = matrix.setRectToRect(imageRect, viewRect, Matrix.ScaleToFit.CENTER);
            }
        }
        if (matrix == null) {
            matrix = new Matrix();
        }
        return matrix;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_2, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        rectF = ImageUtils.getImageBounds(imgView2Edit);
        BitmapFactory.Options options = ImageUtils.getImageOptions(filePath);
        scale = (rectF.right - rectF.left) / options.outWidth;

        int id = item.getItemId();
        if (id == R.id.action_set_image) {
            setWallPaper();
            return true;
        } else if (id == R.id.action_image_info) {
            displayImageInfo(options);
            return true;
        } else if (id == R.id.action_delete_image) {
            handleDelete();
        }
        return super.onOptionsItemSelected(item);
    }


    private void displayImageInfo(BitmapFactory.Options options) {
        Rect rect = new Rect(0,0,0,0);
        rectF.round(rect);
        String message = String.format(Locale.CANADA, "file=%s\nleft=%d, top=%d\nright=%d, bottom=%d\nwidth=%d, height=%d\nscale=%f",
                imageModel.getName(), rect.left, rect.top, rect.right, rect.bottom, options.outWidth, options.outHeight, scale);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    private void setWallPaper() {
        Bitmap imageBitmap = BitmapFactory.decodeFile(filePath);

        Matrix matrix = imgView2Edit.getImageMatrix();

        int imageWidth = imageBitmap.getWidth();
        int imageHeight = imageBitmap.getHeight();

        int left = Math.max(Math.round(-rectF.left / scale), 0);
        int top = Math.max(Math.round(-rectF.top / scale), 0);

        int right = left + Math.round(displayWidth / scale);
        int bottom = top + Math.round(displayHeight / scale);

        Rect visibleRect = new Rect(left, top, right, bottom);
        //ImageUtils.setWallPaper(imageBitmap, this, visibleRect);

        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
        String rectStr = String.format(Locale.CANADA, "%d,%d,%d,%d", visibleRect.left, visibleRect.top, visibleRect.right, visibleRect.bottom);
        preferencesEditor.putString(imageModel.getName(), rectStr);
        preferencesEditor.apply();
        Toast.makeText(this, "Image rect is set!", Toast.LENGTH_LONG).show();
    }


    private void handleDelete() {
        //Toast.makeText(mainContext, imagesList.get(position).getName() + " Long Pressed!", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder myAlterDialog = new AlertDialog.Builder(this);
        myAlterDialog.setTitle("Delete Image");
        myAlterDialog.setMessage("Are you sure you want to delete this image?");

        // Add the dialog buttons.
        myAlterDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        if (imageModel.file.delete()) {
                            Intent replyIntent = new Intent();
                            replyIntent.putExtra(PictureActivity.DELETE_MESSAGE, imageModel.getName() + " is now deleted!");
                            setResult(RESULT_OK, replyIntent);
                            finish();
                            //Toast.makeText(PictureEditActivity.this, imageModel.getName() + " is now deleted!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(PictureEditActivity.this, "!!!Error - " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        myAlterDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(PictureEditActivity.this, imageModel.getName() + " - Pressed Cancel", Toast.LENGTH_SHORT).show();
                }
            });

        // Create and show the AlertDialog.
        myAlterDialog.show();
    }


    //Clean up extra bars from the top and the buttom
    private void removeStatusAndNavBar() {
        Objects.requireNonNull(getSupportActionBar()).hide(); //hide the title bar

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }


    //remove the Title from actoin bar
    private void clearTitle() {
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
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
            float MIN_SIZE = 0.5F;
            float MAX_SIZE = 4f;
            currentScale = Math.max(MIN_SIZE, Math.min(currentScale, MAX_SIZE));
            matrix.setScale(currentScale, currentScale, detector.getFocusX(), detector.getFocusY());
            matrix.postTranslate(-currentX, -currentY);
            imgView2Edit.setImageMatrix(matrix);
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

            if (rectF.right < displayWidth) {
                currentX -= ((displayWidth - rectF.right) * currentScale);
            }

            if (rectF.bottom < displayHeight) {
                currentY -= ((displayHeight - rectF.bottom) * currentScale);
            }
        }
    }

    private class ScrollListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            rectF = ImageUtils.getImageBounds(imgView2Edit);

            if (rectF.left > distanceX || rectF.right - distanceX < displayWidth) {
                distanceX = 0;
            }
            if (rectF.top > distanceY || rectF.bottom - distanceY < displayHeight) {
                distanceY = 0;
            }

            currentX += distanceX;
            currentY += distanceY;
            matrix.postTranslate(-distanceX, -distanceY);
            imgView2Edit.setImageMatrix(matrix);

            return true;
        }

    }

    private class ImageOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mScaleGestureDetector.onTouchEvent(event);
            mGestureDetector.onTouchEvent(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //do nothing
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    break;
                default:
                    break;
            }
            return true;
        }


    }
    //endregion
}