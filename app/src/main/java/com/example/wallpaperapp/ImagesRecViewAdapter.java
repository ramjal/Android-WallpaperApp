package com.example.wallpaperapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class ImagesRecViewAdapter extends RecyclerView.Adapter<ImagesRecViewAdapter.PictureViewHolder> {

    //private final SharedPreferences sharedPreferences;
    private static ArrayList<ImageModel> imagesList;
    private final Context mainContext;
    private OnPictureClickListener onPictureClickListener;

    public ImagesRecViewAdapter(Context context, OnPictureClickListener onPictureClickListener) {
        mainContext = context;
        this.onPictureClickListener = onPictureClickListener;
        //Get the shared preferences for reading app saved data
        //sharedPreferences = mainContext.getSharedPreferences(MainActivity.SHARED_PREF_FILE_NAME, MODE_PRIVATE);
    }

    @NonNull
    @Override
    public PictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.images_list_item, parent, false);
        PictureViewHolder holder = new PictureViewHolder(view, onPictureClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PictureViewHolder holder, int position) {
        ImageModel image = imagesList.get(position);
        holder.txtViewItem.setText(String.valueOf(position));
        Glide.with(mainContext)
                .load(image.getFile().getAbsolutePath())
                .into(holder.imageViewItem);

        holder.imageViewItem.setImageMatrix(createImageMartix(holder.imageViewItem, image));
    }

    private Matrix createImageMartix(ImageView imageView, ImageModel image) {
        Matrix matrix = null;

        String imagePath = image.getFile().getAbsolutePath();
        if (image.rectStr != null) {
            BitmapFactory.Options options = ImageUtils.getImageOptions(imagePath);
            int imageViewWidth = imageView.getLayoutParams().width;
            int imageViewHeight = imageView.getLayoutParams().height;
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
            matrix = createImageMartixCenter(imageView, imagePath);
        }
        return matrix;
    }

    private Matrix createImageMartixCenter(ImageView imageView, String imagePath) {
        Matrix matrix = new Matrix();

        float dx = 0f;
        float dy = 0f;
        float aspectView = (float) imageView.getLayoutParams().width / imageView.getLayoutParams().height;
        float aspectImage = (float) ImageUtils.getImageOptions(imagePath).outWidth / ImageUtils.getImageOptions(imagePath).outHeight;

        float ratio = 1f;
        if (aspectView > aspectImage) {
            ratio = (float) imageView.getLayoutParams().width / ImageUtils.getImageOptions(imagePath).outWidth;
            dy = (ImageUtils.getImageOptions(imagePath).outHeight * ratio - imageView.getLayoutParams().height) / 2;
        } else {
            ratio = (float) imageView.getLayoutParams().height / ImageUtils.getImageOptions(imagePath).outHeight;
            dx = (ImageUtils.getImageOptions(imagePath).outWidth * ratio - imageView.getLayoutParams().width) / 2;
        }

        matrix.postTranslate(-dx, -dy);
        return matrix;
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    public void setImagesList(ArrayList<ImageModel> imgList) {
        imagesList = imgList;
        notifyDataSetChanged();
    }

    public ArrayList<ImageModel> getImagesList() {
        return imagesList;
    }

    public class PictureViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final CardView cardView;
        private final ImageView imageViewItem;
        private final TextView txtViewItem;
        private final OnPictureClickListener onPictureClickListener;

        public PictureViewHolder(@NonNull View itemView, OnPictureClickListener onPictureClickListener) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            imageViewItem = itemView.findViewById(R.id.imgViewItem);
            txtViewItem = itemView.findViewById(R.id.txtViewItem);

            this.onPictureClickListener = onPictureClickListener;
            //Make each image 1/3 of the screen width
            Point size = ImageUtils.GetDisplaySize((Activity) mainContext);
            int displayWidth = size.x;
            int displayHeight = size.y;
            imageViewItem.getLayoutParams().width = displayWidth / 3;
            imageViewItem.getLayoutParams().height = imageViewItem.getLayoutParams().width * displayHeight / displayWidth;

            //Should implement onClick and onLongClick
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        //when itemView clicked it calls on OnPictureClickListener.onPictureClick
        @Override
        public void onClick(View v) {
            onPictureClickListener.onPictureClick(getAdapterPosition());
        }


        @Override
        public boolean onLongClick(View v) {
            return onPictureClickListener.onPictureLongClick(getAdapterPosition());
        }
    }

    //The activity should implement this listener
    public interface OnPictureClickListener {
        void onPictureClick(int position);
        boolean onPictureLongClick(int position);
    }
}
