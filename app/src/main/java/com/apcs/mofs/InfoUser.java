package com.apcs.mofs;

import android.graphics.Bitmap;
import android.net.Uri;

public class InfoUser {
    private String name = "";
    private String username = "";
    private String email = "";
    private Uri uri = null;
    private Bitmap bitmap = null;

    public InfoUser() {}

    public InfoUser(String uname) {
        username = uname;
    }

    public InfoUser(String uname, String email, Uri uri) {
        username = uname;
        this.email = email;
        this.uri = uri;
    }

    public InfoUser(String uname, Bitmap uri) {
        username = uname;
        this.bitmap = uri;
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

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
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
