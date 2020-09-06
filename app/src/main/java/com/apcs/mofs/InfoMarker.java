package com.apcs.mofs;

import android.net.Uri;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class InfoMarker {
    private String title;
    private String description;
    private int logoId;
    private LatLng latLong;
    private Uri uri;


    public InfoMarker(String title, String description, int logoId, LatLng latLong) {
        this.title = title;
        this.description = description;
        this.logoId = logoId;
        this.latLong = latLong;
    }

    public InfoMarker() {
        title ="";
        description = "";
        logoId = 0;
        uri = null;
    }

    public InfoMarker(String title, String description, LatLng latLong, Uri uri) {
        this.title = title;
        this.description = description;
        this.latLong = latLong;
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri _uri) {
        this.uri = _uri;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLogoId() {
        return logoId;
    }

    public void setLogoId(int logoId) {
        this.logoId = logoId;
    }

    public LatLng getLatLong() {
        return latLong;
    }

    public void setLatLong(LatLng latLong) {
        this.latLong = latLong;
    }
}