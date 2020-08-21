package com.apcs.mofs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener{
    private final int requestCodeActEditDelMarker = 1604;
    private final int requestCodeActAddLandmark = 123;
    private MapView mView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location myCurrentLocation;
    private DatabaseReference mDatabase;
    private String keyChat = "";
    private String username = "";
    private String TAG = "RRRRRRRRRRRRRRRRRRRRRR";

    private ArrayList<Landmark> myLandmarks= new ArrayList<Landmark>();
    Point despoint = null;
    private NavigationMapRoute navigationMapRoute;
    //    private FloatingActionButton floatingBtnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this,getString(R.string.access_token));
        setContentView(R.layout.map_activity_layout);
        //loadData();
        mView = findViewById(R.id.mapView);
        mView.onCreate(savedInstanceState);
        mView.getMapAsync(this);
        //floatingBtnSearch = (FloatingActionButton)findViewById(R.id.floatingActionButtonSearch);

        //get data from ActivityGroupList
        keyChat = getIntent().getStringExtra("keyChat");
        username = getIntent().getStringExtra("username");
//      String groupName = getIntent().getStringExtra("groupName");
        //database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        retrieveLandmarks();
    }

    //    //Search func
//    private Intent searchLocation(){
//        Point pointOfProximity = Point.fromLngLat(myCurrentLocation.getLongitude(),myCurrentLocation.getLatitude());
//        Intent sendThrough = new PlaceAutocomplete.IntentBuilder()
//                .accessToken(Mapbox.getAccessToken())
//                .placeOptions(PlaceOptions.builder()
//                        .backgroundColor(Color.parseColor("#ffffff"))
//                        .hint("Address: /Sample/ 227 Nguyen Van Cu, Quan 5")
//                        .country(Locale.getDefault())
//                        .proximity(pointOfProximity)
//                        .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS,
//                            GeocodingCriteria.TYPE_POI,
//                            GeocodingCriteria.TYPE_PLACE)
//                        .limit(5)
//                        .build(PlaceOptions.MODE_CARDS))
//                .build(MainActivity.this);
//        return sendThrough;
//    }

    private void loadData() {
        //Load các landmarks từ file vào arraylist & chuyển landmark -> markers, show markers lên bản đồ.
        //Neu arraylist ko rỗng, xóa hết item.
        if(myLandmarks.size()!=0)
            myLandmarks.clear();
        //load dữ liệu (title, description, ảnh, id..) từ file vào arraylist
        //.... làm sau!!
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
                actionAddPlace();
                //refreshMarkers();
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
        startActivity(intent);
    }

    private void openChatActivity() {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("keyChat", keyChat);
        intent.putExtra("username", username);
//        intent.putExtra("groupName", groupName);
        startActivity(intent);
    }

    private void refreshMarkers() {
        updateArrayListLandmark();
        map.removeAnnotations();
        //loadData();
        for(int i =0; i<myLandmarks.size(); i++)
            displayLandMark(i);
    }

    private void updateArrayListLandmark() {
        //Cập nhật du lieu vào database
        //Lay du lieu tu database
    }

    private void retrieveLandmarks() {
        DatabaseReference mGroups = mDatabase.child("landmarks").child(keyChat);
        mGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myLandmarks.clear();
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
                            landmark.setUri(metaSnapshot.getValue(Uri.class));
                        }
                    }
                    landmark.setLatlong(latLng);
                    myLandmarks.add(landmark);
                    landmark = new Landmark();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void sendLandmarksToDatabase(Landmark landmark) {
        String key = mDatabase.child("landmarks").child(keyChat).push().getKey();
        mDatabase.child("landmarks").child(keyChat).child(key).child("longitude").setValue(landmark.getLatlong().getLongitude());
        mDatabase.child("landmarks").child(keyChat).child(key).child("latitude").setValue(landmark.getLatlong().getLatitude());
        mDatabase.child("landmarks").child(keyChat).child(key).child("logoID").setValue(landmark.getLogoID());
        mDatabase.child("landmarks").child(keyChat).child(key).child("title").setValue(landmark.getTitle());
        mDatabase.child("landmarks").child(keyChat).child(key).child("description").setValue(landmark.getDescription());
        mDatabase.child("landmarks").child(keyChat).child(key).child("uri").setValue(landmark.getUri());
    }

    private void displayLandMark(int i) {
        // số thứ tự của landmark cần chuyển thành marker và hiển thị lên map.
        Landmark landmarkI = myLandmarks.get(i);
        markerize(landmarkI);
        Icon icon = null;
        if(landmarkI.getLogoID()!= 0) {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), landmarkI.getLogoID());
            bmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth() / 4, bmp.getHeight() / 4, false);
            IconFactory iconFactory = IconFactory.getInstance(this);
            icon = iconFactory.fromBitmap(bmp);
        }
        map.addMarker(new MarkerOptions()
                .position(landmarkI.getLatlong())
                .title(landmarkI.getTitle())
                .snippet(landmarkI.getDescription())
                .icon(icon)
        );
    }

    private void actionAddPlace() {
        String saySomething = "Tạo activity khác, chứa 1 Textview, cho ng dùng nhập Title, 1 text View cho người dùng nhập Discription, 1 button load ảnh (Nếu ng dùng nhấn btn,thì xin quyền truy cập ảnh & load ảnh của ng dùng vào grid view), 1 GridView để chứa ảnh người dùng, 1 btn xác nhận, 1 btn cancel";
        Toast.makeText(getApplicationContext(),saySomething,Toast.LENGTH_SHORT).show();
        //Khi nhấn xác nhận or cancel, thì quay lại màn hình map, Nếu là xác nhận thì thêm marker lên bản đồ.
        // Hiện tại mình làm chỉ lấy tọa độ của ng dùng thôi --> Ko cho người dùng nhập tọa độ
        //Thêm landmark này vào file chứa landmark và update data lên server luôn.
        Intent intent = new Intent(MapActivity.this,ActivityAddALandMark.class);
        startActivityForResult(intent,123);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        map.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                showRoute(marker);
                return false;
            }
        });
        map.setOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng point) {
                int indexCheckMarker =0;
                for(; indexCheckMarker <myLandmarks.size();indexCheckMarker++)
                {
                    if(myLandmarks.get(indexCheckMarker).getLatlong().getLatitude() == point.getLatitude() && myLandmarks.get(indexCheckMarker).getLatlong().getLongitude() == point.getLongitude()){
                        indexCheckMarker = indexCheckMarker+ myLandmarks.size(); //break loop
                    }
                }
                if(indexCheckMarker>myLandmarks.size() || indexCheckMarker == myLandmarks.size() )
                {
                    Landmark landmarkLondClicked = myLandmarks.get(indexCheckMarker-myLandmarks.size());
                    //Action for aditing or deleting marker
                    Toast.makeText(getApplicationContext(),"Action edit or Delete Marker",Toast.LENGTH_SHORT).show();
                    activityEditDeleteMarker(landmarkLondClicked);
                }
            }
        });
        enableLocation();
    }
    private void activityEditDeleteMarker(Landmark landmarkLongClicked) {
        Intent intent = new Intent(MapActivity.this, ActivityEditDeleteMarker.class);
        intent.putExtra("tit",landmarkLongClicked.getTitle());
        intent.putExtra("des",landmarkLongClicked.getDescription());
        intent.putExtra("lat",landmarkLongClicked.getLatlong().getLatitude());
        intent.putExtra("long",landmarkLongClicked.getLatlong().getLongitude());
        if(landmarkLongClicked.getUri() != null) {
            intent.putExtra("uri", landmarkLongClicked.getUri().toString());
            intent.putExtra("checkUriNull",0);
        }
        else{
            intent.putExtra("logoUri",getString(R.string.null_uri));
            intent.putExtra("checkUriNull",1);
        }
        startActivityForResult(intent,requestCodeActEditDelMarker );
    }
    private void enableLocation(){
        if(PermissionsManager.areLocationPermissionsGranted(this)){
            initializeLocationEngine();
            initializeLocationLayer();
        } else{
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }
    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine(){
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();
        Location lastLocation = locationEngine.getLastLocation();
        if(lastLocation != null){
            myCurrentLocation = lastLocation;
            setCameraLocation(lastLocation);
        }else {
            locationEngine.addLocationEngineListener((LocationEngineListener) this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer(){
        locationLayerPlugin = new LocationLayerPlugin(mView,map,locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
    }

    private void setCameraLocation(Location location){
        //make the camera follow the current location
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),30));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(),location.getLongitude()))
                .zoom(15)
                .bearing(90)
                .tilt(30)
                .build();
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!= null){
            myCurrentLocation = location;
            setCameraLocation(location);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //Show a Toask text why needed
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted)
            enableLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState!= null)
            mView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mView.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == requestCodeActAddLandmark){
            if(resultCode == RESULT_OK){
                Landmark newLandmark = new Landmark();

                newLandmark.setLatlong(new LatLng(myCurrentLocation.getLatitude(),myCurrentLocation.getLongitude()));
                newLandmark.setTitle(data.getStringExtra("tit"));
                int isNull = data.getIntExtra("checkNull",9);
                newLandmark.setDescription(data.getStringExtra("des"));
                String isUri =data.getStringExtra("uri");
                if(isNull == 0) {
                    newLandmark.setUri(Uri.parse(data.getStringExtra("uri")));
                }else if(isNull ==1){
                    newLandmark.setUri(null);
                }
                else{
                    Toast.makeText(getApplicationContext(),String.valueOf(isNull),Toast.LENGTH_SHORT).show();
                }
                myLandmarks.add(newLandmark);
                markerize(newLandmark);
            }
        } else if(requestCode == requestCodeActEditDelMarker){
            if(resultCode == RESULT_OK){
                //Bat intent Marker da sua or xoa.
                //Update arrList Marker
                //Update arrList Landmark.
                //Load lai markers
                int isDelete = data.getIntExtra("isDel",9);
                Landmark newLandmark = new Landmark();
                newLandmark.setLatlong(new LatLng(data.getDoubleExtra("lat",122.0426),data.getDoubleExtra("long",-30.4215)));
                if(isDelete == 1){
                    for(int i =0; i<myLandmarks.size();i++){
                        if(((newLandmark.getLatlong().getLatitude()- myLandmarks.get(i).getLatlong().getLatitude()<1) && (newLandmark.getLatlong().getLongitude()-myLandmarks.get(i).getLatlong().getLongitude()<1))||(( myLandmarks.get(i).getLatlong().getLatitude()-newLandmark.getLatlong().getLatitude()<1) && (myLandmarks.get(i).getLatlong().getLongitude()-newLandmark.getLatlong().getLongitude()<1))){
                            myLandmarks.remove(myLandmarks.get(i));
                            refreshMarkers();
                            return;
                        }
                    }
                }else if(isDelete == 0){
                    int isNull = data.getIntExtra("checkNull",9);
                    newLandmark.setDescription(data.getStringExtra("des"));
                    newLandmark.setTitle(data.getStringExtra("tit"));
                    String isUri =data.getStringExtra("uri");
                    if(isNull == 0) {
                        newLandmark.setUri(Uri.parse(data.getStringExtra("uri")));
                    }else if(isNull ==1){
                        newLandmark.setUri(null);
                    }
                    else{
                        Toast.makeText(getApplicationContext(),String.valueOf(isNull),Toast.LENGTH_SHORT).show();
                    }
                    for(int i =0; i<myLandmarks.size();i++){
                        if(((newLandmark.getLatlong().getLatitude()- myLandmarks.get(i).getLatlong().getLatitude()<1) && (newLandmark.getLatlong().getLongitude()-myLandmarks.get(i).getLatlong().getLongitude()<1))||(( myLandmarks.get(i).getLatlong().getLatitude()-newLandmark.getLatlong().getLatitude()<1) && (myLandmarks.get(i).getLatlong().getLongitude()-newLandmark.getLatlong().getLongitude()<1))){
                            myLandmarks.get(i).setTitle(newLandmark.getTitle());
                            myLandmarks.get(i).setDescription(newLandmark.getDescription());
                            myLandmarks.get(i).setUri(newLandmark.getUri());
                            refreshMarkers();
                            return;
                        }
                    }
                }
            }
        }
        }
    private void markerize(Landmark lmk) {
        if(lmk.getUri() != null ) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(lmk.getUri());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 4, bitmap.getHeight() / 4, false);
                IconFactory iconFactory = IconFactory.getInstance(MapActivity.this);
                Icon icon = iconFactory.fromBitmap(bitmap);
                map.addMarker(new MarkerOptions()
                        .position(lmk.getLatlong())
                        .title(lmk.getTitle())
                        .snippet(lmk.getDescription())
                        .icon(icon));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else{
            map.addMarker(new MarkerOptions()
                    .position(lmk.getLatlong())
                    .title(lmk.getTitle())
                    .snippet(lmk.getDescription()));
        }
    }


    private void showRoute(@NonNull Marker marker) {
        if(despoint == null)
        {
            despoint = Point.fromLngLat(marker.getPosition().getLongitude(),marker.getPosition().getLatitude());
            Point myPoint = Point.fromLngLat(myCurrentLocation.getLongitude(),myCurrentLocation.getLatitude());
            getRoute(myPoint,despoint);
        }else if(despoint != null){
            despoint = null;
            navigationMapRoute.removeRoute();
        }
    }

    private void getRoute(Point myPoint, Point despoint) {
        NavigationRoute.builder()
                .accessToken(Mapbox.getAccessToken())
                .origin(myPoint)
                .destination(despoint)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if(response.body()==null){
                            return;
                        }else if(response.body().routes().size() ==0){
                            return;
                        }
                        DirectionsRoute currentRoute = response.body().routes().get(0);
                        if(navigationMapRoute != null){
                            navigationMapRoute.removeRoute();
                        }else{
                            navigationMapRoute = new NavigationMapRoute(null,mView,map);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                    }
                });

    }


}
