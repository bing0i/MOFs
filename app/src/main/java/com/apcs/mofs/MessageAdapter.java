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

public class MessageAdapter extends ArrayAdapter<MessageInfo> {
    public MessageAdapter(@NonNull Context context, int resource, @NonNull List<MessageInfo> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.message_item, null);

            viewHolder = new ViewHolder();

            viewHolder.tvName = (TextView)convertView.findViewById(R.id.name);
            viewHolder.tvMessage = (TextView)convertView.findViewById(R.id.message);
            viewHolder.imageView = (ImageView)convertView.findViewById(R.id.profileImage);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        MessageInfo messageInfo = getItem(position);
        viewHolder.tvName.setText(messageInfo.getName());
        viewHolder.tvMessage.setText(messageInfo.getMessage());
        viewHolder.imageView.setImageBitmap(messageInfo.getBitmap());
        viewHolder.imageView.setTag(messageInfo.getImagePath());

        return convertView;
    }

    private class ViewHolder {
        TextView tvName = null;
        TextView tvMessage = null;
        ImageView imageView = null;
    }
}
