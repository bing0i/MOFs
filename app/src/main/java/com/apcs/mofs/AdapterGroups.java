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

public class AdapterGroups extends ArrayAdapter<InfoGroup> {
    public AdapterGroups(@NonNull Context context, int resource, @NonNull List<InfoGroup> objects){
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.item_group, null);

            viewHolder = new ViewHolder();

            viewHolder.textViewGroupName = (TextView)convertView.findViewById(R.id.textGroupName);
            viewHolder.imageAva = (ImageView)convertView.findViewById(R.id.imageAva);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        InfoGroup infoGroup = getItem(position);
        viewHolder.textViewGroupName.setText(infoGroup.getGroupName());
        viewHolder.imageAva.setImageResource(infoGroup.getDefaultPhoto());
        viewHolder.imageAva.setTag(infoGroup.getDefaultPhoto());

        return convertView;
    }

    private class ViewHolder {
        ImageView imageAva = null;
        TextView textViewGroupName =null;
    }
}
