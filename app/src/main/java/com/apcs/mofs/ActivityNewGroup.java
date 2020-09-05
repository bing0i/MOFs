package com.apcs.mofs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ActivityNewGroup extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private String username = "";
    private EditText editTextGroupName;
    private String groupName = "";
    private ArrayList<InfoUser> friends = new ArrayList<>();
    private ArrayList<InfoUser> members = new ArrayList<>();
    private AdapterFriends adapterNewGroup;
    private AdapterFriends adapterFriends;
    private ListView listViewFriends;
    private ListView listViewMembers;
    private String TAG = "RRRRRRRRRRRRRRRRRRRR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        initComponent();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ActivityGroups.class);
        startActivity(intent);
        finish();
    }

    private void initComponent() {
        username = getIntent().getStringExtra("username");
        editTextGroupName = (EditText)findViewById(R.id.editText);

        //Retrieve friend list
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mFriends = mDatabase.child("users")
                .child(username).child("friends");
        DatabaseReference mUsers = mDatabase.child("users");
        listViewFriends = (ListView)findViewById(R.id.listFriends);
        TaskUpdateListViewWithFirebaseData taskUpdateListViewWithFirebaseData =
                new TaskUpdateListViewWithFirebaseData("Friends", mFriends, mUsers, listViewFriends, getApplicationContext());
        taskUpdateListViewWithFirebaseData.updateListViewFriends();
        listViewFriends = taskUpdateListViewWithFirebaseData.getListView();
        setEventFriendsListView();
        adapterFriends = taskUpdateListViewWithFirebaseData.getAdapterFriends();
        friends = taskUpdateListViewWithFirebaseData.getListToUpdateListViewFriends();

        //Members in new group
        adapterNewGroup = new AdapterFriends(this, 0, members);
        listViewMembers = (ListView)findViewById(R.id.listNewGroup);
        listViewMembers.setAdapter(adapterNewGroup);
        setEventNewGroupListView();
    }

    private void setEventFriendsListView() {
        listViewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                members.add(friends.get(i));
                friends.remove(friends.get(i));
                adapterFriends.notifyDataSetChanged();
                adapterNewGroup.notifyDataSetChanged();
            }
        });
    }

    private void setEventNewGroupListView() {
        listViewMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                friends.add(members.get(i));
                members.remove(members.get(i));
                adapterNewGroup.notifyDataSetChanged();
                adapterFriends.notifyDataSetChanged();
            }
        });
    }

    public void buttonAddClicked(View view) {
        groupName = editTextGroupName.getText().toString();
        if (groupName.equals(""))
            return;
        String key =  mDatabase.child("groups").push().getKey();
        mDatabase.child("users").child(username).child("groups").child(key).setValue(true);
        mDatabase.child("groups").child(key).child("name").setValue(groupName);
        mDatabase.child("groups").child(key).child("members").child(username).setValue(true);
        for (int i = 0; i < members.size(); ++i) {
            mDatabase.child("groups").child(key).child("members").child(members.get(i).getUsername()).setValue(true);
            mDatabase.child("users").child(members.get(i).getUsername()).child("groups").child(key).setValue(true);
        }
        Intent intent = new Intent(this, ActivityGroups.class);
        intent.putExtra("keyChat", key);
        intent.putExtra("groupName", groupName);
        startActivity(intent);
        finish();
    }
}