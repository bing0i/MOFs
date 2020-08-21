package com.apcs.mofs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NewGroupActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private String username = "";
    private EditText editText;
    private String groupName = "";
    private ArrayList<UserInfo> friends = new ArrayList<>();
    private ArrayList<UserInfo> newGroupList = new ArrayList<>();
    private FriendAdapter newGroupAdapter;
    private FriendAdapter friendAdapter;
    private ListView listViewFriends;
    private ListView listViewNewGroup;
    private String TAG = "RRRRRRRRRRRRRRRRRRRR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        initComponent();
    }

    private void initComponent() {
        username = getIntent().getStringExtra("username");

        mDatabase = FirebaseDatabase.getInstance().getReference();

        editText = (EditText)findViewById(R.id.editText);

        friendAdapter = new FriendAdapter(this, 0, friends);
        listViewFriends = (ListView)findViewById(R.id.listFriends);
        listViewFriends.setAdapter(friendAdapter);
        setEventFriendsListView();

        newGroupAdapter = new FriendAdapter(this, 0, newGroupList);
        listViewNewGroup = (ListView)findViewById(R.id.listNewGroup);
        listViewNewGroup.setAdapter(newGroupAdapter);
        setEventNewGroupListView();

        retrieveFriends();
    }

    private void setEventFriendsListView() {
        listViewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                newGroupList.add(friends.get(i));
                newGroupAdapter.notifyDataSetChanged();

                friends.remove(friends.get(i));
                friendAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setEventNewGroupListView() {
        listViewNewGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                friends.add(newGroupList.get(i));
                friendAdapter.notifyDataSetChanged();

                newGroupList.remove(newGroupList.get(i));
                newGroupAdapter.notifyDataSetChanged();
            }
        });
    }

    public void buttonAddClicked(View view) {
        groupName = editText.getText().toString();
        if (groupName.equals(""))
            return;
        String key =  mDatabase.child("groups").push().getKey();
        mDatabase.child("users").child(username).child("groups").child(key).setValue(true);
        mDatabase.child("groups").child(key).child("name").setValue(groupName);
        mDatabase.child("groups").child(key).child("members").child(username).setValue(true);
        for (int i = 0; i < newGroupList.size(); ++i) {
            mDatabase.child("groups").child(key).child("members").child(newGroupList.get(i).getUsername()).setValue(true);
            mDatabase.child("users").child(newGroupList.get(i).getUsername()).child("groups").child(key).setValue(true);
        }
        Intent intent = new Intent(this, ActivityListGroup.class);
        intent.putExtra("keyChat", key);
        intent.putExtra("groupName", groupName);
        startActivity(intent);
        finish();
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
}