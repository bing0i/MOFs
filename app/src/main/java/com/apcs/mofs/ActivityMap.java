package com.apcs.mofs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NavUtils;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineTranslate;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class ActivityMap extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {
    private ProgressBar progressBar;
    private final int REQUEST_CODE_GET_PLACE = 23123;
    private final int BITMAP_SIZE = 500;
    //Mapbox
    private MapView mapView;
    private MapboxMap mapboxMap;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 781;
    private static final int REQUEST_CODE_PLACE_SELECTION = 8747;
    //PlacesSearch
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";
    //TrackDeviceLocation
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationChangeListeningActivityLocationCallback callback = new LocationChangeListeningActivityLocationCallback(this);

    //Database
    private DatabaseReference mDatabase;
    private DatabaseReference mMarker;
    private String keyChat = "";
    private String username = "";
    private static String TAG = "RRRRRRRRRRRRRRRRRRRRRR";

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
        getSupportActionBar().setTitle("Map");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        keyChat = getIntent().getStringExtra("keyChat");
        username = getIntent().getStringExtra("username");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mMarker = mDatabase.child("landmarks").child(keyChat);
        mStorage = FirebaseStorage.getInstance().getReference();

        //Mapbox
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.getUiSettings().setCompassFadeFacingNorth(false);
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                progressBar.setVisibility(View.GONE);
                initPlacesSearchActivity(style);
                enableLocationComponent(style);
            }
        });
//        showMarkers(mapboxMap);
        retrieveNewMarker();

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
                Log.d(TAG, "Failed to pick image");
            } else {
                try {
                    byte[] bytes = getImageBytes(data);
                    uploadImageToStorage(bytes);
                } catch (Exception e) {
                    Log.d(TAG, "Failed to upload image");
                }
            }
        } else if (requestCode == REQUEST_CODE_AUTOCOMPLETE && resultCode == Activity.RESULT_OK ) {
            addMarkerAfterSearching(data);
        } else if (requestCode == REQUEST_CODE_PLACE_SELECTION && resultCode == RESULT_OK){
            addMarkerAfterSelection(data);
        } else if (requestCode == REQUEST_CODE_GET_PLACE && resultCode == RESULT_OK) {
            animateCameraLatLng(data.getDoubleExtra("latitude", 0), data.getDoubleExtra("longitude", 0));
        }
    }

    private void animateCameraLatLng(double latitude, double longitude) {
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(latitude,
                                longitude))
                        .zoom(16)
                        .build()), 3000);
    }

    @SuppressLint("MissingPermission")
    public void animateCameraToUserLocation(View view){
        if (mapboxMap != null) {
            locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {
                    Location location = result.getLastLocation();
                    animateCameraLatLng(location.getLatitude(), location.getLongitude());
                }
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d(TAG, "Failed to get last location");
                }
            });
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();
            locationComponent.activateLocationComponent(locationComponentActivationOptions);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();
        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Log.d(TAG, getResources().getString(R.string.userLocationPermissionExplanation));
//        Toast.makeText(this, R.string.user_location_permission_explanation,
//                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Log.d(TAG, getResources().getString(R.string.userLocationPermissionNotGranted));
//            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
        }
    }

    private void addMarkerAfterSelection(Intent data) {
        CarmenFeature carmenFeature = PlacePicker.getPlace(data);
        showDialogAddMarker(mapboxMap, new LatLng(new LatLng(
                ((Point) carmenFeature.geometry()).latitude(),
                ((Point) carmenFeature.geometry()).longitude())),
                carmenFeature.placeName());
    }

    private void addMarkerAfterSearching(Intent data) {
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
                showDialogAddMarker(mapboxMap, new LatLng(new LatLng(
                        ((Point) selectedCarmenFeature.geometry()).latitude(),
                        ((Point) selectedCarmenFeature.geometry()).longitude())),
                        selectedCarmenFeature.placeName());
            }
        }
    }

    private void uploadImageToStorage(byte[] bytes) {
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
    }

    private byte[] getImageBytes(Intent data) throws FileNotFoundException {
        InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
        Bitmap bitmapOfNewMarker = BitmapFactory.decodeStream(inputStream);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapOfNewMarker.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
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
        TextView tvAddress = (TextView)layout.findViewById(R.id.address);
        mDatabase.child("landmarks").child(keyChat).child(marker.getSnippet()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals("address")) {
                        if (ds.getValue(String.class).equals("null"))
                            tvAddress.setText("");
                        else
                            tvAddress.setText(ds.getValue(String.class));
                        return;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Failed to read address.");
            }
        });

        ProgressBar progressBarInfoWindow = (ProgressBar)layout.findViewById(R.id.progressBarInfoWindow);
        progressBarInfoWindow.setVisibility(View.VISIBLE);

        ImageView imageView = (ImageView)layout.findViewById(R.id.infoWindowImage);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference landmarkImageRef = storage.getReference().child("landmarks/" + keyChat + "/" + marker.getSnippet() + "/images/infoWindowImage.jpeg");
        landmarkImageRef.getBytes(MAX_SIZE_IMAGE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = getBitmap(bytes);
                imageView.setImageBitmap(bitmap);
                progressBarInfoWindow.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "No such file or path found");
                imageView.setImageResource(R.mipmap.ic_launcher);
                progressBarInfoWindow.setVisibility(View.GONE);
//                Toast.makeText(getApplicationContext(), "No such file or path found", Toast.LENGTH_SHORT).show();
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
        return Bitmap.createScaledBitmap(dstBmp, BITMAP_SIZE, BITMAP_SIZE, true);
    }

    private void initPlacesSearchActivity(@NonNull Style style) {
        initSearchFab();
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
                                .build(PlaceOptions.MODE_CARDS))
                        .build(ActivityMap.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
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

    private void showDialogAddMarker(MapboxMap mapboxMap, LatLng point, String address) {
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
                if (editTextTitle.length() == 0)
                    return;
                if (point != null) {
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(point)
                            .title(editTextTitle)
                            .snippet(markerSnippetKey));
                    sendLandmarksToDatabase(new InfoMarker(editTextTitle, markerSnippetKey, point, address));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actionbar_activity_map,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.navPlaces:
                startPlacesActivity();
                break;
            case R.id.navChat:
                startChatActivity();
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startPlacesActivity() {
        Intent intent = new Intent(this, ActivityPlaces.class);
        intent.putExtra("keyChat", keyChat);
        startActivityForResult(intent, REQUEST_CODE_GET_PLACE);
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
        Map map = new HashMap();
        map.put("longitude", infoMarker.getLatLong().getLongitude());
        map.put("latitude", infoMarker.getLatLong().getLatitude());
        map.put("title", infoMarker.getTitle());
        map.put("snippet", infoMarker.getSnippetKey());
        map.put("address", String.valueOf(infoMarker.getAddress()));
        mMarker.child(markerSnippetKey).updateChildren(map);
    }

    public void navigationBottomClicked(View view) {
        switch (view.getId()) {
            case R.id.aboutGroup:
                startActivityAboutGroup();
                break;
        }
    }

    private void retrieveNewMarker() {
        mMarker.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                ArrayList<InfoMarker> infoMarkers = new ArrayList<>();
                InfoMarker infoMarker = new InfoMarker();
                LatLng latLng = new LatLng();
                for (DataSnapshot metaSnapshot: snapshot.getChildren()) {
                    if (metaSnapshot.getKey().equals("latitude")) {
                        latLng.setLatitude(metaSnapshot.getValue(Double.class));
                    } else if (metaSnapshot.getKey().equals("longitude")) {
                        latLng.setLongitude(metaSnapshot.getValue(Double.class));
                    } else if (metaSnapshot.getKey().equals("title")) {
                        infoMarker.setTitle(metaSnapshot.getValue(String.class));
                    } else if (metaSnapshot.getKey().equals("snippet")) {
                        infoMarker.setSnippetKey(metaSnapshot.getValue(String.class));
                    } else if (metaSnapshot.getKey().equals("address")) {
                        infoMarker.setAddress(metaSnapshot.getValue(String.class));
                    }
                }
                infoMarker.setLatLong(latLng);
                ArrayList<MarkerOptions> markerOptions = new ArrayList<>();
                markerOptions.add(new MarkerOptions()
                        .position(infoMarker.getLatLong())
                        .title(infoMarker.getTitle())
                        .snippet(infoMarker.getSnippetKey()));
                mapboxMap.addMarkers(markerOptions);
//                infoMarkers.add(infoMarker);
                infoMarker = new InfoMarker();
                latLng = new LatLng();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.toString() + " Failed to get child node");
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
        // Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private static class LocationChangeListeningActivityLocationCallback implements LocationEngineCallback<LocationEngineResult> {
        private final WeakReference<ActivityMap> activityWeakReference;

        LocationChangeListeningActivityLocationCallback(ActivityMap activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            ActivityMap activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }
//                Toast.makeText(activity, String.format(activity.getString(R.string.new_location),
//                        String.valueOf(result.getLastLocation().getLatitude()),
//                        String.valueOf(result.getLastLocation().getLongitude())),
//                        Toast.LENGTH_SHORT).show();
                Log.d(TAG, String.format(activity.getString(R.string.newLocation),
                        String.valueOf(result.getLastLocation().getLatitude()),
                        String.valueOf(result.getLastLocation().getLongitude())));
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }
        @Override
        public void onFailure(@NonNull Exception exception) {
            ActivityMap activity = activityWeakReference.get();
            if (activity != null) {
//                Toast.makeText(activity, exception.getLocalizedMessage(),
//                        Toast.LENGTH_SHORT).show();
                Log.d(TAG, exception.getLocalizedMessage());
            }
        }
    }
}
