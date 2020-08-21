package com.apcs.mofs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends ArrayAdapter<UserInfo> {
    public FriendAdapter(@NonNull Context context, int resource, @NonNull List<UserInfo> objects){
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.friend_item, null);

            viewHolder = new ViewHolder();

            viewHolder.textViewUserName = (TextView)convertView.findViewById(R.id.username);
            viewHolder.imageAva = (ImageView)convertView.findViewById(R.id.profilePhoto);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        UserInfo userInfo = getItem(position);
        viewHolder.textViewUserName.setText(userInfo.getUsername());
        viewHolder.imageAva.setImageURI(userInfo.getPhoto());
        viewHolder.imageAva.setTag(userInfo.getPhoto());

        return convertView;
    }

    private class ViewHolder {
        ImageView imageAva = null;
        TextView textViewUserName =null;
    }
}
