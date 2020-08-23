package com.apcs.mofs;

import android.graphics.Bitmap;

public class MessageInfo {
    private String name = "";
    private String message = "";
    private String imagePath = "";
    private Bitmap bitmap = null;

    public MessageInfo() {
    }

    public MessageInfo(String name, String message, String profilePath) {
        this.name = name;
        this.message = message;
        this.imagePath = profilePath;
    }

    public MessageInfo(String name, String message, Bitmap bitmap) {
        this.name = name;
        this.message = message;
        this.bitmap = bitmap;
    }

    public MessageInfo(String key, String s) {
        if (key.equals("username"))
            this.name = s;
        else if (key.equals("message"))
            this.message = s;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
