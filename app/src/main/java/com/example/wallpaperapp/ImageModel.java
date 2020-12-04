package com.example.wallpaperapp;

import java.io.File;

public class ImageModel {
    private File file;
    private String name;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ImageModel(File file, String name) {
        this.file = file;
        this.name = name;
    }
}
