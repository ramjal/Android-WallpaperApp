package com.example.wallpaperapp;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;

import static android.app.WallpaperManager.FLAG_LOCK;

public class ImagesRecViewAdapter extends RecyclerView.Adapter<ImagesRecViewAdapter.ViewHolder> {

    private ArrayList<ImageModel> imagesList = new ArrayList<>();
    private Context mainContext;

    public ImagesRecViewAdapter(Context context) {
        this.mainContext = context;
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
        holder.path.setText(imagesList.get(position).getName());
        Bitmap imgBitmap = BitmapFactory.decodeFile(imagesList.get(position).getFile().getAbsolutePath());
        holder.image.setImageBitmap(imgBitmap);
        //holder.parentLayout.setOnClickListener(new View.OnClickListener() {
        holder.image.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Toast.makeText(mainContext, imagesList.get(position).getName() + " Selected", Toast.LENGTH_SHORT).show();
                setWallPaper(imgBitmap);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setWallPaper(Bitmap imgBitmap) {
        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(mainContext);
        try {
            if (myWallpaperManager.setBitmap(imgBitmap, null, false, FLAG_LOCK) > 0) {
                Toast.makeText(mainContext, "Wallpaper Set!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(mainContext, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    public void setImages(ArrayList<ImageModel> imgList) {
        imagesList = imgList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView path;
        private CardView parentLayout;
        private ImageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            path = itemView.findViewById(R.id.txtPathItem);
            parentLayout = itemView.findViewById(R.id.parentLayout);
            image = itemView.findViewById(R.id.imgViewItem);
        }
    }
}
