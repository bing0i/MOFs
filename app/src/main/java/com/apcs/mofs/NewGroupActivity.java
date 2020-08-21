package com.apcs.mofs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewGroupActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private String username = "";
    private EditText editText;
    private String groupName = "";

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
    }

    public void buttonAddClicked(View view) {
        groupName = editText.getText().toString();
        if (groupName.equals(""))
            return;
        String key =  mDatabase.child("groups").push().getKey();
        mDatabase.child("users").child(username).child("groups").child(key).setValue(true);
        mDatabase.child("groups").child(key).child("name").setValue(groupName);
        mDatabase.child("groups").child(key).child("members").child(username).setValue(true);
        Intent intent = new Intent(this, ActivityListGroup.class);
        intent.putExtra("keyChat", key);
        intent.putExtra("groupName", groupName);
        startActivity(intent);
        finish();
    }
}