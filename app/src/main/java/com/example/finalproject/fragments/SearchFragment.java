package com.example.finalproject.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.finalproject.AIChatActivity;
import com.example.finalproject.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText searchInput;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // [name, lat, lng, location, stringResourceKey, wikipediaPageTitle]
    private static final String[][] LANDMARKS = {
            {
                    "Eiffel Tower",
                    "48.8584", "2.2945",
                    "Paris, France",
                    "landmark_eiffel_tower",
                    "Eiffel_Tower"
            },
            {
                    "Colosseum",
                    "41.8902", "12.4922",
                    "Rome, Italy",
                    "landmark_colosseum",
                    "Colosseum"
            },
            {
                    "Machu Picchu",
                    "-13.1631", "-72.5450",
                    "Cusco, Peru",
                    "landmark_machu_picchu",
                    "Machu_Picchu"
            },
            {
                    "Great Wall of China",
                    "40.4319", "116.5704",
                    "Beijing, China",
                    "landmark_great_wall",
                    "Great_Wall_of_China"
            },
            {
                    "Taj Mahal",
                    "27.1751", "78.0421",
                    "Agra, India",
                    "landmark_taj_mahal",
                    "Taj_Mahal"
            },
            {
                    "Statue of Liberty",
                    "40.6892", "-74.0445",
                    "New York, USA",
                    "landmark_statue_of_liberty",
                    "Statue_of_Liberty"
            },
            {
                    "Sydney Opera House",
                    "-33.8568", "151.2153",
                    "Sydney, Australia",
                    "landmark_sydney_opera_house",
                    "Sydney_Opera_House"
            },
            {
                    "Petra",
                    "30.3285", "35.4444",
                    "Ma'an, Jordan",
                    "landmark_petra",
                    "Petra"
            },
            {
                    "Chichen Itza",
                    "20.6843", "-88.5678",
                    "Yucatán, Mexico",
                    "landmark_chichen_itza",
                    "Chichen_Itza"
            },
            {
                    "Santorini",
                    "36.3932", "25.4615",
                    "Cyclades, Greece",
                    "landmark_santorini",
                    "Santorini"
            },
            {
                    "Angkor Wat",
                    "13.4125", "103.8670",
                    "Siem Reap, Cambodia",
                    "landmark_angkor_wat",
                    "Angkor_Wat"
            },
            {
                    "Acropolis of Athens",
                    "37.9715", "23.7267",
                    "Athens, Greece",
                    "landmark_acropolis",
                    "Acropolis_of_Athens"
            },
            {
                    "Christ the Redeemer",
                    "-22.9519", "-43.2105",
                    "Rio de Janeiro, Brazil",
                    "landmark_christ_redeemer",
                    "Christ_the_Redeemer"
            },
            {
                    "Stonehenge",
                    "51.1789", "-1.8262",
                    "Wiltshire, England",
                    "landmark_stonehenge",
                    "Stonehenge"
            },
            {
                    "Mount Fuji",
                    "35.3606", "138.7274",
                    "Honshu, Japan",
                    "landmark_mount_fuji",
                    "Mount_Fuji"
            },
            {
                    "Victoria Falls",
                    "-17.9243", "25.8572",
                    "Zambia/Zimbabwe",
                    "landmark_victoria_falls",
                    "Victoria_Falls"
            },
            {
                    "Burj Khalifa",
                    "25.1972", "55.2744",
                    "Dubai, UAE",
                    "landmark_burj_khalifa",
                    "Burj_Khalifa"
            },
            {
                    "Sagrada Família",
                    "41.4036", "2.1744",
                    "Barcelona, Spain",
                    "landmark_sagrada_familia",
                    "Sagrada_Familia"
            },
            {
                    "Northern Lights",
                    "69.6492", "18.9553",
                    "Tromsø, Norway",
                    "landmark_northern_lights",
                    "Aurora_borealis"
            },
            {
                    "Galápagos Islands",
                    "-0.9538", "-90.9656",
                    "Ecuador",
                    "landmark_galapagos",
                    "Galapagos_Islands"
            },
            {
                    "Grand Canyon",
                    "36.1069", "-112.1129",
                    "Arizona, USA",
                    "landmark_grand_canyon",
                    "Grand_Canyon"
            },
            {
                    "Amazon Rainforest",
                    "-3.4653", "-62.2159",
                    "South America",
                    "landmark_amazon",
                    "Amazon_rainforest"
            },
            {
                    "Hagia Sophia",
                    "41.0086", "28.9802",
                    "Istanbul, Turkey",
                    "landmark_hagia_sophia",
                    "Hagia_Sophia"
            },
            {
                    "Serengeti",
                    "-2.3333", "34.8333",
                    "Tanzania",
                    "landmark_serengeti",
                    "Serengeti"
            },
            {
                    "Iguazu Falls",
                    "-25.6953", "-54.4367",
                    "Argentina/Brazil",
                    "landmark_iguazu_falls",
                    "Iguazu_Falls"
            }
    };

    private static class LandmarkTag {
        final String description;
        final String wikiPageTitle;

        LandmarkTag(String description, String wikiPageTitle) {
            this.description   = description;
            this.wikiPageTitle = wikiPageTitle;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);
        searchInput = view.findViewById(R.id.searchInput);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchAndZoom(s.toString());
            }
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        for (String[] lm : LANDMARKS) {
            String name      = lm[0];
            double lat       = Double.parseDouble(lm[1]);
            double lng       = Double.parseDouble(lm[2]);
            String location  = lm[3];
            String stringKey = lm[4];
            String wikiTitle = lm[5];

            int resId = getResources().getIdentifier(
                    stringKey, "string", requireContext().getPackageName());
            String description = resId != 0 ? getString(resId) : stringKey;

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lng))
                    .title(name)
                    .snippet(location)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_ROSE)));

            if (marker != null) {
                marker.setTag(new LandmarkTag(description, wikiTitle));
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(20.0, 10.0), 2f));

        mMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() instanceof LandmarkTag) {
                LandmarkTag tag = (LandmarkTag) marker.getTag();
                // Show dialog immediately, then load image via API
                showLandmarkDialog(
                        marker.getTitle(),
                        marker.getSnippet(),
                        tag.description,
                        tag.wikiPageTitle
                );
            }
            return true;
        });

        checkLocationPermission();
    }

    // ─── Dialog ───────────────────────────────────────────────────────────────

    private void showLandmarkDialog(String name, String location,
                                    String description, String wikiPageTitle) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_landmark, null);

        ImageView  ivImage    = dialogView.findViewById(R.id.landmarkImage);
        ProgressBar pbImage   = dialogView.findViewById(R.id.imageProgressBar);
        TextView   tvName     = dialogView.findViewById(R.id.landmarkName);
        TextView   tvLocation = dialogView.findViewById(R.id.landmarkLocation);
        TextView   tvDesc     = dialogView.findViewById(R.id.landmarkDescription);
        Button     btnAskAI   = dialogView.findViewById(R.id.btnAskAI);

        tvName.setText(name);
        tvLocation.setText("📍 " + location);
        tvDesc.setText(description);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
        }

        btnAskAI.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(getActivity(), AIChatActivity.class);
            intent.putExtra("initial_message",
                    "Tell me more about " + name + " in " + location);
            getActivity().startActivity(intent);
        });

        dialog.show();

        // Fetch image URL from Wikipedia API after dialog is shown
        fetchWikipediaImage(wikiPageTitle, imageUrl -> {
            if (getActivity() == null || !dialog.isShowing()) return;

            getActivity().runOnUiThread(() -> {
                pbImage.setVisibility(View.GONE);
                if (imageUrl != null) {
                    Glide.with(SearchFragment.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .centerCrop()
                            .into(ivImage);
                } else {
                    ivImage.setImageResource(R.drawable.placeholder);
                }
            });
        });
    }

    // ─── Wikipedia API ────────────────────────────────────────────────────────

    private interface ImageCallback {
        void onResult(@Nullable String imageUrl);
    }

    private void fetchWikipediaImage(String pageTitle, ImageCallback callback) {
        String url = "https://en.wikipedia.org/w/api.php"
                + "?action=query"
                + "&titles=" + pageTitle
                + "&prop=pageimages"
                + "&format=json"
                + "&pithumbsize=800";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "TrekkrApp/1.0 (Android)")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onResult(null);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onResult(null);
                    return;
                }

                try {
                    String body = response.body().string();
                    JSONObject root  = new JSONObject(body);
                    JSONObject query = root.getJSONObject("query");
                    JSONObject pages = query.getJSONObject("pages");

                    // Pages is a map keyed by page ID — grab the first one
                    String pageId = pages.keys().next();
                    JSONObject page = pages.getJSONObject(pageId);

                    if (page.has("thumbnail")) {
                        String imageUrl = page.getJSONObject("thumbnail")
                                .getString("source");
                        callback.onResult(imageUrl);
                    } else {
                        callback.onResult(null);
                    }
                } catch (Exception e) {
                    callback.onResult(null);
                }
            }
        });
    }

    // ─── Search ───────────────────────────────────────────────────────────────

    private void searchAndZoom(String query) {
        if (mMap == null || query == null || query.trim().isEmpty()) return;
        String lower = query.toLowerCase(Locale.getDefault()).trim();

        for (String[] lm : LANDMARKS) {
            if (lm[0].toLowerCase(Locale.getDefault()).contains(lower) ||
                    lm[3].toLowerCase(Locale.getDefault()).contains(lower)) {

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(Double.parseDouble(lm[1]),
                                Double.parseDouble(lm[2])), 10f));
                return;
            }
        }

        Toast.makeText(requireContext(),
                "No matching landmark found", Toast.LENGTH_SHORT).show();
    }

    // ─── Location permission ──────────────────────────────────────────────────

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) requireActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableLocationFeatures();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void enableLocationFeatures() {
        if (mMap == null) return;
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        moveToUserLocation();
    }

    private void moveToUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(requireActivity());

        client.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(),
                                location.getLongitude()), 12f));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                isLocationEnabled()) {
            enableLocationFeatures();
        }
    }
}