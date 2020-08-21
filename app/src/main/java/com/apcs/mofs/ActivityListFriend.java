package com.apcs.mofs;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ActivityListFriend extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private ArrayList<UserInfo> friends = new ArrayList<>();
    private FriendAdapter friendAdapter;
    private ArrayList<UserInfo> users = new ArrayList<>();
    private String TAG = "RRRRRRRRRRRRRRRRRRRRRR";
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friend);
        initComponents();
    }

    private void initComponents() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        friendAdapter = new FriendAdapter(this, 0, friends);
        listView = (ListView)findViewById(R.id.listViewFriend);
        listView.setAdapter(friendAdapter);
//        retrieveUsers();
        retrieveFriends();
    }

    private void retrieveFriends() {
        DatabaseReference mGroups = mDatabase.child("users").child(getIntent().getStringExtra("username"));
        mGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friends.clear();
                for (DataSnapshot metaSnapshot: dataSnapshot.getChildren()) {
                    if (metaSnapshot.getKey().equals("friends")) {
                        for (DataSnapshot friendSnapshot: metaSnapshot.getChildren()) {
                            friends.add(new UserInfo(friendSnapshot.getKey(), null));
                            friendAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void retrieveUsers() {
            DatabaseReference mUsers = mDatabase.child("users");
            mUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    users.clear();
                    for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                        users.add(new UserInfo(userSnapshot.getKey()));
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
    }
}