package com.apcs.mofs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;



public class ActivityGroups extends AppCompatActivity {
    private static ArrayList<InfoGroup> infoGroupArrayList = new ArrayList<>();
    private ArrayList<InfoGroup> userGroups = new ArrayList<>();
    private AdapterGroups adapterGroups;
    private InfoUser infoUser = new InfoUser();
    private String TAG = "RRRRRRRRRRRRRR";
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private TextView tvUsername = null;
    private TextView tvEmail = null;
    String keyChat = "";
    String groupName = "";
    private DatabaseReference mDatabase;
    private ArrayList<InfoUser> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this,drawerLayout, R.string.Open, R.string.Close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initComponents();
    }

    private void initComponents() {
        setListView();
        setUserInfo();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        View header = navigationView.getHeaderView(0);
        tvUsername = (TextView)header.findViewById(R.id.username);
        tvEmail = (TextView)header.findViewById(R.id.email);
        tvUsername.setText(infoUser.getName());
        tvEmail.setText(infoUser.getEmail());
        keyChat = getIntent().getStringExtra("keyChat");
        groupName = getIntent().getStringExtra("groupName");
        new RetrieveBitmapTask().execute(infoUser.getUri().toString());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        retrieveUsers();
        retrieveGroups();
    }

    private class RetrieveBitmapTask extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... urls) {
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
            return bm;
        }

        protected void onPostExecute(Bitmap bm) {
            NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
            View header = navigationView.getHeaderView(0);
            ImageView ivProfile = (ImageView)header.findViewById(R.id.profileImage);
            ivProfile.setImageBitmap(bm);
        }
    }

    private void retrieveGroups() {
        DatabaseReference mGroups = mDatabase.child("groups");
        mGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                infoGroupArrayList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    readData(new MyCallback() {
                        @Override
                        public void onCallback(ArrayList<String> keys) {
                            for (int i = 0; i < keys.size(); i++) {
                                if (ds.getKey().equals(keys.get(i))) {
                                    for (DataSnapshot ds1 : ds.getChildren()) {
                                        if (ds1.getKey().equals("name")) {
                                            infoGroupArrayList.add(new InfoGroup(ds1.getValue(String.class), keys.get(i)));
                                        }
                                    }
                                }
                            }
                            adapterGroups.notifyDataSetChanged();
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
                    users.add(new InfoUser(userSnapshot.getKey()));
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
        getMenuInflater().inflate(R.menu.menu_actionbar_activity_groups, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)){
            return true;
        }
        switch (item.getItemId()) {
            //Notification
//            case R.id.newFriend:
//                showDialogNewFriend();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showDialogNewFriend() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityGroups.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(ActivityGroups.this).inflate(R.layout.dialog_new_friend, (ConstraintLayout)findViewById(R.id.dialogContainer));
        builder.setView(view);
        ((TextView)view.findViewById(R.id.title)).setText(getResources().getString(R.string.newFriend));
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
                String editTextValue = editText.getText().toString();
                if (editTextValue.equals(infoUser.getUsername())) {
                    Toast.makeText(getApplicationContext(), "Unfortunately, you cannot befriend with yourself :(.", Toast.LENGTH_SHORT).show();
                    editText.setText("");
                    return;
                }
                for (int i = 0; i < users.size(); i++) {
                    if (editTextValue.equals(users.get(i).getUsername())) {
                        mDatabase.child("users").child(infoUser.getUsername()).child("friends").child(editTextValue).setValue(true);
                        mDatabase.child("users").child(editTextValue).child("friends").child(infoUser.getUsername()).setValue(true);
                        Toast.makeText(getApplicationContext(), editTextValue + " and you are friends from now on!", Toast.LENGTH_SHORT).show();
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

    private void setListView() {
        ListView listView = (ListView)findViewById(R.id.listViewGroup);
        adapterGroups = new AdapterGroups(this, 0, infoGroupArrayList);
        listView.setAdapter(adapterGroups);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                openMapActivity(position);
            }
        });
    }

    private void openMapActivity(int position) {
        Intent intent = new Intent(this, ActivityMap.class);
        intent.putExtra("keyChat", infoGroupArrayList.get(position).getKeyGroup());
        intent.putExtra("username", infoUser.getUsername());
        intent.putExtra("photoProfile", infoUser.getUri().toString());
//        intent.putExtra("groupName", groupName);
        startActivity(intent);
    }

    private void setUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String username = user.getEmail().substring(0, user.getEmail().indexOf("@"));
            infoUser.setUsername(username);
            infoUser.setName(user.getDisplayName());
            infoUser.setEmail(user.getEmail());
            infoUser.setUri(user.getPhotoUrl());
        } else {
//            finish();
        }
    }

    public void navigationClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();
                break;
            case R.id.newFriend:
                showDialogNewFriend();
                break;
            case R.id.newGroup:
                Intent intent = new Intent(this, ActivityNewGroup.class);
                intent.putExtra("username", infoUser.getUsername());
                startActivity(intent);
                finish();
                break;
            case R.id.friends:
                Intent intent1 = new Intent(this, ActivityFriends.class);
                intent1.putExtra("username", infoUser.getUsername());
                startActivity(intent1);
                break;
            case R.id.nav_profile:
                Intent intent2 = new Intent(this, ActivityProfile.class);
                intent2.putExtra("username", infoUser.getUsername());
                intent2.putExtra("email", infoUser.getEmail());
                intent2.putExtra("name", infoUser.getName());
                intent2.putExtra("photoProfile", infoUser.getUri().toString());
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
                startActivity(new Intent(getApplicationContext(), ActivityGoogleSignIn.class));
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
        mDatabase.child("users").child(infoUser.getUsername()).child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
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
