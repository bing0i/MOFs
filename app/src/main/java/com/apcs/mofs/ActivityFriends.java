package com.apcs.mofs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

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

public class ActivityFriends extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private ArrayList<InfoUser> friends = new ArrayList<>();
    private AdapterFriends adapterFriends;
    private ArrayList<InfoUser> users = new ArrayList<>();
    private String TAG = "RRRRRRRRRRRRRRRRRRRRRR";
    private ListView listView;
    private String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        initComponents();
    }

    private void initComponents() {
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        username = getIntent().getStringExtra("username");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mFriends = mDatabase.child("users")
                .child(username).child("friends");
        DatabaseReference mUsers = mDatabase.child("users");
        listView = (ListView)findViewById(R.id.listViewFriend);

        TaskUpdateListViewWithFirebaseData taskUpdateListViewWithFirebaseData =
                new TaskUpdateListViewWithFirebaseData("Friends", mFriends, mUsers, listView, R.layout.item_friend_with_delete_button, username, "friends", getApplicationContext());
        taskUpdateListViewWithFirebaseData.updateListViewFriends();
        adapterFriends = taskUpdateListViewWithFirebaseData.getAdapterFriends();
        listView = taskUpdateListViewWithFirebaseData.getListView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}