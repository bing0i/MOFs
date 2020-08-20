package com.apcs.mofs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ActivityMapOfGroup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_of_group);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_actionbar_map_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_about_group:
                openActivityAboutGroup();
                break;
            case R.id.nav_group_mess:
                openChatActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openActivityAboutGroup() {
        Intent intent = new Intent(this, ActivityAboutGroup.class);
        startActivity(intent);
    }

    private void openChatActivity() {
        Intent intent = new Intent(this, ChatActivity.class);
        String groupName = getIntent().getStringExtra("groupName");
        intent.putExtra("groupName", groupName);
        startActivity(intent);
    }
}