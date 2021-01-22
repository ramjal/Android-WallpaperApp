package com.example.wallpaperapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class PictureEditActivity extends AppCompatActivity {

    ImageView imageViewEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_edit);

        getSupportActionBar().hide(); //hide the title bar

        imageViewEdit = findViewById(R.id.imgViewEdit);
        String filePath = getIntent().getStringExtra("FILE_PATH");

        Glide.with(this)
                .load(filePath)
                .centerCrop()
                .into(imageViewEdit);


    }

    public void btnOKClicked(View view) {
        Toast.makeText(this, "OK!", Toast.LENGTH_SHORT).show();
    }
}