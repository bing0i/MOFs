package com.apcs.mofs;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback{
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
        setContentView(R.layout.map_activity_layout);

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
                alertInfoWindowMaker(info, mapboxMap, null, marker);
            }
        });
        mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng point) {
                ArrayList<String> info = new ArrayList<>();
                info.add("Add Marker");
                info.add("Enter Maker Title");
                info.add("Enter Marker Description");
                alertInfoWindowMaker(info, mapboxMap, point, null);
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

        AlertDialog.Builder alert = new AlertDialog.Builder(MapActivity.this);
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
                    sendLandmarksToDatabase(new Landmark(editTextTitle, editTextDescription, point, null));
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
        readData(new MapActivity.MyCallback() {
            @Override
            public void onCallback(ArrayList<Landmark> landmarks) {
                ArrayList<MarkerOptions> markerOptions = new ArrayList<>();
                for (int i = 0; i < landmarks.size(); i++) {
                    markerOptions.add(new MarkerOptions()
                                        .position(landmarks.get(i).getLatlong())
                                        .title(landmarks.get(i).getTitle()));
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
        getMenuInflater().inflate(R.menu.menu_actionbar_map_activity,menu);
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
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("keyChat", keyChat);
        intent.putExtra("username", username);
        intent.putExtra("photoProfile", getIntent().getStringExtra("photoProfile"));
//        intent.putExtra("groupName", groupName);
        startActivity(intent);
    }

    private void sendLandmarksToDatabase(Landmark landmark) {
        String key = mDatabase.child("landmarks").child(keyChat).push().getKey();
        mDatabase.child("landmarks").child(keyChat).child(key).child("longitude").setValue(landmark.getLatlong().getLongitude());
        mDatabase.child("landmarks").child(keyChat).child(key).child("latitude").setValue(landmark.getLatlong().getLatitude());
        mDatabase.child("landmarks").child(keyChat).child(key).child("logoID").setValue(landmark.getLogoID());
        mDatabase.child("landmarks").child(keyChat).child(key).child("title").setValue(landmark.getTitle());
        mDatabase.child("landmarks").child(keyChat).child(key).child("description").setValue(landmark.getDescription());
        mDatabase.child("landmarks").child(keyChat).child(key).child("uri").setValue(String.valueOf(landmark.getUri()));
    }

    public interface MyCallback {
        void onCallback(ArrayList<Landmark> landmarks);
    }

    public void readData(MapActivity.MyCallback myCallback) {
        DatabaseReference mGroups = mDatabase.child("landmarks").child(keyChat);
        mGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Landmark> landmarks = new ArrayList<>();
                Landmark landmark = new Landmark();
                LatLng latLng = new LatLng();
                for (DataSnapshot landmarkSnapshot: dataSnapshot.getChildren()) {
                    for (DataSnapshot metaSnapshot: landmarkSnapshot.getChildren()) {
                        if (metaSnapshot.getKey().equals("latitude")) {
                            latLng.setLatitude(metaSnapshot.getValue(Double.class));
                        } else if (metaSnapshot.getKey().equals("longitude")) {
                            latLng.setLongitude(metaSnapshot.getValue(Double.class));
                        } else if (metaSnapshot.getKey().equals("logoID")) {
                            landmark.setLogoID(metaSnapshot.getValue(Integer.class));
                        } else if (metaSnapshot.getKey().equals("title")) {
                            landmark.setTitle(metaSnapshot.getValue(String.class));
                        } else if (metaSnapshot.getKey().equals("description")) {
                            landmark.setDescription(metaSnapshot.getValue(String.class));
                        } else if (metaSnapshot.getKey().equals("uri")) {
                            landmark.setUri(Uri.parse(metaSnapshot.getValue(String.class)));
                        }
                    }
                    landmark.setLatlong(latLng);
                    landmarks.add(landmark);
                    landmark = new Landmark();
                    latLng = new LatLng();
                }
                myCallback.onCallback(landmarks);
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
