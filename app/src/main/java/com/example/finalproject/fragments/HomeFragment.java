package com.example.finalproject.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.adapters.PlaceAdapter;
import com.example.finalproject.models.Place;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Home Fragment - Displays the feed of places
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateText;

    private PlaceAdapter placeAdapter;
    private List<Place> placeList;
    private DatabaseReference placesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase
        placesRef = FirebaseDatabase.getInstance().getReference("places");

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        // Set up RecyclerView
        placeList = new ArrayList<>();
        placeAdapter = new PlaceAdapter(getContext(), (ArrayList<Place>) placeList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(placeAdapter);

        // Load places
        loadPlaces();

        return view;
    }

    private void loadPlaces() {
        Log.d(TAG, "Loading places from Firebase...");
        progressBar.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);

        placesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                placeList.clear();

                Log.d(TAG, "Data snapshot received. Number of children: " + snapshot.getChildrenCount());

                if (!snapshot.exists()) {
                    Log.d(TAG, "No places found in database");
                    progressBar.setVisibility(View.GONE);
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    emptyStateText.setText("Start sharing your travel moments!");
                    return;
                }

                for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                    try {
                        Place place = placeSnapshot.getValue(Place.class);
                        if (place != null) {
                            if (place.getId() == null || place.getId().isEmpty()) {
                                place.setId(placeSnapshot.getKey());
                            }
                            placeList.add(place);
                            Log.d(TAG, "Loaded place: " + place.getTitle());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing place: " + e.getMessage());
                    }
                }

                // Sort by timestamp (newest first)
                Collections.sort(placeList, (p1, p2) ->
                        Long.compare(p2.getTimestamp(), p1.getTimestamp()));

                placeAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (placeList.isEmpty()) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    emptyStateText.setText("No places found");
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                    Log.d(TAG, "Successfully loaded " + placeList.size() + " places");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.VISIBLE);
                emptyStateText.setText("Failed to load places");
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload places when fragment is resumed
        if (placesRef != null) {
            loadPlaces();
        }
    }
}