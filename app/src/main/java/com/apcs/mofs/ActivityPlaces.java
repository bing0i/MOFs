package com.apcs.mofs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class ActivityPlaces extends AppCompatActivity {
    private GridView gridView;
    private AdapterPlaces adapterPlaces;
    private ArrayList<InfoMarker> markers = new ArrayList<>();
    private DatabaseReference mRef;
    private String keyChat;
    private final String TAG = "RRRRRRRRRRRRRRRRRRRRRR";
    private final long MAX_SIZE_IMAGE = 10485760; //10MB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        initComponents();
    }

    private void initComponents() {
        getSupportActionBar().setTitle("Places");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        keyChat = getIntent().getStringExtra("keyChat");
        mRef = FirebaseDatabase.getInstance().getReference().child("landmarks").child(keyChat);
        gridView = (GridView)findViewById(R.id.gridview_places);
        adapterPlaces = new AdapterPlaces(this, R.layout.item_place, markers, keyChat);
        gridView.setAdapter(adapterPlaces);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                InfoMarker marker = markers.get(position);
                getIntent().putExtra("latitude", marker.getLatLong().getLatitude());
                getIntent().putExtra("longitude", marker.getLatLong().getLongitude());
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });
        retrievePlaces();
    }

    private void retrievePlaces() {
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                markers.clear();
                InfoMarker infoMarker = new InfoMarker();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    for (DataSnapshot dsMarker : ds.getChildren()) {
                        if (dsMarker.getKey().equals("longitude"))
                            infoMarker.setLongtitude(dsMarker.getValue(Double.class));
                        else if (dsMarker.getKey().equals("latitude"))
                            infoMarker.setLatitude(dsMarker.getValue(Double.class));
                        else if (dsMarker.getKey().equals("title"))
                            infoMarker.setTitle(dsMarker.getValue(String.class));
                        else if (dsMarker.getKey().equals("address"))
                            infoMarker.setAddress(dsMarker.getValue(String.class));
                        else if (dsMarker.getKey().equals("snippet"))
                            infoMarker.setSnippetKey(dsMarker.getValue(String.class));
                    }
                    infoMarker.setLatLong();
                    markers.add(infoMarker);
                    infoMarker = new InfoMarker();
                }
                adapterPlaces.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}