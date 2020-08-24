package com.apcs.mofs;

import android.graphics.Bitmap;
import android.net.Uri;

public class InfoUser {
    private String name = "";
    private String username = "";
    private String email = "";
    private Uri photo = null;
    private Bitmap bitmap = null;

    public InfoUser() {
    }
    public InfoUser(String uname) {
        username = uname;
    }
    public InfoUser(String uname, String email, Uri photo) {
        username = uname;
        this.email = email;
        this.photo = photo;
    }
    public InfoUser(String uname, Bitmap photo) {
        username = uname;
        this.bitmap = photo;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Uri getPhoto() {
        return photo;
    }
    public void setPhoto(Uri photo) {
        this.photo = photo;
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
