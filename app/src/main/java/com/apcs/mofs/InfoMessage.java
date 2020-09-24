package com.apcs.mofs;

import android.graphics.Bitmap;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import static java.text.DateFormat.getDateTimeInstance;

public class InfoMessage {
    private String name = "";
    private String message = "";
    private String imagePath = "";
    private Bitmap bitmap = null;
    private long timestamp;

    public InfoMessage() {
    }

    public InfoMessage(String name, String message, String profilePath) {
        this.name = name;
        this.message = message;
        this.imagePath = profilePath;
    }

    public InfoMessage(String name, String message, Bitmap bitmap) {
        this.name = name;
        this.message = message;
        this.bitmap = bitmap;
    }

    public InfoMessage(String key, String s) {
        if (key.equals("username"))
            this.name = s;
        else if (key.equals("message"))
            this.message = s;
    }

    public static String getTimeDate(long timestamp){
        try{
            DateFormat dateFormat = getDateTimeInstance();
            Date netDate = (new Date(timestamp));
            return dateFormat.format(netDate);
        } catch(Exception e) {
            return "date";
        }
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
