package com.example.finalproject.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private EditText searchInput;
    private RecyclerView searchResults;
    private TextView emptyText;
    private ProgressBar progressBar;

    private PlaceAdapter placeAdapter;
    private List<Place> allPlaces;
    private List<Place> filteredPlaces;
    private DatabaseReference placesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize views
        searchInput = view.findViewById(R.id.searchInput);
        searchResults = view.findViewById(R.id.searchResults);
        emptyText = view.findViewById(R.id.emptyText);
        progressBar = view.findViewById(R.id.progressBar);

        // Initialize Firebase
        placesRef = FirebaseDatabase.getInstance().getReference("places");

        // Set up RecyclerView
        allPlaces = new ArrayList<>();
        filteredPlaces = new ArrayList<>();
        placeAdapter = new PlaceAdapter(getContext(), (ArrayList<Place>) filteredPlaces);

        searchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResults.setAdapter(placeAdapter);

        // Load all places
        loadPlaces();

        // Set up search functionality
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPlaces(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadPlaces() {
        progressBar.setVisibility(View.VISIBLE);

        placesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allPlaces.clear();

                for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                    Place place = placeSnapshot.getValue(Place.class);
                    if (place != null) {
                        if (place.getId() == null || place.getId().isEmpty()) {
                            place.setId(placeSnapshot.getKey());
                        }
                        allPlaces.add(place);
                    }
                }

                progressBar.setVisibility(View.GONE);

                // Show instruction text
                if (searchInput.getText().toString().isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText("Search for places by title or description");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void filterPlaces(String query) {
        filteredPlaces.clear();

        if (query.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("Search for places by title or description");
            searchResults.setVisibility(View.GONE);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());

            for (Place place : allPlaces) {
                if (place.getTitle().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        place.getDescription().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredPlaces.add(place);
                }
            }

            if (filteredPlaces.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
                emptyText.setText("No places found matching \"" + query + "\"");
                searchResults.setVisibility(View.GONE);
            } else {
                emptyText.setVisibility(View.GONE);
                searchResults.setVisibility(View.VISIBLE);
            }

            placeAdapter.notifyDataSetChanged();
        }
    }
}