package com.apcs.mofs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

public class ActivityMap extends AppCompatActivity implements OnMapReadyCallback{
    //Mapbox
    private MapView mapView;
    private MapboxMap mapboxMap;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 781;
    private static final int REQUEST_CODE_PLACE_SELECTION = 8747;
    private CarmenFeature home;
    private CarmenFeature work;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";

    //Database
    private DatabaseReference mDatabase;
    private String keyChat = "";
    private String username = "";
    private String TAG = "RRRRRRRRRRRRRRRRRRRRRR";

    //Storage
    StorageReference mStorage;

    //Load image
    private final long MAX_SIZE_IMAGE = 10485760; //10MB
    private final int REQUEST_CODE_PICK_PHOTO = 12;
    private String markerSnippetKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);

        initComponents(savedInstanceState);
    }

    private void initComponents(Bundle savedInstanceState) {
        keyChat = getIntent().getStringExtra("keyChat");
        username = getIntent().getStringExtra("username");
//      String groupName = getIntent().getStringExtra("groupName");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        //Mapbox
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                startPlacesSearchActivity(style);
            }
        });
        showMarkers(mapboxMap);
        mapboxMap.setInfoWindowAdapter(new MapboxMap.InfoWindowAdapter() {
            @Nullable
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                return getViewInfoWindow(marker);
            }
        });
        mapboxMap.setOnInfoWindowLongClickListener(new MapboxMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(@NonNull Marker marker) {
                marker.hideInfoWindow();
                showDialogEditMarker(mapboxMap, marker);
            }
        });
        mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
            @Override
            public boolean onMapLongClick(@NonNull LatLng point) {
                startPlacePickerActivity(point);
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_PHOTO && resultCode == RESULT_OK) {
            if (data == null) {
                Log.d(TAG, "Failed to pick photo");
            } else {
                try {
                    InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
                    Bitmap bitmapOfNewMarker = BitmapFactory.decodeStream(inputStream);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmapOfNewMarker.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bytes = baos.toByteArray();

                    StorageReference landmarkImageRef = mStorage.child("landmarks/" + keyChat + "/" + markerSnippetKey + "/images/infoWindowImage.jpeg");

                    UploadTask uploadTask = landmarkImageRef.putBytes(bytes);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d(TAG, "Failed to upload image");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    });
                } catch (Exception e) {
                    Log.d(TAG, "Failed to get bitmap");
                }
            }
        } else if (requestCode == REQUEST_CODE_AUTOCOMPLETE && resultCode == Activity.RESULT_OK ) {
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(14)
                                    .build()), 4000);
                    MarkerOptions markerOption = (new MarkerOptions()
                            .position(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                    ((Point) selectedCarmenFeature.geometry()).longitude())));
                    mapboxMap.addMarker(markerOption);
                }
            }
        } else if (requestCode == REQUEST_CODE_PLACE_SELECTION && resultCode == RESULT_OK){
            CarmenFeature carmenFeature = PlacePicker.getPlace(data);
            showDialogAddMarker(mapboxMap, new LatLng(carmenFeature.center().latitude(), carmenFeature.center().longitude()));
        }
    }

    private void startPlacePickerActivity(@NonNull LatLng point) {
        Intent intent = new PlacePicker.IntentBuilder()
                .accessToken(Mapbox.getAccessToken())
                .placeOptions(
                        PlacePickerOptions.builder()
                                .statingCameraPosition(
                                        new CameraPosition.Builder()
                                                .target(point)
                                                .zoom(16)
                                                .build())
                                .build())
                .build(ActivityMap.this);
        startActivityForResult(intent, REQUEST_CODE_PLACE_SELECTION);
    }

    @NonNull
    private View getViewInfoWindow(@NonNull Marker marker) {
        View layout = LayoutInflater.from(ActivityMap.this).inflate(R.layout.window_marker_info, (ConstraintLayout)findViewById(R.id.layout), false);
        TextView tvTitle = (TextView)layout.findViewById(R.id.title);
        tvTitle.setText(marker.getTitle());
        ImageView imageView = (ImageView)layout.findViewById(R.id.infoWindowImage);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference landmarkImageRef = storage.getReference().child("landmarks/" + keyChat + "/" + marker.getSnippet() + "/images/infoWindowImage.jpeg");
        landmarkImageRef.getBytes(MAX_SIZE_IMAGE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = getBitmap(bytes);
                imageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "No such file or path found!!", Toast.LENGTH_SHORT).show();
            }
        });
        return layout;
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

    private void startPlacesSearchActivity(@NonNull Style style) {
        initSearchFab();
        addUserLocations();
        setUpSource(style);
        setupLayer(style);
    }

    private void initSearchFab() {
        findViewById(R.id.fab_location_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.mapbox_access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .addInjectedFeature(home)
                                .addInjectedFeature(work)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(ActivityMap.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }

    private void addUserLocations() {
        home = CarmenFeature.builder().text("Mapbox SF Office")
                .geometry(Point.fromLngLat(-122.3964485, 37.7912561))
                .placeName("50 Beale St, San Francisco, CA")
                .id("mapbox-sf")
                .properties(new JsonObject())
                .build();

        work = CarmenFeature.builder().text("Mapbox DC Office")
                .placeName("740 15th Street NW, Washington DC")
                .geometry(Point.fromLngLat(-77.0338348, 38.899750))
                .id("mapbox-dc")
                .properties(new JsonObject())
                .build();
    }

    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));
    }

    private void setupLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                iconImage(symbolIconId),
                iconOffset(new Float[] {0f, -8f})
        ));
    }

    private void showDialogEditMarker(MapboxMap mapboxMap, Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMap.this, R.style.AlertDialogTheme);
        View layout = LayoutInflater.from(ActivityMap.this).inflate(R.layout.dialog_edit_marker, (ConstraintLayout)findViewById(R.id.layout), false);
        builder.setView(layout);
        ((TextView)layout.findViewById(R.id.title)).setText(getResources().getString(R.string.detailsOfTheMarker));
        ((TextView)layout.findViewById(R.id.message1)).setText(getResources().getString(R.string.markerName));
        ((Button)layout.findViewById(R.id.buttonNo)).setText(getResources().getString(R.string.cancel));
        ((Button)layout.findViewById(R.id.buttonYes)).setText(getResources().getString(R.string.edit));
        ((Button)layout.findViewById(R.id.buttonDelete)).setText(getResources().getString(R.string.delete));
        ((Button)layout.findViewById(R.id.buttonChooseImage)).setText(getResources().getString(R.string.chooseImage));
        ((ImageView)layout.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_baseline_location_on_24);
        EditText editText1 = (EditText)layout.findViewById(R.id.editText1);
        editText1.setText(marker.getTitle());

        AlertDialog alertDialog = builder.create();

        markerSnippetKey = marker.getSnippet();

        layout.findViewById(R.id.buttonNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        layout.findViewById(R.id.buttonYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editTextTitle = editText1.getText().toString();
                marker.setTitle(editTextTitle);
                sendLandmarksToDatabase(new InfoMarker(editTextTitle, marker.getSnippet(),
                        new LatLng(marker.getPosition().getLatitude(), marker.getPosition().getLongitude()), null));
                alertDialog.dismiss();
                marker.showInfoWindow(mapboxMap, mapView);
            }
        });

        layout.findViewById(R.id.buttonDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                marker.remove();
                DatabaseReference databaseLandmarkRef = mDatabase.child("landmarks").child(keyChat).child(markerSnippetKey);
                databaseLandmarkRef.removeValue();
                StorageReference landmarkImageRef = mStorage.child("landmarks/" + keyChat + "/" + markerSnippetKey + "/images/infoWindowImage.jpeg");
                landmarkImageRef.delete();
            }
        });

        layout.findViewById(R.id.buttonChooseImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPhoto();
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    private void showDialogAddMarker(MapboxMap mapboxMap, LatLng point) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMap.this, R.style.AlertDialogTheme);
        View layout = LayoutInflater.from(ActivityMap.this).inflate(R.layout.dialog_add_marker, (ConstraintLayout)findViewById(R.id.layout), false);
        builder.setView(layout);
        ((TextView)layout.findViewById(R.id.title)).setText(getResources().getString(R.string.detailsOfTheMarker));
        ((TextView)layout.findViewById(R.id.message1)).setText(getResources().getString(R.string.markerName));
        ((Button)layout.findViewById(R.id.buttonChooseImage)).setText(getResources().getString(R.string.chooseImage));
        ((Button)layout.findViewById(R.id.buttonNo)).setText(getResources().getString(R.string.cancel));
        ((Button)layout.findViewById(R.id.buttonYes)).setText(getResources().getString(R.string.add));
        ((ImageView)layout.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_baseline_location_on_24);
        EditText editText1 = (EditText)layout.findViewById(R.id.editText1);

        AlertDialog alertDialog = builder.create();

        markerSnippetKey = mDatabase.child("landmarks").child(keyChat).push().getKey();

        layout.findViewById(R.id.buttonNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        layout.findViewById(R.id.buttonYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editTextTitle = editText1.getText().toString();
                if (point != null) {
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(point)
                            .title(editTextTitle)
                            .snippet(markerSnippetKey));
                    sendLandmarksToDatabase(new InfoMarker(editTextTitle, markerSnippetKey, point, null));
                }
                alertDialog.dismiss();
            }
        });

        layout.findViewById(R.id.buttonChooseImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPhoto();
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

    private void pickPhoto() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Choose photo");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, REQUEST_CODE_PICK_PHOTO);
    }

    private void showMarkers(MapboxMap mapboxMap) {
        readMarkers(new CallbackReadMarkers() {
            @Override
            public void onCallback(ArrayList<InfoMarker> infoMarkers) {
                ArrayList<MarkerOptions> markerOptions = new ArrayList<>();
                for (int i = 0; i < infoMarkers.size(); i++) {
                    markerOptions.add(new MarkerOptions()
                                        .position(infoMarkers.get(i).getLatLong())
                                        .title(infoMarkers.get(i).getTitle())
                                        .snippet(infoMarkers.get(i).getSnippetKey()));
                }
                mapboxMap.addMarkers(markerOptions);
            }
        });
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
            case R.id.nav_group_mess:
                startChatActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startActivityAboutGroup() {
        Intent intent = new Intent(this, ActivityAboutGroup.class);
        intent.putExtra("keyChat", keyChat);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void startChatActivity() {
        Intent intent = new Intent(this, ActivityChat.class);
        intent.putExtra("keyChat", keyChat);
        intent.putExtra("username", username);
        intent.putExtra("photoProfile", getIntent().getStringExtra("photoProfile"));
//        intent.putExtra("groupName", groupName);
        startActivity(intent);
    }

    private void sendLandmarksToDatabase(InfoMarker infoMarker) {
//        keyNewLandmark = mDatabase.child("landmarks").child(keyChat).push().getKey();
        mDatabase.child("landmarks").child(keyChat).child(markerSnippetKey).child("longitude").setValue(infoMarker.getLatLong().getLongitude());
        mDatabase.child("landmarks").child(keyChat).child(markerSnippetKey).child("latitude").setValue(infoMarker.getLatLong().getLatitude());
        mDatabase.child("landmarks").child(keyChat).child(markerSnippetKey).child("logoID").setValue(infoMarker.getLogoId());
        mDatabase.child("landmarks").child(keyChat).child(markerSnippetKey).child("title").setValue(infoMarker.getTitle());
        mDatabase.child("landmarks").child(keyChat).child(markerSnippetKey).child("snippet").setValue(infoMarker.getSnippetKey());
        mDatabase.child("landmarks").child(keyChat).child(markerSnippetKey).child("uri").setValue(String.valueOf(infoMarker.getUri()));
    }

    public void navigationBottomClicked(View view) {
        switch (view.getId()) {
            case R.id.aboutGroup:
                startActivityAboutGroup();
                break;
        }
    }

    public interface CallbackReadMarkers {
        void onCallback(ArrayList<InfoMarker> infoMarkers);
    }

    public void readMarkers(CallbackReadMarkers callbackReadMarkers) {
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
                        } else if (metaSnapshot.getKey().equals("snippet")) {
                            infoMarker.setSnippetKey(metaSnapshot.getValue(String.class));
                        } else if (metaSnapshot.getKey().equals("uri")) {
                            infoMarker.setUri(Uri.parse(metaSnapshot.getValue(String.class)));
                        }
                    }
                    infoMarker.setLatLong(latLng);
                    infoMarkers.add(infoMarker);
                    infoMarker = new InfoMarker();
                    latLng = new LatLng();
                }
                callbackReadMarkers.onCallback(infoMarkers);
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
