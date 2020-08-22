package com.apcs.mofs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ActivityAddALandMark extends AppCompatActivity {
    EditText editTextTitle;
    EditText editTextDescription;
    Button btnInsertImage;
    GridView gridViewImgs;
    Button btnAddPlace;
    Button btnCancel;

    private ArrayList<imageItem>_item;
    private GridViewArrayAdapter _adapter;
    private imageItem itemSelected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_a_land_mark);

        getView();
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
            _adapter = new GridViewArrayAdapter(ActivityAddALandMark.this,R.layout.gridview_image_item,_item);
            gridViewImgs.setAdapter(_adapter);
        }
    }
    private void controlClicking() {
        //Btn Insert Img
        controlButtonInsertImage();
        //An item in gridView clicked
        itemClicked();
        //Btn Cancel
        controlButtonCancel();
        //Btn Add Place
        controlButtonAddPlace();

    }

    private void controlButtonAddPlace() {
        btnAddPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Update landmark to server
                //Trong trường hợp app offline, ta t đưa item về MainActivity(Map) bằng intent

                Intent itemIntent = new Intent();
                itemIntent.putExtra("tit",String.valueOf(editTextTitle.getText()));
                itemIntent.putExtra("des",String.valueOf(editTextDescription.getText()));
                if(itemSelected==null){
                    itemIntent.putExtra("checkNull",1);
                    itemIntent.putExtra("uri",getString(R.string.null_uri).toString());
                    Toast.makeText(getApplicationContext(),String.valueOf(editTextTitle.getText())+"----"+getString(R.string.null_uri).toString(),Toast.LENGTH_SHORT).show();
                }else {
                    itemIntent.putExtra("checkNull",0);
                    itemIntent.putExtra("uri", itemSelected.getUri().toString());
                    Toast.makeText(getApplicationContext(),String.valueOf(editTextDescription.getText())+"----"+itemSelected.getUri().toString(),Toast.LENGTH_SHORT).show();
                }
                setResult(RESULT_OK,itemIntent);
                finish();
            }
        });
    }

    private void controlButtonCancel() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void itemClicked() {
        gridViewImgs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                itemSelected = _item.get(i);
                ArrayList<imageItem> newArrItem = new ArrayList<imageItem>();
                newArrItem.add(itemSelected);
                gridViewImgs.setNumColumns(1);
                gridViewImgs.setAdapter(new GridViewArrayAdapter(ActivityAddALandMark.this,R.layout.gridview_image_item_selected,newArrItem));
            }
        });
    }

    private void controlButtonInsertImage() {
        btnInsertImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                doClickBtnInsertImg();
                btnInsertImage.setEnabled(false);
            }

            private void doClickBtnInsertImg() {
                checkMakeRequestPermission();
                loadImgsToGridView();
            }


            private void checkMakeRequestPermission() {
                int permiss_Storage = ContextCompat.checkSelfPermission(ActivityAddALandMark.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if(permiss_Storage!= PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(ActivityAddALandMark.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
            private void loadImgsToGridView() {
                (new AsyncTaskLoadImage()).execute();
            }
        });
    }


    private void getView() {
        // Bắt id cho các item
        editTextTitle = findViewById(R.id.edTxtTitle);
        editTextDescription = findViewById(R.id.edTxtDescription);
        btnInsertImage = findViewById(R.id.btnInsertImg);
        gridViewImgs = findViewById(R.id.grViewImgs);
        btnAddPlace = findViewById(R.id.addBtn);
        btnCancel = findViewById(R.id.cancelBtn);
    }
}
