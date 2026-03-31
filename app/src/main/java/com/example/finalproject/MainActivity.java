package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.finalproject.fragments.HomeFragment;
import com.example.finalproject.fragments.ProfileFragment;
import com.example.finalproject.fragments.SearchFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private Toolbar toolbar;

    // Bottom navigation buttons
    private ImageButton btnHome;
    private ImageButton btnAI;
    private FloatingActionButton fabAdd;
    private ImageButton btnSearch;
    private ImageButton btnProfile;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Check if user is logged in
        if (firebaseAuth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Set up toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Trekkr");
        }

        // Initialize bottom navigation buttons
        btnHome = findViewById(R.id.btnHome);
        btnAI = findViewById(R.id.btnAI);
        fabAdd = findViewById(R.id.fabAdd);
        btnSearch = findViewById(R.id.btnSearch);
        btnProfile = findViewById(R.id.btnProfile);

        // Set up bottom navigation click listeners
        setupBottomNavigation();

        // Load Home Fragment by default
        if (savedInstanceState == null) {
            currentFragment = new HomeFragment();
            loadFragment(currentFragment);
            highlightButton(btnHome);
        }
    }

    private void setupBottomNavigation() {
        // Home button - Refresh if already on home, otherwise go to home
        btnHome.setOnClickListener(v -> {
            if (currentFragment instanceof HomeFragment) {
                // Refresh the current home fragment
                loadFragment(new HomeFragment());
                Toast.makeText(this, "Feed refreshed", Toast.LENGTH_SHORT).show();
            } else {
                // Navigate to home
                currentFragment = new HomeFragment();
                loadFragment(currentFragment);
            }
            highlightButton(btnHome);
        });

        // Map button - Open Google Maps
        btnAI.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AIChatActivity.class);
            startActivity(intent);
            highlightButton(btnAI);
        });

        // Add button (Large FAB)
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPlaceActivity.class);
            startActivity(intent);
        });

        // Search button - Open SearchFragment
        btnSearch.setOnClickListener(v -> {
            currentFragment = new SearchFragment();
            loadFragment(currentFragment);
            highlightButton(btnSearch);
        });

        // Profile button - Open ProfileFragment
        btnProfile.setOnClickListener(v -> {
            currentFragment = new ProfileFragment();
            loadFragment(currentFragment);
            highlightButton(btnProfile);
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void highlightButton(ImageButton selectedButton) {
        // Reset all buttons to default color
        btnHome.setColorFilter(getResources().getColor(R.color.text_secondary));
        btnAI.setColorFilter(getResources().getColor(R.color.text_secondary));
        btnSearch.setColorFilter(getResources().getColor(R.color.text_secondary));
        btnProfile.setColorFilter(getResources().getColor(R.color.text_secondary));

        // Highlight selected button
        selectedButton.setColorFilter(getResources().getColor(R.color.primary_coral));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mmenu_theme) {
            Toast.makeText(this, "Theme changing coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.menu_profile) {
            Intent i = new Intent(MainActivity.this, ProfileFragment.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload home fragment when returning to app
        if (currentFragment instanceof HomeFragment) {
            loadFragment(new HomeFragment());
        }
    }
}