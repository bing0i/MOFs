package com.apcs.mofs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private ListView listViewMessages = null;
    private MessageAdapter messageAdapter = null;
    private ArrayList<MessageInfo> messages = new ArrayList<>();
    private EditText editTextChat = null;
    private DatabaseReference mDatabase;
    String TAG = "RRRRRRRRRRRRRRRRRRRRR";
    private String groupName = "asl";
    private String username = "Courgette";
    private MessageInfo messageInfoTmp = new MessageInfo("", "", 0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        initComponents();
    }

    private void initComponents() {
        listViewMessages = (ListView)findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this, 0, messages);
        listViewMessages.setAdapter(messageAdapter);
        editTextChat = (EditText)findViewById(R.id.editText);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        retrieveMessages();
//        addAMessage();
    }

    private void retrieveMessages() {
        DatabaseReference mMessage = mDatabase.child("messages").child(groupName);
        mMessage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messages.clear();
                ArrayList<String> metaMessage = new ArrayList<>();
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    for (DataSnapshot metaSnapshot: messageSnapshot.getChildren()) {
                        metaMessage.add(metaSnapshot.getValue(String.class));
                    }
                    if (metaMessage.size() == 2) {
                        messages.add(new MessageInfo(metaMessage.get(1), metaMessage.get(0), 0));
                        messageAdapter.notifyDataSetChanged();
                        metaMessage = new ArrayList<>();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("RRRRRRRR", "Failed to read value.", error.toException());
            }
        });
    }

    private void addAMessage() {
        final DatabaseReference mMessage = mDatabase.child("messages").child(groupName);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String name = "";
                String message = "";
                for (DataSnapshot messageSnapshot: snapshot.getChildren()) {
                    String key = messageSnapshot.getKey();
                    if (key.equals("username"))
                        name = messageSnapshot.getValue(String.class);
                    else if (key.equals("message"))
                        message = messageSnapshot.getValue(String.class);
                }
                addToListView(name, message);
            }

            private void addToListView(String name, String message) {
                if (!messageInfoTmp.getName().equals("") && !messageInfoTmp.getMessage().equals("")) {
                    messages.add(messageInfoTmp);
                    messageInfoTmp = new MessageInfo("", "", 0);
                }
                else if (messages.size() == 0 || (!name.equals("") && !message.equals(""))) {
                    messages.add(new MessageInfo(name, message, 0));
                }
                else {
                    messageInfoTmp.setName(name);
                    messageInfoTmp.setMessage(message);
                }
                messageAdapter.notifyDataSetChanged();
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
            }
        };
        mMessage.addChildEventListener(childEventListener);
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
        if (message == "")
            return;
        String key = mDatabase.child("messages").child(groupName).push().getKey();
        mDatabase.child("messages").child(groupName).child(key).child("username").setValue(username);
        mDatabase.child("messages").child(groupName).child(key).child("message").setValue(message);
        messages.add(new MessageInfo(username, message, 0));
        messageAdapter.notifyDataSetChanged();
    }
}