package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.finalproject.models.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class AddPlaceActivity extends AppCompatActivity {

    private static final String TAG = "AddPlaceActivity";
    private static final int CAMERA_REQUEST = 100;

    private ImageView previewImage;
    private EditText titleInput, descInput;
    private Button btnTakePhoto, btnSave;
    private ProgressBar progressBar;

    private Bitmap capturedBitmap;

    private DatabaseReference dbRef;
    private StorageReference storageRef;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        // Initialize views
        previewImage = findViewById(R.id.previewImage);
        titleInput = findViewById(R.id.inputTitle);
        descInput = findViewById(R.id.inputDescription);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Firebase
        dbRef = FirebaseDatabase.getInstance().getReference("places");
        storageRef = FirebaseStorage.getInstance().getReference("places_images");
        firebaseAuth = FirebaseAuth.getInstance();

        // Button listeners
        btnTakePhoto.setOnClickListener(v -> openCamera());
        btnSave.setOnClickListener(v -> uploadPlace());
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                capturedBitmap = (Bitmap) extras.get("data");
                previewImage.setImageBitmap(capturedBitmap);
                Log.d(TAG, "Photo captured successfully");
            }
        }
    }

    private void uploadPlace() {
        // Validation
        if (capturedBitmap == null) {
            Toast.makeText(this, "Take a picture first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = titleInput.getText().toString().trim();
        String desc = descInput.getText().toString().trim();

        if (title.isEmpty()) {
            titleInput.setError("Required");
            titleInput.requestFocus();
            return;
        }

        if (desc.isEmpty()) {
            descInput.setError("Required");
            descInput.requestFocus();
            return;
        }

        // Check authentication
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user ID
        String userId = firebaseAuth.getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);

        // Convert bitmap to bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = storageRef.child(fileName);

        // Upload to Firebase Storage
        UploadTask uploadTask = fileRef.putBytes(data);

        uploadTask.addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            progressBar.setProgress((int) progress);
        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this, "Upload finished, getting URL...", Toast.LENGTH_SHORT).show();

            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Toast.makeText(this, "Got URL, saving metadata...", Toast.LENGTH_SHORT).show();

                String id = dbRef.push().getKey();
                if (id == null) {
                    Toast.makeText(this, "Failed to generate push key!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                long timestamp = System.currentTimeMillis();

                // Create Place with userId
                Place place = new Place(title, desc, uri.toString(), timestamp, userId);
                place.setId(id);

                dbRef.child(id).setValue(place).addOnCompleteListener(dbTask -> {
                    progressBar.setVisibility(View.GONE);

                    if (dbTask.isSuccessful()) {
                        Toast.makeText(AddPlaceActivity.this, "Saved to database!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra("upload_success", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        String error = dbTask.getException() != null ?
                                dbTask.getException().getMessage() : "Unknown error";
                        Toast.makeText(AddPlaceActivity.this,
                                "Database save failed: " + error,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to get URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}