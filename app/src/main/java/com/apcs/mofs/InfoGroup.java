package com.apcs.mofs;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;

public class InfoGroup {
    private int defaultPhoto = R.drawable.ic_group;
    private Uri uri = null;
    private Bitmap bitmap = null;
    private String groupName = "";
    private String keyGroup = "";
    private ArrayList<String> namesOfMembers;

    public InfoGroup() {}

    public InfoGroup(String name, String key) {
        groupName = name;
        keyGroup = key;
    }

    public InfoGroup(String key) {
        keyGroup = key;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getKeyGroup() {
        return keyGroup;
    }

    public void setKeyGroup(String keyGroup) {
        this.keyGroup = keyGroup;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public ArrayList<String> getNamesOfMembers() {
        return namesOfMembers;
    }

    public void setNamesOfMembers(ArrayList<String> namesOfMembers) {
        this.namesOfMembers = namesOfMembers;
    }

    public int getDefaultPhoto() {
        return defaultPhoto;
    }

    public void setDefaultPhoto(int defaultPhoto) {
        this.defaultPhoto = defaultPhoto;
    }
}
