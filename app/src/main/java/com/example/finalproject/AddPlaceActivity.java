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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.models.Place;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class AddPlaceActivity extends AppCompatActivity {

    private static final String TAG = "AddPlaceActivity";
    private static final int CAMERA_REQUEST  = 100;
    private static final int GALLERY_REQUEST = 101;

    // Views
    private ImageView    previewImage;
    private LinearLayout imagePlaceholder;
    private EditText     titleInput, descInput, locationInput, tipsInput;
    private Button       btnTakePhoto, btnChooseGallery, btnSave, btnShareSocial;
    private ProgressBar  progressBar;
    private TextView     tvUploadStatus;
    private Spinner      spinnerCategory;
    private ChipGroup    chipGroupMood;

    // Image state
    private Bitmap capturedBitmap = null;
    private Uri cameraImageUri;
    private Uri    galleryUri     = null;

    // Firebase
    private DatabaseReference dbRef;
    private StorageReference  storageRef;
    private FirebaseAuth      firebaseAuth;

    // Saved after upload (used for share + AI)
    private Place savedPlace = null;

    // ─────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        bindViews();
        setupCategorySpinner();
        setupFirebase();
        setupClickListeners();
    }

    // ─────────────────────────────────────────────
    private void bindViews() {
        previewImage      = findViewById(R.id.previewImage);
        imagePlaceholder  = findViewById(R.id.imagePlaceholder);
        titleInput        = findViewById(R.id.inputTitle);
        descInput         = findViewById(R.id.inputDescription);
        locationInput     = findViewById(R.id.inputLocation);
        tipsInput         = findViewById(R.id.inputTips);
        btnTakePhoto      = findViewById(R.id.btnTakePhoto);
        btnChooseGallery  = findViewById(R.id.btnChooseGallery);
        btnSave           = findViewById(R.id.btnSave);
        btnShareSocial    = findViewById(R.id.btnShareSocial);
        progressBar       = findViewById(R.id.progressBar);
        tvUploadStatus    = findViewById(R.id.tvUploadStatus);
        spinnerCategory   = findViewById(R.id.spinnerCategory);
        chipGroupMood     = findViewById(R.id.chipGroupMood);
    }

    private void setupCategorySpinner() {
        String[] categories = {
                "🗺️  Select Category",
                "🏖️  Beach",
                "🏔️  Mountain",
                "🏙️  City",
                "🌿  Nature",
                "🏛️  Landmark",
                "🍽️  Food & Drink",
                "🎭  Culture & Art",
                "🛍️  Shopping",
                "🏕️  Camping",
                "🌊  Water Sports",
                "🧘  Wellness"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupFirebase() {
        dbRef        = FirebaseDatabase.getInstance().getReference("places");
        storageRef   = FirebaseStorage.getInstance().getReference("places_images");
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void setupClickListeners() {
        btnTakePhoto.setOnClickListener(v -> openCamera());
        btnChooseGallery.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> uploadPlace());
        btnShareSocial.setOnClickListener(v -> sharePlaceToSocialMedia());
    }

    // ─── Camera ───────────────────────────────────
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                java.io.File imageFile = createImageFile();

                cameraImageUri = androidx.core.content.FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        imageFile
                );

                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                startActivityForResult(intent, CAMERA_REQUEST);

            } catch (Exception e) {
                Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private java.io.File createImageFile() throws java.io.IOException {
        String fileName = "JPEG_" + System.currentTimeMillis();
        java.io.File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        return java.io.File.createTempFile(fileName, ".jpg", storageDir);
    }


    // ─── Gallery ──────────────────────────────────
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    // ─── Activity Result ─────────────────────────
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == CAMERA_REQUEST) {
            if (cameraImageUri != null) {
                galleryUri = cameraImageUri;
                capturedBitmap = null;

                try {
                    Bitmap bmp = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), cameraImageUri);

                    previewImage.setImageBitmap(bmp);
                    imagePlaceholder.setVisibility(View.GONE);

                } catch (IOException e) {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == GALLERY_REQUEST) {
            galleryUri     = data.getData();
            capturedBitmap = null;
            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), galleryUri);
                previewImage.setImageBitmap(bmp);
                imagePlaceholder.setVisibility(View.GONE);
            } catch (IOException e) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ─── Validation ───────────────────────────────
    private boolean hasImage()  { return capturedBitmap != null || galleryUri != null; }

    private String getSelectedMood() {
        int checkedId = chipGroupMood.getCheckedChipId();
        if (checkedId == View.NO_ID) return "";
        Chip chip = findViewById(checkedId);
        return chip != null ? chip.getText().toString() : "";
    }

    private String getSelectedCategory() {
        int pos = spinnerCategory.getSelectedItemPosition();
        return pos == 0 ? "" : spinnerCategory.getSelectedItem().toString();
    }

    // ─── Upload ───────────────────────────────────
    private void uploadPlace() {
        if (!hasImage()) {
            Toast.makeText(this, "Please add a photo first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String title    = titleInput.getText().toString().trim();
        String desc     = descInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String tips     = tipsInput.getText().toString().trim();

        if (title.isEmpty()) {
            titleInput.setError("Title is required");
            titleInput.requestFocus();
            return;
        }
        if (desc.isEmpty()) {
            descInput.setError("Description is required");
            descInput.requestFocus();
            return;
        }
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();

        setUploading(true);

        // Convert image to bytes
        byte[] imageBytes = getImageBytes();
        if (imageBytes == null) {
            setUploading(false);
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = storageRef.child(fileName);

        UploadTask uploadTask = fileRef.putBytes(imageBytes);

        uploadTask.addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            progressBar.setProgress((int) progress);
            tvUploadStatus.setText("Uploading… " + (int) progress + "%");
        }).addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String id        = dbRef.push().getKey();
                    long   timestamp = System.currentTimeMillis();

                    // Build the Place
                    Place place = new Place(title, desc, uri.toString(), timestamp, userId);
                    place.setId(id);
                    place.setLocationTag(location);
                    place.setTips(tips);
                    place.setCategory(getSelectedCategory());
                    place.setMood(getSelectedMood());

                    dbRef.child(id).setValue(place).addOnCompleteListener(dbTask -> {
                        setUploading(false);
                        if (dbTask.isSuccessful()) {
                            savedPlace = place;
                            tvUploadStatus.setText("✅ Posted successfully!");
                            tvUploadStatus.setVisibility(View.VISIBLE);
                            btnShareSocial.setVisibility(View.VISIBLE);
                            btnSave.setText("✓ Posted!");
                            btnSave.setEnabled(false);
                            Toast.makeText(this, "Adventure shared!", Toast.LENGTH_SHORT).show();

                            // Signal home to refresh
                            Intent result = new Intent();
                            result.putExtra("upload_success", true);
                            setResult(RESULT_OK, result);
                        } else {
                            String err = dbTask.getException() != null
                                    ? dbTask.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Save failed: " + err, Toast.LENGTH_LONG).show();
                        }
                    });
                }).addOnFailureListener(e -> {
                    setUploading(false);
                    Toast.makeText(this, "Failed to get URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
        ).addOnFailureListener(e -> {
            setUploading(false);
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private byte[] getImageBytes() {
        try {
            Bitmap bmp;
            if (capturedBitmap != null) {
                bmp = capturedBitmap;
            } else {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), galleryUri);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "getImageBytes error: " + e.getMessage());
            return null;
        }
    }

    private void setUploading(boolean uploading) {
        progressBar.setVisibility(uploading ? View.VISIBLE : View.GONE);
        tvUploadStatus.setVisibility(View.VISIBLE);
        btnSave.setEnabled(!uploading);
        btnTakePhoto.setEnabled(!uploading);
        btnChooseGallery.setEnabled(!uploading);
        if (!uploading) progressBar.setProgress(0);
    }

    // ─── Share to Social Media ────────────────────
    private void sharePlaceToSocialMedia() {
        if (savedPlace == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("🌍 ").append(savedPlace.getTitle());

        if (savedPlace.getLocationTag() != null && !savedPlace.getLocationTag().isEmpty()) {
            sb.append("\n📍 ").append(savedPlace.getLocationTag());
        }
        if (savedPlace.getCategory() != null && !savedPlace.getCategory().isEmpty()) {
            sb.append("\n").append(savedPlace.getCategory());
        }
        sb.append("\n\n").append(savedPlace.getDescription());

        if (savedPlace.getTips() != null && !savedPlace.getTips().isEmpty()) {
            sb.append("\n\n💡 Tips: ").append(savedPlace.getTips());
        }
        if (savedPlace.getMood() != null && !savedPlace.getMood().isEmpty()) {
            sb.append("\n\nVibes: ").append(savedPlace.getMood());
        }
        sb.append("\n\n#Trekkr #Travel #Adventure");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Share your adventure via…"));
    }

    // ─── Open in AI Agent ─────────────────────────
    /**
     * Called from PlaceAdapter / HomeFragment when user taps
     * "Ask AI about this place". Pass a Place object in the intent.
     */
    public static Intent buildAiIntent(android.content.Context ctx, Place place) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Tell me more about this travel destination:\n\n");
        prompt.append("📍 Place: ").append(place.getTitle()).append("\n");

        if (place.getLocationTag() != null && !place.getLocationTag().isEmpty())
            prompt.append("Location: ").append(place.getLocationTag()).append("\n");

        prompt.append("Description: ").append(place.getDescription()).append("\n");

        if (place.getCategory() != null && !place.getCategory().isEmpty())
            prompt.append("Category: ").append(place.getCategory()).append("\n");

        if (place.getMood() != null && !place.getMood().isEmpty())
            prompt.append("Vibe: ").append(place.getMood()).append("\n");

        if (place.getTips() != null && !place.getTips().isEmpty())
            prompt.append("Existing tips: ").append(place.getTips()).append("\n");

        prompt.append("\nPlease give me:\n");
        prompt.append("• More things to do nearby\n");
        prompt.append("• Best time to visit\n");
        prompt.append("• Local food recommendations\n");
        prompt.append("• Hidden gems in the area\n");
        prompt.append("• Any travel tips I should know");

        Intent intent = new Intent(ctx, AIChatActivity.class);
        intent.putExtra("auto_prompt", prompt.toString());
        return intent;
    }
}