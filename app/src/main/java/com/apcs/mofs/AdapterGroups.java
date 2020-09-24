package com.apcs.mofs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class AdapterGroups extends ArrayAdapter<InfoGroup> {
    private final long MAX_SIZE_IMAGE = 10485760; //10MB
    private String TAG = "RRRRRRRRRRRRRRRRR";

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
            viewHolder.imageView = (ImageView)convertView.findViewById(R.id.imageAva);
            viewHolder.progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        InfoGroup infoGroup = getItem(position);
        viewHolder.textViewGroupName.setText(infoGroup.getGroupName());
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        setBitmapAvatarGroup(viewHolder, infoGroup);

        return convertView;
    }

    private class ViewHolder {
        ImageView imageView = null;
        TextView textViewGroupName = null;
        ProgressBar progressBar = null;
    }

    private void setBitmapAvatarGroup(ViewHolder viewHolder, InfoGroup infoGroup) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference AvatarRef = storage.getReference().child("groups/" + infoGroup.getKeyGroup() + "/images/avatarGroup.jpeg");
        AvatarRef.getBytes(MAX_SIZE_IMAGE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = getBitmap(bytes);
                viewHolder.imageView.setImageBitmap(bitmap);
                viewHolder.progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "No such file or path found");
                viewHolder.imageView.setImageResource(infoGroup.getDefaultPhoto());
                viewHolder.imageView.setTag(infoGroup.getDefaultPhoto());
                viewHolder.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private Bitmap getBitmap(byte[] bytes) {
        Bitmap srcBmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){
            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight());
        } else {
            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth());
        }
        return Bitmap.createScaledBitmap(dstBmp, 200, 200, true);
    }
}
