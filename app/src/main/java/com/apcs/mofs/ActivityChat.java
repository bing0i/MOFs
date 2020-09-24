package com.apcs.mofs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityChat extends AppCompatActivity {
    private static ListView listViewMessages = null;
    private static AdapterChat adapterChat = null;
    private static ArrayList<InfoMessage> messages = new ArrayList<>();
    private EditText editTextChat = null;
    private DatabaseReference mDatabase;
    DatabaseReference mMessage;
    private static String TAG = "RRRRRRRRRRRRRRRRRRRRR";
    private String keyChat = "";
    private String username = "";
    private String photoProfile;
    private int countMessages = 0;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initComponents();
    }

    private void initComponents() {
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        getSupportActionBar().setTitle("Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        keyChat = getIntent().getStringExtra("keyChat");
        username = getIntent().getStringExtra("username");
        photoProfile = getIntent().getStringExtra("photoProfile");

        listViewMessages = (ListView)findViewById(R.id.listMessages);
        adapterChat = new AdapterChat(this, 0, messages, username);
        listViewMessages.setAdapter(adapterChat);
        editTextChat = (EditText)findViewById(R.id.editText);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mMessage = mDatabase.child("messages").child(keyChat);
        retrieveNewMessage();
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

    private void retrieveNewMessage() {
        mMessage.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                InfoMessage metaMessage = new InfoMessage();
                for (DataSnapshot metaSnapshot: snapshot.getChildren()) {
                    if (metaSnapshot.getKey().equals("username"))
                        metaMessage.setName(metaSnapshot.getValue(String.class));
                    else if (metaSnapshot.getKey().equals("message"))
                        metaMessage.setMessage(metaSnapshot.getValue(String.class));
                    else if (metaSnapshot.getKey().equals("photoProfile"))
                        metaMessage.setImagePath(metaSnapshot.getValue(String.class));
                    else if (metaSnapshot.getKey().equals("timestamp"))
                        metaMessage.setTimestamp(metaSnapshot.getValue(Long.class));
                }
                new ActivityChat.RetrieveBitmapTask().execute(metaMessage.getName(), metaMessage.getMessage(), metaMessage.getImagePath());
                metaMessage = new InfoMessage();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.toString() + " Failed to get child node");
            }
        });
    }

    public void buttonClicked(View view) {
        switch (view.getId()) {
            case R.id.addButton:
                break;
            case R.id.sendButton:
                String message = editTextChat.getText().toString();
                sendMetaMessageToFirebase(message);
                editTextChat.setText("");
                break;
        }
    }

    private void sendMetaMessageToFirebase(String message) {
        if (message.equals(""))
            return;
        String key = mDatabase.child("messages").child(keyChat).push().getKey();
        Map map = new HashMap();
        map.put("timestamp", ServerValue.TIMESTAMP);
        map.put("username", username);
        map.put("message", message);
        map.put("photoProfile", photoProfile);
        mMessage.child(key).updateChildren(map);;
    }

    private static class MyTaskParams {
        ArrayList<String> info;
        Bitmap bitmap;

        MyTaskParams(ArrayList<String> info, Bitmap bitmap) {
            this.info = info;
            this.bitmap = bitmap;
        }
    }

    private static class RetrieveBitmapTask extends AsyncTask<String, Void, MyTaskParams> {

        protected MyTaskParams doInBackground(String... urls) {
            Bitmap bm = null;
            try {
                URL aURL = new URL(urls[2]);
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
            ArrayList<String> info = new ArrayList<>();
            info.add(urls[0]);
            info.add(urls[1]);
            MyTaskParams myTaskParams = new MyTaskParams(info, bm);
            return myTaskParams;
        }

        protected void onPostExecute(MyTaskParams myTaskParams) {
            if (myTaskParams.bitmap != null) {
                messages.add(new InfoMessage(myTaskParams.info.get(0), myTaskParams.info.get(1), myTaskParams.bitmap));
                adapterChat.notifyDataSetChanged();
                listViewMessages.post(new Runnable() {
                    @Override
                    public void run() {
                        // Select the last row so it will scroll into view...
                        listViewMessages.setSelection(adapterChat.getCount() - 1);
                    }
                });
            }
        }
    }
}