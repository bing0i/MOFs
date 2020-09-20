package com.apcs.mofs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ActivityAboutGroup extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private AdapterFriends adapterMembers;
    private String TAG = "RRRRRRRRRRRRRRRRRRRRRR";
    private ListView listView;
    private TextView textView;
    private String keyChat = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_group);
        initComponents();
    }

    private void initComponents() {
        keyChat = getIntent().getStringExtra("keyChat");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mFriends = mDatabase.child("groups")
                .child(keyChat).child("members");
        DatabaseReference mUsers = mDatabase.child("users");
        listView = (ListView)findViewById(R.id.listViewAboutGroup);
        TaskUpdateListViewWithFirebaseData taskUpdateListViewWithFirebaseData =
                new TaskUpdateListViewWithFirebaseData("Friends", mFriends, mUsers, listView, getApplicationContext());
        taskUpdateListViewWithFirebaseData.updateListViewFriends();
        listView = taskUpdateListViewWithFirebaseData.getListView();
        adapterMembers = taskUpdateListViewWithFirebaseData.getAdapterFriends();
        textView = (TextView)findViewById(R.id.group_name);
        setTextViewGroupName();
    }

    private void setTextViewGroupName() {
        DatabaseReference mRefGroupName = mDatabase.child("groups")
                .child(keyChat).child("name");
        mRefGroupName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                textView.setText(dataSnapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    public void navigationBottomClicked(View view) {
        switch (view.getId()) {
            case R.id.map:
                finish();
                break;
            case R.id.addMember:
                break;
        }
    }
}
