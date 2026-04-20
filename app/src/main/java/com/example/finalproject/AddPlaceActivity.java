package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AddPlaceActivity extends AppCompatActivity {

    private static final String TAG            = "AddPlaceActivity";
    private static final int    CAMERA_REQUEST  = 100;
    private static final int    GALLERY_REQUEST = 101;

    // ── Views ────────────────────────────────────────────────────────────────
    private ImageView    previewImage;
    private LinearLayout imagePlaceholder;
    private EditText     titleInput, descInput, locationInput, tipsInput;
    private Button       btnTakePhoto, btnChooseGallery, btnSave, btnShareSocial;
    private ProgressBar  progressBar;
    private TextView     tvUploadStatus;
    private Spinner      spinnerCategory;
    private ChipGroup    chipGroupMood;

    // ── Image state ──────────────────────────────────────────────────────────
    private Bitmap capturedBitmap = null;  // kept for backward compat (thumbnail fallback)
    private Uri    cameraImageUri = null;  // full-res URI written by camera via FileProvider
    private Uri    galleryUri     = null;  // URI selected from gallery

    // ── App state ────────────────────────────────────────────────────────────
    private Place savedPlace = null;  // populated after a successful upload (used by share / AI)

    // ── Firebase ─────────────────────────────────────────────────────────────
    private DatabaseReference dbRef;
    private StorageReference  storageRef;
    private FirebaseAuth      firebaseAuth;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        bindViews();
        setupCategorySpinner();
        setupFirebase();
        setupClickListeners();
    }

    // ── View binding ──────────────────────────────────────────────────────────
    private void bindViews() {
        previewImage     = findViewById(R.id.previewImage);
        imagePlaceholder = findViewById(R.id.imagePlaceholder);
        titleInput       = findViewById(R.id.inputTitle);
        descInput        = findViewById(R.id.inputDescription);
        locationInput    = findViewById(R.id.inputLocation);
        tipsInput        = findViewById(R.id.inputTips);
        btnTakePhoto     = findViewById(R.id.btnTakePhoto);
        btnChooseGallery = findViewById(R.id.btnChooseGallery);
        btnSave          = findViewById(R.id.btnSave);
        btnShareSocial   = findViewById(R.id.btnShareSocial);
        progressBar      = findViewById(R.id.progressBar);
        tvUploadStatus   = findViewById(R.id.tvUploadStatus);
        spinnerCategory  = findViewById(R.id.spinnerCategory);
        chipGroupMood    = findViewById(R.id.chipGroupMood);
    }

    // ── Category spinner ──────────────────────────────────────────────────────
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

    // ── Firebase ──────────────────────────────────────────────────────────────
    private void setupFirebase() {
        dbRef        = FirebaseDatabase.getInstance().getReference("places");
        storageRef   = FirebaseStorage.getInstance().getReference("places_images");
        firebaseAuth = FirebaseAuth.getInstance();
    }

    // ── Click listeners ───────────────────────────────────────────────────────
    private void setupClickListeners() {
        btnTakePhoto    .setOnClickListener(v -> openCamera());
        btnChooseGallery.setOnClickListener(v -> openGallery());
        btnSave         .setOnClickListener(v -> uploadPlace());
        btnShareSocial  .setOnClickListener(v -> sharePlaceToSocialMedia());
    }

    // ── Camera ────────────────────────────────────────────────────────────────
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                File imageFile = createImageFile();

                // NOTE: authority must match the <provider> entry in AndroidManifest.xml
                // Use getPackageName() + ".provider"
                cameraImageUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        imageFile
                );

                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                startActivityForResult(intent, CAMERA_REQUEST);

            } catch (Exception e) {
                Log.e(TAG, "Error opening camera: " + e.getMessage());
                Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String fileName = "JPEG_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    // ── Gallery ───────────────────────────────────────────────────────────────
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    // ── onActivityResult ──────────────────────────────────────────────────────
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        if (requestCode == CAMERA_REQUEST) {
            if (cameraImageUri != null) {
                // Full-resolution photo saved to our FileProvider URI
                galleryUri     = cameraImageUri;
                capturedBitmap = null; // clear any old thumbnail

                try {
                    Bitmap bmp = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(cameraImageUri));
                    showPreview(bmp);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to load camera photo: " + e.getMessage());
                    Toast.makeText(this, "Failed to load photo", Toast.LENGTH_SHORT).show();
                }

            } else if (data != null && data.getExtras() != null) {
                // Fallback: low-res thumbnail (no FileProvider set up)
                capturedBitmap = (Bitmap) data.getExtras().get("data");
                galleryUri     = null;
                showPreview(capturedBitmap);
            }

        } else if (requestCode == GALLERY_REQUEST && data != null && data.getData() != null) {
            galleryUri     = data.getData();
            capturedBitmap = null;

            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), galleryUri);
                showPreview(bmp);
            } catch (IOException e) {
                Log.e(TAG, "Failed to load gallery image: " + e.getMessage());
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Puts the bitmap in the ImageView and hides the placeholder overlay. */
    private void showPreview(Bitmap bitmap) {
        if (bitmap == null) return;
        previewImage.setImageBitmap(bitmap);
        imagePlaceholder.setVisibility(View.GONE);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** True if either a camera URI or a gallery URI has been set. */
    private boolean hasImage() {
        return capturedBitmap != null || galleryUri != null;
    }

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

    /**
     * Converts whichever image source is active (camera URI or gallery URI
     * or in-memory bitmap) into a JPEG byte array ready for Firebase Storage.
     */
    private byte[] getImageBytes() {
        try {
            Bitmap bmp;
            if (galleryUri != null) {
                // Covers both full-res camera (FileProvider) and gallery picks
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), galleryUri);
            } else {
                // Fallback thumbnail captured as a Bitmap directly
                bmp = capturedBitmap;
            }
            if (bmp == null) return null;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "getImageBytes error: " + e.getMessage());
            return null;
        }
    }

    // ── Upload ────────────────────────────────────────────────────────────────
    private void uploadPlace() {
        if (!hasImage()) {
            Toast.makeText(this, "Please add a photo first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String title    = titleInput   .getText().toString().trim();
        String desc     = descInput    .getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String tips     = tipsInput    .getText().toString().trim();

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
                    tvUploadStatus.setText("Saving…");

                    String id        = dbRef.push().getKey();
                    long   timestamp = System.currentTimeMillis();

                    // Build the Place object with all fields
                    Place place = new Place(title, desc, uri.toString(), timestamp, userId);
                    place.setId(id);
                    place.setLocationTag(location);
                    place.setTips(tips);
                    place.setCategory(getSelectedCategory());
                    place.setMood(getSelectedMood());

                    dbRef.child(id).setValue(place).addOnCompleteListener(dbTask -> {
                        setUploading(false);
                        if (dbTask.isSuccessful()) {
                            savedPlace = place; // store for share / AI

                            tvUploadStatus.setText("✅ Posted successfully!");
                            tvUploadStatus.setVisibility(View.VISIBLE);
                            btnShareSocial.setVisibility(View.VISIBLE);
                            btnSave.setText("✓ Posted!");
                            btnSave.setEnabled(false);

                            Toast.makeText(AddPlaceActivity.this,
                                    "Adventure shared!", Toast.LENGTH_SHORT).show();

                            Intent result = new Intent();
                            result.putExtra("upload_success", true);
                            setResult(RESULT_OK, result);

                        } else {
                            String err = dbTask.getException() != null
                                    ? dbTask.getException().getMessage() : "Unknown error";
                            Toast.makeText(AddPlaceActivity.this,
                                    "Save failed: " + err, Toast.LENGTH_LONG).show();
                        }
                    });

                }).addOnFailureListener(e -> {
                    setUploading(false);
                    Toast.makeText(this, "Failed to get URL: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                })

        ).addOnFailureListener(e -> {
            setUploading(false);
            Toast.makeText(this, "Upload failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    /** Enable/disable all interactive controls while an upload is in progress. */
    private void setUploading(boolean uploading) {
        progressBar     .setVisibility(uploading ? View.VISIBLE : View.GONE);
        tvUploadStatus  .setVisibility(View.VISIBLE);
        btnSave         .setEnabled(!uploading);
        btnTakePhoto    .setEnabled(!uploading);
        btnChooseGallery.setEnabled(!uploading);
        if (!uploading) progressBar.setProgress(0);
    }

    // ── Share to social media ─────────────────────────────────────────────────
    private void sharePlaceToSocialMedia() {
        if (savedPlace == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("🌍 ").append(savedPlace.getTitle());

        if (savedPlace.getLocationTag() != null && !savedPlace.getLocationTag().isEmpty())
            sb.append("\n📍 ").append(savedPlace.getLocationTag());

        if (savedPlace.getCategory() != null && !savedPlace.getCategory().isEmpty())
            sb.append("\n").append(savedPlace.getCategory());

        sb.append("\n\n").append(savedPlace.getDescription());

        if (savedPlace.getTips() != null && !savedPlace.getTips().isEmpty())
            sb.append("\n\n💡 Tips: ").append(savedPlace.getTips());

        if (savedPlace.getMood() != null && !savedPlace.getMood().isEmpty())
            sb.append("\n\nVibes: ").append(savedPlace.getMood());

        sb.append("\n\n#Trekkr #Travel #Adventure");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Share your adventure via…"));
    }

    // ── Static AI intent builder (called from adapter / home fragment) ─────────
    /**
     * Builds an intent that opens AIChatActivity pre-filled with details about
     * the given Place. Call from PlaceAdapter or HomeFragment when the user
     * taps "Ask AI about this place".
     */
    public static Intent buildAiIntent(Context ctx, Place place) {
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