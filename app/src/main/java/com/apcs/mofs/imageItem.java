package com.apcs.mofs;

import android.net.Uri;

public class imageItem {
    private Uri _uri;
    private String _title;

    public imageItem(Uri _uri, String _title) {
        this._uri = _uri;
        this._title = _title;
    }

    public Uri getUri() {
        return _uri;
    }

    public void setUri(Uri uri) {
        this._uri = uri;
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        this._title = title;
    }

}