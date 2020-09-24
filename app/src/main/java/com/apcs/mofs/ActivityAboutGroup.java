package com.apcs.mofs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ActivityAboutGroup extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private  DatabaseReference mFriends;
    private DatabaseReference mUsers;
    private AdapterFriends adapterMembers;
    private String TAG = "RRRRRRRRRRRRRRRRRRRRRR";
    private ListView listView;
    private TextView textView;
    private String username = "";
    private String keyChat = "";
    private InfoUser infoUser = new InfoUser();
    private ArrayList<InfoUser> members = new ArrayList<>();
    private ArrayList<InfoUser> friends = new ArrayList<>();
    private ArrayList<InfoUser> membersListView = new ArrayList<>();
    private TaskUpdateListViewWithFirebaseData taskUpdateListViewWithFirebaseData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_group);
        initComponents();
    }

    private void initComponents() {
        getSupportActionBar().setTitle("About group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        keyChat = getIntent().getStringExtra("keyChat");
        username = getIntent().getStringExtra("username");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFriends = mDatabase.child("groups").child(keyChat).child("members");
        mUsers = mDatabase.child("users");
        listView = (ListView)findViewById(R.id.listViewAboutGroup);
        taskUpdateListViewWithFirebaseData = new TaskUpdateListViewWithFirebaseData("Friends", mFriends, mUsers,
                listView, R.layout.item_friend_with_delete_button, keyChat, "group", getApplicationContext());
        taskUpdateListViewWithFirebaseData.updateListViewFriends();
        listView = taskUpdateListViewWithFirebaseData.getListView();
        adapterMembers = taskUpdateListViewWithFirebaseData.getAdapterFriends();
        membersListView = taskUpdateListViewWithFirebaseData.getListToUpdateListViewFriends();
        textView = (TextView)findViewById(R.id.group_name);
        setTextViewGroupName();
        retrieveFriends();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTextViewGroupName() {
        DatabaseReference mRefGroupName = mDatabase.child("groups")
                .child(keyChat).child("name");
        mRefGroupName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                textView.setText(dataSnapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    public void navigationBottomClicked(View view) {
        switch (view.getId()) {
            case R.id.map:
                finish();
                break;
            case R.id.addMember:
                showDialogNewFriend();
                break;
        }
    }

    public void showDialogNewFriend() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityAboutGroup.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(ActivityAboutGroup.this).inflate(R.layout.dialog_new_friend, (ConstraintLayout)findViewById(R.id.layout));
        builder.setView(view);
        ((TextView)view.findViewById(R.id.title)).setText(getResources().getString(R.string.newMember));
        ((TextView)view.findViewById(R.id.message)).setText(getResources().getString(R.string.newFriendMessage));
        ((Button)view.findViewById(R.id.buttonNo)).setText(getResources().getString(R.string.cancel));
        ((Button)view.findViewById(R.id.buttonYes)).setText(getResources().getString(R.string.add));
        ((ImageView)view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_person_add);
        EditText editText = (EditText)view.findViewById(R.id.editText);

        AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        view.findViewById(R.id.buttonYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = editText.getText().toString();
                if (user.equals(username)) {
                    Toast.makeText(getApplicationContext(), "Unfortunately, you cannot add yourself :(.", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                    return;
                }
                for (int i = 0; i < membersListView.size(); i++) {
                    if (user.equals(membersListView.get(i).getUsername())) {
                        Toast.makeText(getApplicationContext(), "Your friend is already here.", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                        return;
                    }
                }
                for (int i = 0; i < friends.size(); i++) {
                    if (user.equals(friends.get(i).getUsername())) {
                        mDatabase.child("groups").child(keyChat).child("members").child(user).setValue(true);
                        taskUpdateListViewWithFirebaseData = new TaskUpdateListViewWithFirebaseData("Friends", mFriends, mUsers,
                                listView, R.layout.item_friend_with_delete_button, keyChat, "group", getApplicationContext());
                        taskUpdateListViewWithFirebaseData.updateListViewFriends();
                        alertDialog.dismiss();
                        return;
                    }
                }
                editText.setText("");
                Toast.makeText(getApplicationContext(), "Did you forget your friend's name?", Toast.LENGTH_SHORT).show();
                return;
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    private void retrieveFriends() {
        DatabaseReference mUsers = mDatabase.child("users").child(username).child("friends");
        mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    friends.add(new InfoUser(userSnapshot.getKey()));
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void buttonLeaveGroupClicked(View view) {
        DatabaseReference databaseLandmarkRef = FirebaseDatabase.getInstance().getReference().child("groups").child(keyChat).child("members").child(username);
        databaseLandmarkRef.removeValue();
        databaseLandmarkRef = FirebaseDatabase.getInstance().getReference().child("users").child(username).child("groups").child(keyChat);
        databaseLandmarkRef.removeValue();
        Intent intent = new Intent(ActivityAboutGroup.this, ActivityGroups.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
