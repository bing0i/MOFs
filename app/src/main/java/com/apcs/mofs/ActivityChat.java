package com.apcs.mofs;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class ActivityChat extends AppCompatActivity {
    private ListView listViewMessages = null;
    private static AdapterChat adapterChat = null;
    private static ArrayList<InfoMessage> messages = new ArrayList<>();
    private EditText editTextChat = null;
    private DatabaseReference mDatabase;
    private static String TAG = "RRRRRRRRRRRRRRRRRRRRR";
    private String keyChat = "";
    private String username = "";
    private String photoProfile;
    private int countMessages = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initComponents();
    }

    private void initComponents() {
        keyChat = getIntent().getStringExtra("keyChat");
        username = getIntent().getStringExtra("username");
        photoProfile = getIntent().getStringExtra("photoProfile");

        listViewMessages = (ListView)findViewById(R.id.listMessages);
        adapterChat = new AdapterChat(this, 0, messages);
        listViewMessages.setAdapter(adapterChat);
        editTextChat = (EditText)findViewById(R.id.editText);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        retrieveMessages();
    }

    private void retrieveMessages() {
        DatabaseReference mMessage = mDatabase.child("messages").child(keyChat);
        mMessage.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messages.clear();
                InfoMessage metaMessage = new InfoMessage();
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    for (DataSnapshot metaSnapshot: messageSnapshot.getChildren()) {
                        if (metaSnapshot.getKey().equals("username"))
                            metaMessage.setName(metaSnapshot.getValue(String.class));
                        else if (metaSnapshot.getKey().equals("message"))
                            metaMessage.setMessage(metaSnapshot.getValue(String.class));
                        else if (metaSnapshot.getKey().equals("photoProfile"))
                            metaMessage.setImagePath(metaSnapshot.getValue(String.class));
                    }
                    new ActivityChat.RetrieveBitmapTask().execute(metaMessage.getName(), metaMessage.getMessage(), metaMessage.getImagePath());
                    metaMessage = new InfoMessage();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void buttonClicked(View view) {
        switch (view.getId()) {
            case R.id.addButton:
                break;
            case R.id.sendButton:
                String message = editTextChat.getText().toString();
                setDataMessage(message);
                editTextChat.setText("");
                break;
        }
    }

    private void setDataMessage(String message) {
        if (message.equals(""))
            return;
        String key = mDatabase.child("messages").child(keyChat).push().getKey();
        mDatabase.child("messages").child(keyChat).child(key).child("username").setValue(username);
        mDatabase.child("messages").child(keyChat).child(key).child("message").setValue(message);
        mDatabase.child("messages").child(keyChat).child(key).child("photoProfile").setValue(photoProfile);
        new ActivityChat.RetrieveBitmapTask().execute(username, message, photoProfile);
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
            }
        }
    }
}