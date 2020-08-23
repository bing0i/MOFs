package com.apcs.mofs;

import android.net.Uri;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class Landmark {
    private String _title;
    private String _description;
    private int _logoID;
    private LatLng _latlong;
    private Uri _uri;


    public Landmark(String title, String description, int logoID, LatLng latlong) {
        this._title = title;
        this._description = description;
        this._logoID = logoID;
        this._latlong = latlong;
    }

    public Landmark() {
        _title ="";
        _description = "";
        _logoID = 0;
        _uri = null;
    }

    public Landmark(String _title, String _description, LatLng _latlong, Uri _uri) {
        this._title = _title;
        this._description = _description;
        this._latlong = _latlong;
        this._uri = _uri;
    }

    public Uri getUri() {
        return _uri;
    }

    public void setUri(Uri _uri) {
        this._uri = _uri;
    }


    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        this._title = title;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        this._description = description;
    }

    public int getLogoID() {
        return _logoID;
    }

    public void setLogoID(int logoID) {
        this._logoID = logoID;
    }

    public LatLng getLatlong() {
        return _latlong;
    }

    public void setLatlong(LatLng latlong) {
        this._latlong = latlong;
    }
}