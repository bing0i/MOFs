package com.apcs.mofs;

import android.graphics.Bitmap;
import android.net.Uri;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class InfoMarker {
    private String title;
    private String snippetKey;
    private Bitmap bitmap;
    private LatLng latLong;
    private Double latitude;
    private Double longtitude;
    private String address;

    public InfoMarker(String title, String snippetKey, Bitmap bitmap, LatLng latLong) {
        this.title = title;
        this.snippetKey = snippetKey;
        this.bitmap = bitmap;
        this.latLong = latLong;
    }

    public InfoMarker() {
        title ="";
        snippetKey = "";
        bitmap = null;
    }

    public InfoMarker(String title, String snippetKey, LatLng latLong, String address) {
        this.title = title;
        this.snippetKey = snippetKey;
        this.latLong = latLong;
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippetKey() {
        return snippetKey;
    }

    public void setSnippetKey(String snippetKey) {
        this.snippetKey = snippetKey;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public LatLng getLatLong() {
        return latLong;
    }

    public void setLatLong(LatLng latLong) {
        this.latLong = latLong;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(Double longtitude) {
        this.longtitude = longtitude;
    }

    public void setLatLong() {
        setLatLong(new LatLng(latitude, longtitude));
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}