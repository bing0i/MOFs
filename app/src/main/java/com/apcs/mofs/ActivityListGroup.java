package com.apcs.mofs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;



public class ActivityListGroup extends AppCompatActivity {
    public static ArrayList<GroupInfo> groupInfoArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listgroup_layout);

        initializeArray();
        setListView();
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_actionbar, menu);
//        return true;
//    }

    private void setListView() {
        ListView listView = (ListView)findViewById(R.id.listViewGroup);
        final GroupAdapter groupAdapter = new GroupAdapter(this, 0, groupInfoArrayList);
        listView.setAdapter(groupAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                openActivityMapOfGroup(position);

            }
        });
    }

    public static void initializeArray() {
        groupInfoArrayList.add(new GroupInfo(R.drawable.pudle_toy, "asl"));
        groupInfoArrayList.add(new GroupInfo(R.drawable.husky, "Hội Husky"));
    }

    private void openActivityMapOfGroup(int position) {
        Intent intent = new Intent(this, ActivityMapOfGroup.class);
        intent.putExtra("groupName", groupInfoArrayList.get(position)._textViewGroupName);
        startActivity(intent);
    }

}
