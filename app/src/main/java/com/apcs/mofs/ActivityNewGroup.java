package com.apcs.mofs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
    String keyChat = "";

    //Load image
    private final long MAX_SIZE_IMAGE = 10485760; //10MB
    private final int REQUEST_CODE_PICK_PHOTO = 12;

    //Storage
    StorageReference mStorage;

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
        mStorage = FirebaseStorage.getInstance().getReference();
        getSupportActionBar().setTitle("New group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        if (keyChat.length() == 0)
            keyChat =  mDatabase.child("groups").push().getKey();
        mDatabase.child("users").child(username).child("groups").child(keyChat).setValue(true);
        mDatabase.child("groups").child(keyChat).child("name").setValue(groupName);
        mDatabase.child("groups").child(keyChat).child("members").child(username).setValue(true);
        for (int i = 0; i < members.size(); ++i) {
            mDatabase.child("groups").child(keyChat).child("members").child(members.get(i).getUsername()).setValue(true);
            mDatabase.child("users").child(members.get(i).getUsername()).child("groups").child(keyChat).setValue(true);
        }
        finish();
    }

    public void buttonChooseImageClicked(View view) {
        keyChat =  mDatabase.child("groups").push().getKey();

        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Choose photo");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, REQUEST_CODE_PICK_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_PHOTO && resultCode == RESULT_OK) {
            if (data == null) {
                Log.d(TAG, "Failed to pick image");
            } else {
                try {
                    byte[] bytes = getImageBytes(data);
                    uploadImageToStorage(bytes);
                } catch (Exception e) {
                    Log.d(TAG, "Failed to upload image");
                }
            }
        }
    }

    private byte[] getImageBytes(Intent data) throws FileNotFoundException {
        InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
        Bitmap bitmapOfNewMarker = BitmapFactory.decodeStream(inputStream);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapOfNewMarker.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    private void uploadImageToStorage(byte[] bytes) {
        StorageReference landmarkImageRef = mStorage.child("groups/" + keyChat + "/" + "images/avatarGroup.jpeg");

        UploadTask uploadTask = landmarkImageRef.putBytes(bytes);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "Failed to upload image");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            }
        });
    }
}