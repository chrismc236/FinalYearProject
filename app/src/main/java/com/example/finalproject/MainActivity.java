package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.finalproject.adapters.PlaceAdapter;
import com.example.finalproject.models.Place;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddPost;
    private Toolbar toolbar;
    private ArrayList<Place> placeList;
    private PlaceAdapter adapter;
    private DatabaseReference dbRef;
    private static final int ADD_PLACE_REQUEST = 1001;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() == null){
            // the user is not logged in, should never get to this point, redirects to login
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
            return;
        }

// check firebase is working
        checkFirebaseInitialization();

        // Toolbar setup
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("TREKKR");
        }

        fabAddPost = findViewById(R.id.fabAddPost);
        fabAddPost.setOnClickListener(v -> {
                    Intent i = new Intent(MainActivity.this, AddPlaceActivity.class);
                    startActivity(i);
        });

        String userName = firebaseAuth.getCurrentUser().getEmail();
        Toast.makeText(this, "Welcome " + userName, Toast.LENGTH_SHORT).show();

// --- Initialize UI ---
        FloatingActionButton fab = findViewById(R.id.fabAddPost);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

// Buttons
        Button btnUpload = findViewById(R.id.openUploadActivity);
        Button btnTutorial = findViewById(R.id.openTutorialsButton);
        Button btnSettings = findViewById(R.id.openSettings);

// --- Setup adapter ---
        placeList = new ArrayList<>();
        adapter = new PlaceAdapter(this, placeList);
        recyclerView.setAdapter(adapter);

// --- Firebase reference ---
        dbRef = FirebaseDatabase.getInstance().getReference("places");

// --- Load places from Firebase ---
        loadPlaces();

// --- FAB action ---
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPlaceActivity.class);
            startActivityForResult(intent, ADD_PLACE_REQUEST);
        });

// --- Button actions ---
        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPlaceActivity.class);
            startActivityForResult(intent, ADD_PLACE_REQUEST);
        });

        btnTutorial.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TutorialActivity.class));
        });

        btnSettings.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
            boolean dark = prefs.getBoolean("darkMode", false);
            prefs.edit().putBoolean("darkMode", !dark).apply();
            recreate(); // restart activity to apply theme
        });

    }

    private void loadPlaces() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                placeList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Place place = ds.getValue(Place.class);
                    if (place != null) {
                        place.setId(ds.getKey());
                        placeList.add(place);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load places: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean dark = prefs.getBoolean("darkMode", false);

        if (dark) {
            setTheme(R.style.Theme_MySpot_Dark);
        } else {
            setTheme(R.style.Theme_MySpot_Light);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menuToggleTheme) {
            SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
            boolean dark = prefs.getBoolean("darkMode", false);
            prefs.edit().putBoolean("darkMode", !dark).apply();
            recreate();
            return true;
        }

        if(item.getItemId() == R.id.menu_logout){
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return true;
        }

        if(item.getItemId() == R.id.menu_profile){
            // TODO open profile activity
            Toast.makeText(this, "Profile page coming soon", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_PLACE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("upload_success", false)) {
                Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show();
                // optionally refresh your RecyclerView here
            }
        }
    }

    public void checkFirebaseInitialization() {
// 1. Check if FirebaseApp is initialized
        if (FirebaseApp.getApps(this).isEmpty()) {
            Toast.makeText(this, "Firebase NOT initialized!", Toast.LENGTH_LONG).show();
            return;
        } else {
            Toast.makeText(this, "Firebase initialized successfully!", Toast.LENGTH_SHORT).show();
        }

// 2. Check DatabaseReference
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("places");
        if (dbRef == null) {
            Toast.makeText(this, "Database reference is null!", Toast.LENGTH_LONG).show();
            return;
        } else {
            Toast.makeText(this, "Database reference: " + dbRef.toString(), Toast.LENGTH_SHORT).show();
        }

// 3. Try generating a push key
        String id = dbRef.push().getKey();
        if (id == null) {
            Toast.makeText(this, "Failed to generate push key! DB not ready.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Generated push key: " + id, Toast.LENGTH_SHORT).show();
        }
    }

}
