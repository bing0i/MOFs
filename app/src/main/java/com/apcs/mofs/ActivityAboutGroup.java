package com.apcs.mofs;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
    private ArrayList<UserInfo> members = new ArrayList<>();
    private FriendAdapter memberAdapter;
    private ArrayList<UserInfo> users = new ArrayList<>();
    private String TAG = "RRRRRRRRRRRRRRRRRRRRRR";
    private ListView listView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_group);
        initComponents();
    }

    private void initComponents() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        memberAdapter = new FriendAdapter(this, 0, users);
        listView = (ListView)findViewById(R.id.listViewAboutGroup);
        listView.setAdapter(memberAdapter);
        textView = (TextView)findViewById(R.id.group_name);
        retrieveUsers();
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

    private void retrieveUsers() {
        DatabaseReference mUsers = mDatabase.child("users");
        mUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    readData(new ActivityAboutGroup.MyCallback() {
                        @Override
                        public void onCallback(ArrayList<UserInfo> members) {
                            for (int i = 0; i < members.size(); i++) {
                                if (userSnapshot.getKey().equals(members.get(i).getUsername())) {
                                    for (DataSnapshot metaSnapshot: userSnapshot.getChildren()) {
                                        if (metaSnapshot.getKey().equals("profilePhoto")) {
                                            new ActivityAboutGroup.RetrieveBitmapTask().execute(metaSnapshot.getValue(String.class), members.get(i).getUsername());
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
        void onCallback(ArrayList<UserInfo> members);
    }

    public void readData(ActivityAboutGroup.MyCallback myCallback) {
        DatabaseReference mGroups = mDatabase.child("groups").child(getIntent().getStringExtra("keyChat"));
        mGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                members.clear();
                for (DataSnapshot groupSnapshot: dataSnapshot.getChildren()) {
                    if (groupSnapshot.getKey().equals("members")) {
                        for (DataSnapshot metaSnapshot: groupSnapshot.getChildren()) {
                            members.add(new UserInfo(metaSnapshot.getKey(), null));
                        }
                    }
                    else if (groupSnapshot.getKey().equals("name")) {
                        textView.setText(groupSnapshot.getValue(String.class));
                    }
                }
                myCallback.onCallback(members);
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

    private class RetrieveBitmapTask extends AsyncTask<String, Void, ActivityAboutGroup.MyTaskParams> {

        protected ActivityAboutGroup.MyTaskParams doInBackground(String... urls) {
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
            ActivityAboutGroup.MyTaskParams myTaskParams = new ActivityAboutGroup.MyTaskParams(urls[1], bm);
            return myTaskParams;
        }

        protected void onPostExecute(ActivityAboutGroup.MyTaskParams myTaskParams) {
            users.add(new UserInfo(myTaskParams.username, myTaskParams.bitmap));
            memberAdapter.notifyDataSetChanged();
        }
    }
}
