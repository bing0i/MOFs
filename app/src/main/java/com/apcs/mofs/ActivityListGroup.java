package com.apcs.mofs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;



public class ActivityListGroup extends AppCompatActivity {
    private static ArrayList<GroupInfo> groupInfoArrayList = new ArrayList<>();
    private ArrayList<GroupInfo> userGroups = new ArrayList<>();
    private GroupAdapter groupAdapter;
    private UserInfo userInfo = new UserInfo();
    private String TAG = "RRRRRRRRRRRRRR";
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private TextView tvUsername = null;
    private TextView tvEmail = null;
    private ImageView ivProfile = null;
    String keyChat = "";
    String groupName = "";
    private DatabaseReference mDatabase;
    private ArrayList<UserInfo> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listgroup_layout);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this,drawerLayout, R.string.Open, R.string.Close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setListView();
        setUserInfo();
        initComponents();
    }

    private void initComponents() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        View header = navigationView.getHeaderView(0);
        tvUsername = (TextView)header.findViewById(R.id.username);
        tvEmail = (TextView)header.findViewById(R.id.email);
        ivProfile = (ImageView)header.findViewById(R.id.profileImage);
        tvUsername.setText(userInfo.getName());
        tvEmail.setText(userInfo.getEmail());
        ivProfile.setImageURI(userInfo.getPhoto());
        keyChat = getIntent().getStringExtra("keyChat");
        groupName = getIntent().getStringExtra("groupName");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        retrieveUsers();
        retrieveGroups();
    }

    private void retrieveGroups() {
        DatabaseReference mGroups = mDatabase.child("groups");
        mGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupInfoArrayList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    readData(new MyCallback() {
                        @Override
                        public void onCallback(ArrayList<String> keys) {
                            for (int i = 0; i < keys.size(); i++) {
                                if (ds.getKey().equals(keys.get(i))) {
                                    for (DataSnapshot ds1 : ds.getChildren()) {
                                        if (ds1.getKey().equals("name")) {
                                            groupInfoArrayList.add(new GroupInfo(ds1.getValue(String.class), keys.get(i)));
                                        }
                                    }
                                }
                            }
                            groupAdapter.notifyDataSetChanged();
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

    private void retrieveUsers() {
        DatabaseReference mUsers = mDatabase.child("users");
        mUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    users.add(new UserInfo(userSnapshot.getKey()));
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actionbar_listgroup, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)){
            return true;
        }
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.newFriend:
                addNewFriend();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addNewFriend() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(this);
        alert.setMessage("Enter username");
        alert.setTitle("New Friend");
        alert.setView(edittext);
        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                //Editable YouEditTextValue = edittext.getText();
                //OR
                String editTextValue = edittext.getText().toString();
                for (int i = 0; i < users.size(); i++) {
                    if (editTextValue.equals(users.get(i).getUsername())) {
                        mDatabase.child("users").child(userInfo.getUsername()).child("friends").child(editTextValue).setValue(true);
                        mDatabase.child("users").child(editTextValue).child("friends").child(userInfo.getUsername()).setValue(true);
                        Toast.makeText(getApplicationContext(), editTextValue + " becomes your friend", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Toast.makeText(getApplicationContext(), "Username does not exist", Toast.LENGTH_SHORT).show();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });
        alert.show();
    }

    private void setListView() {
        ListView listView = (ListView)findViewById(R.id.listViewGroup);
        groupAdapter = new GroupAdapter(this, 0, groupInfoArrayList);
        listView.setAdapter(groupAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                openMapActivity(position);
            }
        });
    }

    private void openMapActivity(int position) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("keyChat", groupInfoArrayList.get(position).getKeyGroup());
        intent.putExtra("username", userInfo.getUsername());
//        intent.putExtra("groupName", groupName);
        startActivity(intent);
    }

    private void setUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String username = user.getEmail().substring(0, user.getEmail().indexOf("@"));
            userInfo.setUsername(username);
            userInfo.setName(user.getDisplayName());
            userInfo.setEmail(user.getEmail());
            userInfo.setPhoto(user.getPhotoUrl());
            // Check if user's email is verified
//            boolean emailVerified = user.isEmailVerified();
            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
//            String uid = user.getUid();
        } else {
//            finish();
        }
    }

    public void navigationClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();
                break;
            case R.id.newGroup:
                Intent intent = new Intent(this, NewGroupActivity.class);
                intent.putExtra("username", userInfo.getUsername());
                startActivity(intent);
                finish();
                break;
            case R.id.friends:
                Intent intent1 = new Intent(this, ActivityListFriend.class);
                intent1.putExtra("username", userInfo.getUsername());
                startActivity(intent1);
                break;
            case R.id.nav_profile:
                Intent intent2 = new Intent(this, ProfileUserActivity.class);
                intent2.putExtra("username", userInfo.getUsername());
                startActivity(intent2);
                break;
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();

        GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                .requestIdToken(getString(R.string.default_web_client_id))
                                                .requestEmail()
                                                .build())
                .signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startActivity(new Intent(getApplicationContext(), GoogleSignInActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Sign Out Failed");
            }
        });
    }

    public interface MyCallback {
        void onCallback(ArrayList<String> keys);
    }

    public void readData(MyCallback myCallback) {
        mDatabase.child("users").child(userInfo.getUsername()).child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> keyChats = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    keyChats.add(ds.getKey());
                }
//                String value = dataSnapshot.getValue(String.class);
                myCallback.onCallback(keyChats);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
