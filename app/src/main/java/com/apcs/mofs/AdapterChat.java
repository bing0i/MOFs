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
    public AdapterChat(@NonNull Context context, int resource, @NonNull List<InfoMessage> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.item_message, null);

            viewHolder = new ViewHolder();

            viewHolder.tvName = (TextView)convertView.findViewById(R.id.name);
            viewHolder.tvMessage = (TextView)convertView.findViewById(R.id.message);
            viewHolder.imageView = (ImageView)convertView.findViewById(R.id.profileImage);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        InfoMessage infoMessage = getItem(position);
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
