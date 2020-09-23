package com.apcs.mofs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AdapterPlaces extends ArrayAdapter<InfoMarker> {
    private Context context;
    private int layoutId;
    private ArrayList<InfoMarker> markers;
    private final String TAG = "RRRRRRRRRRRRRRRRRRRRRR";
    private final long MAX_SIZE_IMAGE = 10485760; //10MB
    private String keyChat = "";

    public AdapterPlaces(@NonNull Context context, int resource, @NonNull List<InfoMarker> objects, String keyChat) {
        super(context, resource, objects);
        this.context = context;
        layoutId = resource;
        markers = (ArrayList<InfoMarker>) objects;
        this.keyChat = keyChat;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(layoutId, null, false);

            viewHolder = new ViewHolder();

            viewHolder.tvTitle = (TextView)convertView.findViewById(R.id.title);
            viewHolder.tvAddress = (TextView)convertView.findViewById(R.id.address);
            viewHolder.imageView = (ImageView)convertView.findViewById(R.id.imageView);
            viewHolder.progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        InfoMarker marker = markers.get(position);
        viewHolder.tvTitle.setText(marker.getTitle());
        viewHolder.tvAddress.setText(marker.getAddress());
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        setBitmapInfoMarker(viewHolder, marker);

        return convertView;
    }

    private class ViewHolder {
        TextView tvTitle = null;
        TextView tvAddress = null;
        ImageView imageView = null;
        ProgressBar progressBar = null;
    }

    private void setBitmapInfoMarker(ViewHolder viewHolder, InfoMarker infoMarker) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference landmarkImageRef = storage.getReference().child("landmarks/" + keyChat + "/" + infoMarker.getSnippetKey() + "/images/infoWindowImage.jpeg");
        landmarkImageRef.getBytes(MAX_SIZE_IMAGE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
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
                viewHolder.imageView.setImageResource(R.mipmap.ic_launcher);
                viewHolder.progressBar.setVisibility(View.GONE);
//                Toast.makeText(getApplicationContext(), "No such file or path found", Toast.LENGTH_SHORT).show();
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
