package com.apcs.mofs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AdapterFriends extends ArrayAdapter<InfoUser> {
    private int layout = 1;
    private String key = "";
    private List<InfoUser> objects = null;
    private String flag = "";

    public AdapterFriends(@NonNull Context context, int resource, @NonNull List<InfoUser> objects){
        super(context, resource, objects);
        layout = resource;
        this.objects = objects;
    }

    public AdapterFriends(@NonNull Context context, int resource, @NonNull List<InfoUser> objects, String key, String flag){
        super(context, resource, objects);
        layout = resource;
        this.key = key;
        this.objects = objects;
        this.flag = flag;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder viewHolder = null;
        if (convertView == null) {
            if (layout == 0)
                layout = R.layout.item_friend;
            convertView = LayoutInflater.from(this.getContext()).inflate(layout, null);

            viewHolder = new ViewHolder();

            viewHolder.textViewUserName = (TextView)convertView.findViewById(R.id.username);
            viewHolder.imageAva = (ImageView)convertView.findViewById(R.id.profilePhoto);
            if (layout == R.layout.item_friend_with_delete_button)
                viewHolder.buttonDelete = (ImageButton)convertView.findViewById(R.id.buttonDelete);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        InfoUser infoUser = getItem(position);
        viewHolder.textViewUserName.setText(infoUser.getUsername());
        viewHolder.imageAva.setImageBitmap(infoUser.getBitmap());
        if (layout == R.layout.item_friend_with_delete_button) {
            viewHolder.buttonDelete.setTag(String.valueOf(infoUser.getUsername()));
            if  (flag.equals("friends")) {
                viewHolder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String friend = view.getTag().toString();
                        DatabaseReference databaseLandmarkRef = FirebaseDatabase.getInstance().getReference().child("users").child(key).child("friends").child(friend);
                        databaseLandmarkRef.removeValue();
                        databaseLandmarkRef = FirebaseDatabase.getInstance().getReference().child("users").child(friend).child("friends").child(key);
                        databaseLandmarkRef.removeValue();
                        objects.remove(position);
                        notifyDataSetChanged();
                    }
                });
            } else if (flag.equals("group")) {
                viewHolder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String user = view.getTag().toString();
                        DatabaseReference databaseLandmarkRef = FirebaseDatabase.getInstance().getReference().child("groups").child(key).child("members").child(user);
                        databaseLandmarkRef.removeValue();
                        objects.remove(position);
                        notifyDataSetChanged();
                    }
                });
            }
        }

        return convertView;
    }

    private class ViewHolder {
        ImageView imageAva = null;
        TextView textViewUserName = null;
        ImageButton buttonDelete = null;
    }
}
