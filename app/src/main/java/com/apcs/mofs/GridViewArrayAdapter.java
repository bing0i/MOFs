package com.apcs.mofs;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GridViewArrayAdapter extends ArrayAdapter<imageItem> {
    private Context _context;
    private int _layoutID;
    private List<imageItem> _item;

    public GridViewArrayAdapter(@NonNull Context context, int resource, @NonNull List<imageItem> objects) {
        super(context, resource, objects);
        _context = context;
        _layoutID = resource;
        _item = objects;
    }

    @Override
    public int getCount() {
        return _item.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            LayoutInflater layoutInflater = LayoutInflater.from(_context);
            convertView = layoutInflater.inflate(_layoutID,null, false);
        }

        imageItem item = _item.get(position);

        Bitmap bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(_context.getContentResolver(),item.getUri());
        } catch (IOException e) {
            Toast.makeText(_context,"fail", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        ImageView imageView = convertView.findViewById(R.id.imageView);
        imageView.setImageBitmap(bmp);

        return convertView;
    }
}
