package com.apcs.mofs;

import android.net.Uri;

public class UserInfo {
    private String name = "";
    private String username = "";
    private String email = "";
    private Uri photo = null;

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
}
