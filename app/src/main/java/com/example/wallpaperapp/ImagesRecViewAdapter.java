package com.example.wallpaperapp;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.app.WallpaperManager.FLAG_LOCK;

public class ImagesRecViewAdapter extends RecyclerView.Adapter<ImagesRecViewAdapter.ViewHolder> {

    private static ArrayList<ImageModel> imagesList = new ArrayList<>();
    private Context mainContext;
    private DisplayMetrics displayMetrics;


    public ImagesRecViewAdapter(Context context) {
        mainContext = context;
        displayMetrics = mainContext.getResources().getDisplayMetrics();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.images_list_item, parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Bitmap imgBitmap = BitmapFactory.decodeFile(imagesList.get(position).getFile().getAbsolutePath());
//        holder.imageViewItem.setImageBitmap(imgBitmap);

        Glide.with(mainContext)
                .load(imagesList.get(position).getFile().getAbsolutePath())
                .centerCrop()
                .into(holder.imageViewItem);

        holder.imageViewItem.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                //setWallPaper(imgBitmap);
                //Toast.makeText(mainContext, imagesList.get(position).getName() + " Selected", Toast.LENGTH_SHORT).show();
            }
        });

        holder.imageViewItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handleDelete(position);
                return true;
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CardView parentLayout;
        private ImageView imageViewItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parentLayout = itemView.findViewById(R.id.parentLayout);
            imageViewItem = itemView.findViewById(R.id.imgViewItem);
            //Make each image 1/3 of the screen width
            imageViewItem.getLayoutParams().width = displayMetrics.widthPixels / 3;
            imageViewItem.getLayoutParams().height = imageViewItem.getLayoutParams().width * displayMetrics.heightPixels / displayMetrics.widthPixels;
        }
    }
}
