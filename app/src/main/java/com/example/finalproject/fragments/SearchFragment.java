package com.example.finalproject.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.finalproject.R;
import com.example.finalproject.models.Place;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText searchInput;

    private DatabaseReference placesRef;
    private List<Place> allPlaces;

    private ClusterManager<PlaceClusterItem> clusterManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchInput = view.findViewById(R.id.searchInput);

        placesRef = FirebaseDatabase.getInstance().getReference("places");
        allPlaces = new ArrayList<>();

        // Load Google Map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadPlaces();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchAndZoom(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }
    private boolean isLocationEnabled() {
        if (requireActivity() == null) return false;

        LocationManager locationManager =
                (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    private void checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Permission already granted
            enableLocationFeatures();

        } else {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }
    private void enableLocationFeatures() {

        if (mMap == null) return;

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        moveToUserLocation();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (isLocationEnabled()) {
                    enableLocationFeatures();
                }

            } else {
                Toast.makeText(requireContext(),
                        "Location permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        clusterManager = new ClusterManager<>(requireContext(), mMap);
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        checkLocationPermission();
    }

    private void moveToUserLocation() {

        if (requireActivity() == null) return;

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(requireActivity());

        client.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null && mMap != null) {

                        LatLng user = new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        );

                        mMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(user, 12f)
                        );
                    }
                });
    }

    private void loadPlaces() {

        placesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allPlaces.clear();

                if (mMap != null) {
                    mMap.clear();
                }

                if (clusterManager != null) {
                    clusterManager.clearItems();
                }

                for (DataSnapshot placeSnapshot : snapshot.getChildren()) {

                    Place place = placeSnapshot.getValue(Place.class);

                    if (place != null) {

                        if (place.getId() == null || place.getId().isEmpty()) {
                            place.setId(placeSnapshot.getKey());
                        }

                        allPlaces.add(place);

                        if (mMap != null) {

                            clusterManager.addItem(
                                    new PlaceClusterItem(
                                            place.getLat(),
                                            place.getLng(),
                                            place.getTitle(),
                                            place.getDescription()
                                    )
                            );
                        }
                    }
                }

                // ⭐ IMPORTANT: cluster AFTER loop
                if (clusterManager != null) {
                    clusterManager.cluster();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // You can log error here if needed
            }
        });
    }

    private void searchAndZoom(String query) {

        if (mMap == null || allPlaces == null || allPlaces.isEmpty()) return;

        if (query == null || query.trim().isEmpty()) return;

        String lowerQuery = query.toLowerCase(Locale.getDefault()).trim();

        for (Place place : allPlaces) {

            if (place.getTitle() != null &&
                    place.getTitle().toLowerCase(Locale.getDefault()).contains(lowerQuery)) {

                LatLng position = new LatLng(place.getLat(), place.getLng());

                mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(position, 14f)
                );

                return;
            }
        }

        Toast.makeText(requireContext(),
                "No matching place found",
                Toast.LENGTH_SHORT).show();
    }
    private BitmapDescriptor getMarkerIcon(int resId) {

        BitmapDrawable bitmapDrawable =
                (BitmapDrawable) ContextCompat.getDrawable(requireContext(), resId);

        Bitmap bitmap = bitmapDrawable.getBitmap();

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}