package com.apcs.mofs;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.material.navigation.NavigationView;
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
        friendAdapter = new FriendAdapter(this, 0, users);
        listView = (ListView)findViewById(R.id.listViewFriend);
        listView.setAdapter(friendAdapter);
        retrieveUsers();
    }

    private void retrieveUsers() {
        DatabaseReference mUsers = mDatabase.child("users");
        mUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    readData(new ActivityListFriend.MyCallback() {
                        @Override
                        public void onCallback(ArrayList<UserInfo> friends) {
                            for (int i = 0; i < friends.size(); i++) {
                                if (userSnapshot.getKey().equals(friends.get(i).getUsername())) {
                                    for (DataSnapshot metaSnapshot: userSnapshot.getChildren()) {
                                        if (metaSnapshot.getKey().equals("profilePhoto")) {
                                            new ActivityListFriend.RetrieveBitmapTask().execute(metaSnapshot.getValue(String.class), friends.get(i).getUsername());
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

    public void readData(ActivityListFriend.MyCallback myCallback) {
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

    private class RetrieveBitmapTask extends AsyncTask<String, Void, MyTaskParams> {

        protected MyTaskParams doInBackground(String... urls) {
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
            MyTaskParams myTaskParams = new MyTaskParams(urls[1], bm);
            return myTaskParams;
        }

        protected void onPostExecute(MyTaskParams myTaskParams) {
            users.add(new UserInfo(myTaskParams.username, myTaskParams.bitmap));
            friendAdapter.notifyDataSetChanged();
        }
    }
}