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

public class ActivityAboutGroup extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private ArrayList<UserInfo> members = new ArrayList<>();
    private FriendAdapter memberAdapter;
    private ArrayList<UserInfo> users = new ArrayList<>();
    private String TAG = "RRRRRRRRRRRRRRRRRRRRRR";
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_group);
        initComponents();
    }

    private void initComponents() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        memberAdapter = new FriendAdapter(this, 0, members);
        listView = (ListView)findViewById(R.id.listViewAboutGroup);
        listView.setAdapter(memberAdapter);
        retrieveMembers();
    }

    private void retrieveMembers() {
        DatabaseReference mGroups = mDatabase.child("groups").child(getIntent().getStringExtra("keyChat"));
        mGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                members.clear();
                for (DataSnapshot groupSnapshot: dataSnapshot.getChildren()) {
                    if (groupSnapshot.getKey().equals("members")) {
                        for (DataSnapshot metaSnapshot: groupSnapshot.getChildren()) {
                            members.add(new UserInfo(metaSnapshot.getKey(), null));
                            memberAdapter.notifyDataSetChanged();
                        }
                        return;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
}
