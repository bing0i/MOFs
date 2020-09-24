package com.apcs.mofs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AdapterChat extends ArrayAdapter<InfoMessage> {
    private String username;
    private int resource;
    public AdapterChat(@NonNull Context context, int resource, @NonNull List<InfoMessage> objects, String username) {
        super(context, resource, objects);
        this.username = username;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = null;
        InfoMessage infoMessage = getItem(position);
        if (infoMessage.getName().equals(username))
            resource = R.layout.item_message_user;
        else
            resource = R.layout.item_message;
        convertView = LayoutInflater.from(this.getContext()).inflate(resource, null);

        viewHolder = new ViewHolder();

        viewHolder.tvName = (TextView)convertView.findViewById(R.id.name);
        viewHolder.tvMessage = (TextView)convertView.findViewById(R.id.message);
        viewHolder.imageView = (ImageView)convertView.findViewById(R.id.profileImage);

        convertView.setTag(viewHolder);

        viewHolder.tvName.setText(infoMessage.getName());
        viewHolder.tvMessage.setText(infoMessage.getMessage());
        viewHolder.imageView.setImageBitmap(infoMessage.getBitmap());
        viewHolder.imageView.setTag(infoMessage.getImagePath());

        return convertView;
    }

    private class ViewHolder {
        TextView tvName = null;
        TextView tvMessage = null;
        ImageView imageView = null;
    }
}
