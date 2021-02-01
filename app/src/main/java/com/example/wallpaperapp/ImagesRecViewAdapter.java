package com.example.wallpaperapp;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.app.WallpaperManager.FLAG_LOCK;

public class ImagesRecViewAdapter extends RecyclerView.Adapter<ImagesRecViewAdapter.PictureViewHolder> {

    private static ArrayList<ImageModel> imagesList;
    private Context mainContext;
    private DisplayMetrics displayMetrics;
    private OnPictureClickListener onPictureClickListener;

    public ImagesRecViewAdapter(Context context, OnPictureClickListener onPictureClickListener) {
        mainContext = context;
        displayMetrics = mainContext.getResources().getDisplayMetrics();
        this.onPictureClickListener = onPictureClickListener;
    }

    @NonNull
    @Override
    public PictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.images_list_item, parent,false);
        PictureViewHolder holder = new PictureViewHolder(view, onPictureClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PictureViewHolder holder, int position) {
//        Bitmap imgBitmap = BitmapFactory.decodeFile(imagesList.get(position).getFile().getAbsolutePath());
//        holder.imageViewItem.setImageBitmap(imgBitmap);

        String imagePath = imagesList.get(position).getFile().getAbsolutePath();

        Glide.with(mainContext)
                .load(imagePath)
                .into(holder.imageViewItem);
        //        .centerCrop()

        holder.imageViewItem.setImageMatrix(createImageMartix(holder.imageViewItem, imagePath));

//        holder.imageViewItem.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.N)
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(mainContext, PictureEditActivity.class);
//                intent.putExtra("FILE_PATH", imagesList.get(position).getFile().getAbsolutePath());
//                mainContext.startActivity(intent);
//
//                //setWallPaper(imgBitmap); //should fix this - should use an interface and do this inside the activity - look at RecyclerViewExample project
//                //Toast.makeText(mainContext, imagesList.get(position).getName() + " Selected", Toast.LENGTH_SHORT).show();
//            }
//        });

//        holder.imageViewItem.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                handleDelete(position);
//                return true;
//            }
//        });

    }

    private Matrix createImageMartix(ImageView imageView, String imagePath) {
        Matrix mMatrix = new Matrix();

        float dx = 0f;
        float dy = 0f;
        float aspectView = (float)imageView.getLayoutParams().width / imageView.getLayoutParams().height;
        float aspectImage = (float)ImageUtils.getImageOptions(imagePath).outWidth / ImageUtils.getImageOptions(imagePath).outHeight;

        float ratio = 1f;
        if (aspectView > aspectImage) {
            ratio = (float)imageView.getLayoutParams().width / ImageUtils.getImageOptions(imagePath).outWidth;
            dy = (ImageUtils.getImageOptions(imagePath).outHeight * ratio - imageView.getLayoutParams().height) / 2;
        } else {
            ratio = (float)imageView.getLayoutParams().height / ImageUtils.getImageOptions(imagePath).outHeight;
            dx = (ImageUtils.getImageOptions(imagePath).outWidth * ratio - imageView.getLayoutParams().width) / 2;
        }

        //mMatrix.setScale(currentScale, currentScale);
        mMatrix.postTranslate(-dx, -dy);
        return mMatrix;
    }

    private void setWallPaper(Bitmap imageBitmap) {
        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(mainContext);

        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        int imageWidth = imageBitmap.getWidth();
        int imageHeight = imageBitmap.getHeight();
        int left = 0;
        int top = 0;
        int right = imageWidth;
        int bottom = imageHeight;
        if (imageWidth > displayWidth) {
            left = (imageWidth - displayWidth) / 2;
            right = left + imageWidth;
        }

        int dw = myWallpaperManager.getDesiredMinimumWidth();
        int dh = myWallpaperManager.getDesiredMinimumHeight();

        Rect visibleRect = new Rect(left, top, right, bottom);
        //visibleRect = null;


       // https://stackoverflow.com/questions/7383361/android-wallpapermanager-crops-image


        String message = String.format("displayWidth = %d, displayHeight = %d" +
                        "\nimageWidth = %d, imageHeight = %d" +
                        "\nleft = %d, right = %d",
                displayWidth, displayHeight, imageWidth, imageHeight, left, right);

        //"Wallpaper Set!"
        try {
            if (myWallpaperManager.setBitmap(imageBitmap, visibleRect, false, FLAG_LOCK) > 0) {
                Toast.makeText(mainContext, message, Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(mainContext, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void handleDelete(int position) {
        //Toast.makeText(mainContext, imagesList.get(position).getName() + " Long Pressed!", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder myAlterDialog = new AlertDialog.Builder(mainContext);
        myAlterDialog.setTitle("Alert");
        myAlterDialog.setMessage("Click OK to continue, or Cancel to stop deleting this image.");

        // Add the dialog buttons.
        myAlterDialog.setPositiveButton("OK", new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        File file = imagesList.get(position).getFile();
                        try {
                            file.delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(mainContext, "!!!Error - Cannot Delete " + imagesList.get(position).getFile().getPath(), Toast.LENGTH_LONG).show();
                        }
                        Toast.makeText(mainContext, imagesList.get(position).getName() + " is now deleted!", Toast.LENGTH_SHORT).show();
                    }
                });
        myAlterDialog.setNegativeButton("Cancel", new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(mainContext, imagesList.get(position).getName() + " - Pressed Cancel", Toast.LENGTH_SHORT).show();
                   }
                });

        // Create and show the AlertDialog.
        myAlterDialog.show();
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    public void setImagesList(ArrayList<ImageModel> imgList) {
        imagesList = imgList;
        notifyDataSetChanged();
    }

    public static ArrayList<ImageModel> getImagesList() {
        return imagesList;
    }

    public class PictureViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final CardView cardView;
        private final RelativeLayout parentOfImage;
        private final ImageView imageViewItem;
        private final OnPictureClickListener onPictureClickListener;

        public PictureViewHolder(@NonNull View itemView, OnPictureClickListener onPictureClickListener) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            parentOfImage = itemView.findViewById(R.id.parentOfImage);
            imageViewItem = itemView.findViewById(R.id.imgViewItem);
            this.onPictureClickListener = onPictureClickListener;
            //Make each image 1/3 of the screen width
            imageViewItem.getLayoutParams().width = displayMetrics.widthPixels / 3;
            imageViewItem.getLayoutParams().height = imageViewItem.getLayoutParams().width * displayMetrics.heightPixels / displayMetrics.widthPixels;
            //Should implement onClick
            itemView.setOnClickListener(this);
        }

        //when itemView clicked it calls on OnPictureClickListener.onPictureClick
        @Override
        public void onClick(View v) {
            onPictureClickListener.onPictureClick(getAdapterPosition());
        }
    }

    //The activity should implement this listener
    public interface OnPictureClickListener {
        void onPictureClick(int position);
    }
}
