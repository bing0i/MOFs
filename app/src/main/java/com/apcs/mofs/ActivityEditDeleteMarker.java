package com.apcs.mofs;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.Manifest;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class ActivityEditDeleteMarker extends AppCompatActivity {
    private EditText edTxtTitle;
    private EditText edTxtDes;
    private Button btnInsertImg;
    private GridView grViewImgs;
    private Button btnSave;
    private Button btnDel;
    private Button btnCancel;
    private boolean isDel = false;
    Landmark landmark; //Luu lai landmark cần edit or del

    private ArrayList<imageItem>_item;
    private GridViewArrayAdapter _adapter;
    private imageItem itemSelected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_delete_marker);

        setView();
        loadData();
        controlClicking();
    }

    private class AsyncTaskLoadImage extends AsyncTask<Void,Void, List<imageItem>>{
        @Override
        protected List<imageItem> doInBackground(Void... voids) {
            ArrayList<imageItem> results = new ArrayList<>();

            String[] projection = new String[] {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,

            };
            String sortOrder = MediaStore.Images.Media.DISPLAY_NAME + " ASC";

            try (Cursor cursor = getApplicationContext().getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    sortOrder
            )) {
                // Cache column indices.
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int nameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);


                while (cursor.moveToNext()) {
                    // Get values of columns for a given video.
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);

                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    imageItem item = new imageItem(contentUri,name);

                    results.add(item);
                }
            }

            return results;
        }

        @Override
        protected void onPostExecute(List<imageItem> imageItems) {
            _item = (ArrayList<imageItem>) imageItems;
            Log.d("@#@#@#@#",String.valueOf(_item.size()));
            _adapter = new GridViewArrayAdapter(ActivityEditDeleteMarker.this,R.layout.gridview_image_item,_item);
            grViewImgs.setAdapter(_adapter);
        }
    }

    private void loadData() {
        Intent intent = getIntent();
        landmark = new Landmark();
        landmark.setTitle(intent.getStringExtra("tit"));
        landmark.setDescription(intent.getStringExtra("des"));
        landmark.setLatlong(new LatLng(intent.getDoubleExtra("lat",122.0042),intent.getDoubleExtra("long",-30.4120)));
        int isNullUri = intent.getIntExtra("checkUriNull",9);
        if(isNullUri == 0){
            landmark.setUri(Uri.parse(intent.getStringExtra("uri")));
            itemSelected =new imageItem(Uri.parse(intent.getStringExtra("uri")),null);
            grViewImgs.setNumColumns(1);
            ArrayList<imageItem> newArr = new ArrayList<imageItem>();
            newArr.add(itemSelected);
            grViewImgs.setAdapter(new GridViewArrayAdapter(ActivityEditDeleteMarker.this,R.layout.gridview_image_item_selected,newArr));
        } else{

        }
        edTxtTitle.setText(landmark.getTitle());
        edTxtDes.setText(landmark.getDescription());

    }
    //-------------------------------------

    private void controlClicking() {
        //Btn InsertImg
        btnInsertImgCtroler();
        //An item in gridView clicked
        selectedItem();
        //Btn Cancel
        btnCancelCtroler();
        //Btn delete
        btnDeleteCtroler();
        //Btn Save
        btnSaveCtroler();

    }

    private void btnDeleteCtroler() {
        btnDel.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(ActivityEditDeleteMarker.this)
                        .setTitle("Delete this Marker!")
                        .setMessage("This action can not undo. Are you sure?")
                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteMarker();
                            }
                        }).setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).create();
                alertDialog.show();
            }
        });

    }

    private void deleteMarker() {
        // Do delete Marker later
        //Nho finish!!
        isDel =true;
        sendIntent();
        finish();
    }

    private void btnSaveCtroler() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Update landmark to server
                //Trong trường hợp app offline, ta t đưa item về MainActivity(Map) bằng intent

                sendIntent();
                finish();
            }
        });
    }

    private void sendIntent() {
        Intent itemIntent = new Intent();
        itemIntent.putExtra("tit",String.valueOf(edTxtTitle.getText()));
        itemIntent.putExtra("des",String.valueOf(edTxtDes.getText()));
        itemIntent.putExtra("lat",landmark.getLatlong().getLatitude());
        itemIntent.putExtra("long",landmark.getLatlong().getLongitude());
        if(isDel == false){
            itemIntent.putExtra("isDel",0);
        }
        else{
            itemIntent.putExtra("isDel",1);
        }
        if(itemSelected==null){
            itemIntent.putExtra("checkNull",1);
            itemIntent.putExtra("uri",getString(R.string.null_uri).toString());
            Toast.makeText(getApplicationContext(),String.valueOf(edTxtTitle.getText())+"----"+getString(R.string.null_uri).toString(),Toast.LENGTH_SHORT).show();
        }else {
            itemIntent.putExtra("checkNull",0);
            itemIntent.putExtra("uri", itemSelected.getUri().toString());
            Toast.makeText(getApplicationContext(),String.valueOf(edTxtDes.getText())+"----"+itemSelected.getUri().toString(),Toast.LENGTH_SHORT).show();
        }
        setResult(RESULT_OK,itemIntent);
    }

    private void btnCancelCtroler() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void selectedItem() {
        grViewImgs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                itemSelected = _item.get(i);
                ArrayList<imageItem> newArrItem = new ArrayList<imageItem>();
                newArrItem.add(itemSelected);
                grViewImgs.setNumColumns(1);
                grViewImgs.setAdapter(new GridViewArrayAdapter(ActivityEditDeleteMarker.this,R.layout.gridview_image_item_selected,newArrItem));
            }
        });
    }

    private void btnInsertImgCtroler() {
        btnInsertImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                grViewImgs.setNumColumns(3);
                doClickBtnInsertImg();
                btnInsertImg.setEnabled(false);
            }

            private void doClickBtnInsertImg() {
                checkMakeRequestPermission();
                loadImgsToGridView();
            }


            private void checkMakeRequestPermission() {
                int permiss_Storage = ContextCompat.checkSelfPermission(ActivityEditDeleteMarker.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if(permiss_Storage!= PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(ActivityEditDeleteMarker.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
            private void loadImgsToGridView() {
                (new ActivityEditDeleteMarker.AsyncTaskLoadImage()).execute();
            }
        });
    }





    //-------------

    private void setView() {
        edTxtTitle = findViewById(R.id.edTxtTitle);
        edTxtDes = findViewById(R.id.edTxtDescription);
        btnInsertImg = findViewById(R.id.btnInsertImg);
        grViewImgs = findViewById(R.id.grViewImgs);
        btnSave = findViewById(R.id.saveBtn);
        btnCancel = findViewById(R.id.cancelBtn);
        btnDel = findViewById(R.id.deleteBtn);

    }
}