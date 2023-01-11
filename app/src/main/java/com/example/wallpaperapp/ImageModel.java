package com.example.wallpaperapp;

import org.parceler.Parcel;

import java.io.File;

//intent.putExtra("ImageModel", Parcels.wrap(imageModel));
//ImageModel imageModel = (ImageModel) Parcels.unwrap(getIntent().getParcelableExtra("ImageModel"));

@Parcel
public class ImageModel {
    // for @Parcel fields must be package private
    File file;
    String name;
    String rectStr;

    // empty constructor is needed by the Parceler library
    public ImageModel() {
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public ImageModel(File file, String name, String rectStr) {
        this.file = file;
        this.name = name;
        this.rectStr = rectStr;
    }
}
