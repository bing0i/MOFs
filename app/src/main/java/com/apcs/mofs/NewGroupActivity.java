package com.apcs.mofs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class NewGroupActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private String username = "";
    private EditText editText;
    private String groupName = "";
    private ArrayList<UserInfo> friends = new ArrayList<>();
    private ArrayList<UserInfo> users = new ArrayList<>();
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ActivityListGroup.class);
        startActivity(intent);
        finish();
    }

    private void initComponent() {
        username = getIntent().getStringExtra("username");

        mDatabase = FirebaseDatabase.getInstance().getReference();

        editText = (EditText)findViewById(R.id.editText);

        friendAdapter = new FriendAdapter(this, 0, users);
        listViewFriends = (ListView)findViewById(R.id.listFriends);
        listViewFriends.setAdapter(friendAdapter);
        setEventFriendsListView();

        newGroupAdapter = new FriendAdapter(this, 0, newGroupList);
        listViewNewGroup = (ListView)findViewById(R.id.listNewGroup);
        listViewNewGroup.setAdapter(newGroupAdapter);
        setEventNewGroupListView();

        retrieveUsers();
    }

    private void setEventFriendsListView() {
        listViewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                newGroupList.add(users.get(i));
                newGroupAdapter.notifyDataSetChanged();

                users.remove(users.get(i));
                friendAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setEventNewGroupListView() {
        listViewNewGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                users.add(newGroupList.get(i));
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

    private void retrieveUsers() {
        DatabaseReference mUsers = mDatabase.child("users");
        mUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    readData(new NewGroupActivity.MyCallback() {
                        @Override
                        public void onCallback(ArrayList<UserInfo> friends) {
                            for (int i = 0; i < friends.size(); i++) {
                                if (userSnapshot.getKey().equals(friends.get(i).getUsername())) {
                                    for (DataSnapshot metaSnapshot: userSnapshot.getChildren()) {
                                        if (metaSnapshot.getKey().equals("profilePhoto")) {
                                            new NewGroupActivity.RetrieveBitmapTask().execute(metaSnapshot.getValue(String.class), friends.get(i).getUsername());
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public interface MyCallback {
        void onCallback(ArrayList<UserInfo> friends);
    }

    public void readData(NewGroupActivity.MyCallback myCallback) {
        DatabaseReference mGroups = mDatabase.child("users").child(getIntent().getStringExtra("username"));
        mGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friends.clear();
                for (DataSnapshot metaSnapshot: dataSnapshot.getChildren()) {
                    if (metaSnapshot.getKey().equals("friends")) {
                        for (DataSnapshot friendSnapshot: metaSnapshot.getChildren()) {
                            friends.add(new UserInfo(friendSnapshot.getKey(), null));
                        }
                    }
                }
                myCallback.onCallback(friends);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private static class MyTaskParams {
        String username;
        Bitmap bitmap;

        MyTaskParams(String username, Bitmap bitmap) {
            this.username = username;
            this.bitmap = bitmap;
        }
    }

    private class RetrieveBitmapTask extends AsyncTask<String, Void, NewGroupActivity.MyTaskParams> {

        protected NewGroupActivity.MyTaskParams doInBackground(String... urls) {
            Bitmap bm = null;
            try {
                URL aURL = new URL(urls[0]);
                URLConnection conn = aURL.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                bm = BitmapFactory.decodeStream(bis);
                bis.close();
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Error getting bitmap", e);
            }
            NewGroupActivity.MyTaskParams myTaskParams = new NewGroupActivity.MyTaskParams(urls[1], bm);
            return myTaskParams;
        }

        protected void onPostExecute(NewGroupActivity.MyTaskParams myTaskParams) {
            users.add(new UserInfo(myTaskParams.username, myTaskParams.bitmap));
            friendAdapter.notifyDataSetChanged();
        }
    }
}