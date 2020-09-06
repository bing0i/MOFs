package com.apcs.mofs;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;

public class ActivityMap extends AppCompatActivity implements OnMapReadyCallback{
    //Mapbox
    private MapView mapView;

    //Database
    private DatabaseReference mDatabase;
    private String keyChat = "";
    private String username = "";
    private String TAG = "RRRRRRRRRRRRRRRRRRRRRR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);

        initComponents(savedInstanceState);
    }

    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        showMarkers(mapboxMap);
//        mapboxMap.addMarker(new MarkerOptions()
//                .position(new LatLng(10.764051, 106.682000))
//                .title("HCMUS"));
        mapboxMap.setOnInfoWindowLongClickListener(new MapboxMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(@NonNull Marker marker) {
                ArrayList<String> info = new ArrayList<>();
                info.add("Edit Marker");
                info.add(marker.getTitle());
                info.add("");
//                alertInfoWindowMaker(info, mapboxMap, null, marker);
                showDialogEditMarker(mapboxMap, null, marker);
            }
        });
        mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng point) {
                ArrayList<String> info = new ArrayList<>();
                info.add("Add Marker");
                info.add("Enter Maker Title");
                info.add("Enter Marker Description");
//                alertInfoWindowMaker(info, mapboxMap, point, null);
                showDialogAddMarker(mapboxMap, point);
            }
        });
    }

    private void alertInfoWindowMaker(ArrayList<String> info, MapboxMap mapboxMap, LatLng point, Marker marker) {
        LinearLayout layout = new LinearLayout(getApplicationContext());

        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText edittextTitle = getEditText(info.get(1));
        final EditText edittextDescription = getEditText(info.get(2));

        layout.addView(edittextTitle);
        layout.addView(edittextDescription);

        AlertDialog.Builder alert = new AlertDialog.Builder(ActivityMap.this);
//        alert.setMessage("Enter username");
        alert.setTitle(info.get(0));
        alert.setView(layout);
        alert.setPositiveButton(info.get(0), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String editTextTitle = edittextTitle.getText().toString();
                String editTextDescription = edittextDescription.getText().toString();
                if (point != null) {
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(point)
                            .title(editTextTitle));
                    sendLandmarksToDatabase(new InfoMarker(editTextTitle, editTextDescription, point, null));
                }
                else
                    marker.setTitle(editTextTitle);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        if (info.get(0).equals("Add Marker")) {
            alert.show();
            return;
        }
        alert.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                marker.remove();
            }
        });
        alert.show();
    }

    private void showDialogEditMarker(MapboxMap mapboxMap, LatLng point, Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMap.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(ActivityMap.this).inflate(R.layout.dialog_edit_marker, (ConstraintLayout)findViewById(R.id.dialogContainer));
        builder.setView(view);
        ((TextView)view.findViewById(R.id.title)).setText(getResources().getString(R.string.detailsOfTheMarker));
        ((TextView)view.findViewById(R.id.message1)).setText(getResources().getString(R.string.markerName));
        ((TextView)view.findViewById(R.id.message2)).setText(getResources().getString(R.string.markerDescription));
        ((Button)view.findViewById(R.id.buttonNo)).setText(getResources().getString(R.string.cancel));
        ((Button)view.findViewById(R.id.buttonYes)).setText(getResources().getString(R.string.edit));
        ((Button)view.findViewById(R.id.buttonDelete)).setText(getResources().getString(R.string.delete));
        ((ImageView)view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_baseline_location_on_24);
        EditText editText1 = (EditText)view.findViewById(R.id.editText1);
        editText1.setText(marker.getTitle());
        EditText editText2 = (EditText)view.findViewById(R.id.editText2);

        AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        view.findViewById(R.id.buttonYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editTextTitle = editText1.getText().toString();
                String editTextDescription = editText2.getText().toString();
                marker.setTitle(editTextTitle);
                alertDialog.dismiss();
            }
        });

        view.findViewById(R.id.buttonDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                marker.remove();
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    private void showDialogAddMarker(MapboxMap mapboxMap, LatLng point) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMap.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(ActivityMap.this).inflate(R.layout.dialog_add_marker, (ConstraintLayout)findViewById(R.id.dialogContainer));
        builder.setView(view);
        ((TextView)view.findViewById(R.id.title)).setText(getResources().getString(R.string.detailsOfTheMarker));
        ((TextView)view.findViewById(R.id.message1)).setText(getResources().getString(R.string.markerName));
        ((TextView)view.findViewById(R.id.message2)).setText(getResources().getString(R.string.markerDescription));
        ((Button)view.findViewById(R.id.buttonNo)).setText(getResources().getString(R.string.cancel));
        ((Button)view.findViewById(R.id.buttonYes)).setText(getResources().getString(R.string.add));
        ((ImageView)view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_baseline_location_on_24);
        EditText editText1 = (EditText)view.findViewById(R.id.editText1);
        EditText editText2 = (EditText)view.findViewById(R.id.editText2);

        AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        view.findViewById(R.id.buttonYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editTextTitle = editText1.getText().toString();
                String editTextDescription = editText2.getText().toString();
                if (point != null) {
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(point)
                            .title(editTextTitle));
                    sendLandmarksToDatabase(new InfoMarker(editTextTitle, editTextDescription, point, null));
                }
                alertDialog.dismiss();
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    private EditText getEditText(String hint) {
        Resources r = this.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                20,
                r.getDisplayMetrics()
        );
        final EditText edittext = new EditText(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(px, px, px, 0);
        edittext.setLayoutParams(lp);
        edittext.setHint(hint);
        return edittext;
    }

    private void showMarkers(MapboxMap mapboxMap) {
        readData(new ActivityMap.MyCallback() {
            @Override
            public void onCallback(ArrayList<InfoMarker> infoMarkers) {
                ArrayList<MarkerOptions> markerOptions = new ArrayList<>();
                for (int i = 0; i < infoMarkers.size(); i++) {
                    markerOptions.add(new MarkerOptions()
                                        .position(infoMarkers.get(i).getLatLong())
                                        .title(infoMarkers.get(i).getTitle()));
                }
                mapboxMap.addMarkers(markerOptions);
            }
        });
    }

    private void initComponents(Bundle savedInstanceState) {
        keyChat = getIntent().getStringExtra("keyChat");
        username = getIntent().getStringExtra("username");
//      String groupName = getIntent().getStringExtra("groupName");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Mapbox
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actionbar_activity_map,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.addPlaceMenu:
                break;
            case R.id.nav_about_group:
                openActivityAboutGroup();
                break;
            case R.id.nav_group_mess:
                openChatActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openActivityAboutGroup() {
        Intent intent = new Intent(this, ActivityAboutGroup.class);
        intent.putExtra("keyChat", keyChat);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void openChatActivity() {
        Intent intent = new Intent(this, ActivityChat.class);
        intent.putExtra("keyChat", keyChat);
        intent.putExtra("username", username);
        intent.putExtra("photoProfile", getIntent().getStringExtra("photoProfile"));
//        intent.putExtra("groupName", groupName);
        startActivity(intent);
    }

    private void sendLandmarksToDatabase(InfoMarker infoMarker) {
        String key = mDatabase.child("landmarks").child(keyChat).push().getKey();
        mDatabase.child("landmarks").child(keyChat).child(key).child("longitude").setValue(infoMarker.getLatLong().getLongitude());
        mDatabase.child("landmarks").child(keyChat).child(key).child("latitude").setValue(infoMarker.getLatLong().getLatitude());
        mDatabase.child("landmarks").child(keyChat).child(key).child("logoID").setValue(infoMarker.getLogoId());
        mDatabase.child("landmarks").child(keyChat).child(key).child("title").setValue(infoMarker.getTitle());
        mDatabase.child("landmarks").child(keyChat).child(key).child("description").setValue(infoMarker.getDescription());
        mDatabase.child("landmarks").child(keyChat).child(key).child("uri").setValue(String.valueOf(infoMarker.getUri()));
    }

    public interface MyCallback {
        void onCallback(ArrayList<InfoMarker> infoMarkers);
    }

    public void readData(ActivityMap.MyCallback myCallback) {
        DatabaseReference mGroups = mDatabase.child("landmarks").child(keyChat);
        mGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<InfoMarker> infoMarkers = new ArrayList<>();
                InfoMarker infoMarker = new InfoMarker();
                LatLng latLng = new LatLng();
                for (DataSnapshot landmarkSnapshot: dataSnapshot.getChildren()) {
                    for (DataSnapshot metaSnapshot: landmarkSnapshot.getChildren()) {
                        if (metaSnapshot.getKey().equals("latitude")) {
                            latLng.setLatitude(metaSnapshot.getValue(Double.class));
                        } else if (metaSnapshot.getKey().equals("longitude")) {
                            latLng.setLongitude(metaSnapshot.getValue(Double.class));
                        } else if (metaSnapshot.getKey().equals("logoID")) {
                            infoMarker.setLogoId(metaSnapshot.getValue(Integer.class));
                        } else if (metaSnapshot.getKey().equals("title")) {
                            infoMarker.setTitle(metaSnapshot.getValue(String.class));
                        } else if (metaSnapshot.getKey().equals("description")) {
                            infoMarker.setDescription(metaSnapshot.getValue(String.class));
                        } else if (metaSnapshot.getKey().equals("uri")) {
                            infoMarker.setUri(Uri.parse(metaSnapshot.getValue(String.class)));
                        }
                    }
                    infoMarker.setLatLong(latLng);
                    infoMarkers.add(infoMarker);
                    infoMarker = new InfoMarker();
                    latLng = new LatLng();
                }
                myCallback.onCallback(infoMarkers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
