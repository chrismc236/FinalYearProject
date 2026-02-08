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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private TextView profileEmail;
    private TextView profileName;
    private TextView profilePostsCount;
    private Button btnEditProfile;
    private Button btnSettings;
    private Button btnLogout;

    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

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

            // Navigate to login screen
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
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            // Set email
            profileEmail.setText(user.getEmail());

            // Set name (placeholder - you can get this from Firebase Database later)
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                profileName.setText(displayName);
            } else {
                profileName.setText("Traveler");
            }

            // Placeholder for posts count
            profilePostsCount.setText("0 Posts");
        }
    }
}