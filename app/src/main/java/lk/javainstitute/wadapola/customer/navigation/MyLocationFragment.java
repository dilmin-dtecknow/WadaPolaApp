package lk.javainstitute.wadapola.customer.navigation;

import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.HashMap;

import lk.javainstitute.wadapola.MapActivity;
import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.model.CustomToast;
import lk.javainstitute.wadapola.model.CustomerData;


public class MyLocationFragment extends Fragment {

    private Double latitude;
    private Double longitude;
    private boolean isMarkerAdded = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_location, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
        String customer = sharedPreferences.getString("customer", null);

        Gson gson = new Gson();
        CustomerData customerData = gson.fromJson(customer, CustomerData.class);

        Button button2 = view.findViewById(R.id.buttonCustomLoc1);
        button2.setVisibility(View.INVISIBLE);

        SupportMapFragment supportMapFragment = new SupportMapFragment();

        FragmentManager childFragmentManager = getChildFragmentManager();
        childFragmentManager.beginTransaction().add(R.id.frameLayoutMapFragment1,supportMapFragment)
                .commit();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {

                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setAllGesturesEnabled(true);

                CustomToast.cusErrorToast(getContext(),"Map Loaded success",true);
                if (checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Log.i("Log1", "ACCESS_FINE_LOCATION Granted");
                    if (checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Log.i("Log1", "ACCESS_COARSE_LOCATION Granted");
                        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
                        googleMap.setMyLocationEnabled(true);

                        fusedLocationProviderClient.getLastLocation()
                                .addOnSuccessListener(new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            LatLng latLng2 = new LatLng(location.getLatitude(), location.getLongitude());
                                            // Handle location here
                                            latitude=location.getLatitude();
                                            longitude = location.getLongitude();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i("Log1", "Map current location failed", e);
                                    }
                                });
                    } else {
                        Log.i("Log1", "ACCESS_COARSE_LOCATION Denied");
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
                    }
                } else {
                    Log.i("Log1", "ACCESS_FINE_LOCATION Denied");
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                }

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        if (!isMarkerAdded) {
                            googleMap.addMarker(
                                    new MarkerOptions()
                                            .position(latLng)
                                            .title("Custom location")
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.location))
                            );


                            button2.setVisibility(View.VISIBLE);
                            isMarkerAdded = true; // Set the flag to true
                            button2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    double latitudeCustom = latLng.latitude;
                                    double longitudeCustom = latLng.longitude;

                                    firestore.collection("address").whereEqualTo("user_id",customerData.getId())
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                    Log.i("Log1", String.valueOf(latitudeCustom));
                                                    Log.i("Log1", String.valueOf(longitudeCustom));

//                                                    if (latitudeCustom&&longitudeCustom.equals(null)){
//                                                        CustomToast.cusErrorToast(getContext(),"Location not found Please restart or check if location off",false);
//                                                        return;
//                                                    }

                                                    HashMap<String,Object> document = new HashMap<>();
                                                    document.put("latitude", String.valueOf(latitudeCustom));
                                                    document.put("longitude", String.valueOf(longitudeCustom));

                                                    firestore.collection("address").document(queryDocumentSnapshots.getDocuments().get(0).getId())
                                                            .update(document).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    CustomToast.cusErrorToast(getContext(),"Location update Success",true);
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.i("Log1","My Location address update fail");
                                                                }
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("Log1","My Location address load fail");
                                                }
                                            });

                                }
                            });
                        }
                    }
                });

            }
        });



//current location update
        Button button = view.findViewById(R.id.buttonUpdateCurentLoc1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firestore.collection("address").whereEqualTo("user_id",customerData.getId())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                Log.i("Log1", String.valueOf(latitude));
                                Log.i("Log1", String.valueOf(longitude));

                                if (latitude.equals(null)&&longitude.equals(null)){
                                    CustomToast.cusErrorToast(getContext(),"Location not found Please restart or check if location off",false);
                                    return;
                                }

                                HashMap<String,Object> document = new HashMap<>();
                                document.put("latitude",latitude.toString());
                                document.put("longitude",longitude.toString());

                                firestore.collection("address").document(queryDocumentSnapshots.getDocuments().get(0).getId())
                                        .update(document).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                CustomToast.cusErrorToast(getContext(),"Location update Success",true);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i("Log1","My Location address update fail");
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("Log1","My Location address load fail");
                            }
                        });
            }
        });



        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, re-trigger map setup or location fetching
                SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frameLayoutMapFragment1);
                if (supportMapFragment != null) {
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            if (checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                googleMap.setMyLocationEnabled(true);
                            }
                        }
                    });
                }
            } else {
                CustomToast.cusErrorToast(getContext(), "Permission denied", false);
            }
        }
        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, re-trigger location fetching
                SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frameLayoutMapFragment1);
                if (supportMapFragment != null) {
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            if (checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                googleMap.setMyLocationEnabled(true);
                            }
                        }
                    });
                }
            } else {
                CustomToast.cusErrorToast(getContext(), "Permission denied", false);
            }
        }
    }
}