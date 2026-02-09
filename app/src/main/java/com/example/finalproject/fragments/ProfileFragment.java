package com.example.finalproject.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalproject.LoginActivity;
import com.example.finalproject.R;
import com.example.finalproject.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView profileEmail;
    private TextView profileName;
    private TextView profilePostsCount;
    private Button btnEditProfile;
    private Button btnSettings;
    private Button btnLogout;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;
    private DatabaseReference placesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        placesRef = FirebaseDatabase.getInstance().getReference("places");

        // Initialize views
        profileEmail = view.findViewById(R.id.profileEmail);
        profileName = view.findViewById(R.id.profileName);
        profilePostsCount = view.findViewById(R.id.profilePostsCount);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnSettings = view.findViewById(R.id.btnSettings);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Load user data
        loadUserData();

        // Set up button listeners
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Edit profile feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnSettings.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Settings feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        return view;
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            // Load user info from database
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        // Set name
                        profileName.setText(user.getName() != null ? user.getName() : "Traveler");
                        // Set email
                        profileEmail.setText(user.getEmail());
                    } else {
                        // Fallback if user data not in database
                        profileName.setText("Traveler");
                        profileEmail.setText(firebaseUser.getEmail());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Fallback on error
                    profileName.setText("Traveler");
                    profileEmail.setText(firebaseUser.getEmail());
                }
            });

            // Count user's posts
            Query userPlacesQuery = placesRef.orderByChild("userId").equalTo(userId);
            userPlacesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long postCount = snapshot.getChildrenCount();
                    profilePostsCount.setText(postCount + (postCount == 1 ? " Post" : " Posts"));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    profilePostsCount.setText("0 Posts");
                }
            });
        }
    }
}