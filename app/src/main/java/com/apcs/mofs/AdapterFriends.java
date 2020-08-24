package com.apcs.mofs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import java.util.List;

public class AdapterFriends extends ArrayAdapter<InfoUser> {
    public AdapterFriends(@NonNull Context context, int resource, @NonNull List<InfoUser> objects){
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.item_friend, null);

            viewHolder = new ViewHolder();

            viewHolder.textViewUserName = (TextView)convertView.findViewById(R.id.username);
            viewHolder.imageAva = (ImageView)convertView.findViewById(R.id.profilePhoto);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        InfoUser infoUser = getItem(position);
        viewHolder.textViewUserName.setText(infoUser.getUsername());
        viewHolder.imageAva.setImageBitmap(infoUser.getBitmap());

        return convertView;
    }

    private class ViewHolder {
        ImageView imageAva = null;
        TextView textViewUserName =null;
    }
}
