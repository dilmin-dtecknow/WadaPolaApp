package lk.javainstitute.wadapola;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String latitude = intent.getStringExtra("latitude");
        String longitude = intent.getStringExtra("longitude");
        String name = intent.getStringExtra("name");
        String category = intent.getStringExtra("category");

        SupportMapFragment supportMapFragment = new SupportMapFragment();

        FragmentManager supportFragmentManager = getSupportFragmentManager();
        supportFragmentManager.beginTransaction().add(R.id.mapFrameLayout1,supportMapFragment)
                .commit();


//        Load map
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                Toast.makeText(MapActivity.this, "Map loaded success", Toast.LENGTH_SHORT).show();
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);

                LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                    Log.i("Log1","ACCESS_FINE_LOCATIONACCESS_FINE_LOCATION Granted");
                    if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Log.i("Log1","ACCESS_COARSE_LOCATION Granted");
                        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
                        googleMap.setMyLocationEnabled(true);

                        fusedLocationProviderClient.getLastLocation()
                                .addOnSuccessListener(new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        LatLng latLng2 = new LatLng(location.getLatitude(), location.getLongitude());

                                        googleMap.addPolyline(
                                                new PolylineOptions()
                                                        .add(latLng)
                                                        .add(latLng2)
                                                        .width(15)
                                                        .color(getColor(R.color.c1))
                                                        .startCap(new RoundCap())
                                                        .endCap(new RoundCap())
                                        );
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    Log.i("Log1","Map current Location fail");
                                    }
                                });

                    }else {
                        Log.i("Log1","ACCESS_COARSE_LOCATION Denied");
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},200);
                    }
                } else {
                    Log.i("Log1","ACCESS_FINE_LOCATIONACCESS_FINE_LOCATION Denied");
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},100);
                }

                googleMap.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(latLng)
                                        .zoom(18)
                                        .build()
                        )
                );

                googleMap.addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .title(name+"\n"+category)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.worker))
                );


            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        if (requestCode == 100 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            Log.i("Log1","ACCESS_FINE_LOCATIONACCESS_FINE_LOCATION Denied");
        }
        if (requestCode == 200 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            Log.i("Log1","ACCESS_COARSE_LOCATION Denied");
        }
    }
}