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

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateText;

    private PlaceAdapter placeAdapter;
    private List<Place> placeList;
    private DatabaseReference placesRef;

    // Store the listener so we can remove it later
    private ValueEventListener placesListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        placesRef = FirebaseDatabase.getInstance().getReference("places");

        recyclerView      = view.findViewById(R.id.recyclerView);
        progressBar       = view.findViewById(R.id.progressBar);
        emptyStateLayout  = view.findViewById(R.id.emptyStateLayout);
        emptyStateText    = view.findViewById(R.id.emptyStateText);

        placeList   = new ArrayList<>();
        placeAdapter = new PlaceAdapter(getContext(), (ArrayList<Place>) placeList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(placeAdapter);

        // Attach the persistent listener once, here in onCreateView
        attachListener();

        return view;
    }

    private void attachListener() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);

        placesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                placeList.clear();

                for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                    try {
                        Place place = placeSnapshot.getValue(Place.class);
                        if (place != null) {
                            if (place.getId() == null || place.getId().isEmpty()) {
                                place.setId(placeSnapshot.getKey());
                            }
                            placeList.add(place);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing place: " + e.getMessage());
                    }
                }

                Collections.sort(placeList, (p1, p2) ->
                        Long.compare(p2.getTimestamp(), p1.getTimestamp()));

                placeAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (placeList.isEmpty()) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    emptyStateText.setText("No places found. Start sharing!");
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.VISIBLE);
                emptyStateText.setText("Failed to load places");
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        };

        placesRef.addValueEventListener(placesListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove the listener when the fragment is not visible
        // This prevents the double-registration bug when onResume fires
        if (placesRef != null && placesListener != null) {
            placesRef.removeEventListener(placesListener);
            placesListener = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Re-attach listener when fragment becomes visible again
        // Only attach if it was removed (i.e. placesListener is null)
        if (placesRef != null && placesListener == null) {
            attachListener();
        }
    }

    // onResume override removed entirely — it was the source of the bug
}