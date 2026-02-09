package com.example.finalproject.adapters;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalproject.R;
import com.example.finalproject.models.Place;
import com.example.finalproject.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private static final String TAG = "PlaceAdapter";
    private Context context;
    private ArrayList<Place> placeList;
    private Map<String, String> userNamesCache; // Cache for user names

    public PlaceAdapter(Context context, ArrayList<Place> placeList) {
        this.context = context;
        this.placeList = placeList;
        this.userNamesCache = new HashMap<>();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);

        holder.placeTitle.setText(place.getTitle());
        holder.placeDescription.setText(place.getDescription());

        // Format timestamp
        holder.placeTimestamp.setText(android.text.format.DateFormat.format(
                "dd/MM/yyyy hh:mm a", place.getTimestamp()));

        // Load username
        loadUsername(place.getUserId(), holder.placeUsername);

        // Load image
        Glide.with(context)
                .load(place.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.placeImage);

        holder.itemView.setOnClickListener(v -> showPlacePopup(place));

        // Long press for delete
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Place")
                    .setMessage("Are you sure you want to delete this place?")
                    .setPositiveButton("Yes", (dialog, which) -> deletePlace(place, position))
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });
    }

    private void loadUsername(String userId, TextView usernameTextView) {
        if (userId == null || userId.isEmpty()) {
            usernameTextView.setText("Unknown User");
            return;
        }

        // Check cache first
        if (userNamesCache.containsKey(userId)) {
            usernameTextView.setText(userNamesCache.get(userId));
            return;
        }

        // Load from Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && user.getName() != null) {
                    String username = user.getName();
                    userNamesCache.put(userId, username); // Cache it
                    usernameTextView.setText(username);
                } else {
                    usernameTextView.setText("Unknown User");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load username: " + error.getMessage());
                usernameTextView.setText("Unknown User");
            }
        });
    }

    private void showPlacePopup(Place place) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_place_details);

        ImageView detailImage = dialog.findViewById(R.id.detailImage);
        TextView detailTitle = dialog.findViewById(R.id.detailTitle);
        TextView detailDescription = dialog.findViewById(R.id.detailDescription);
        TextView detailTimestamp = dialog.findViewById(R.id.detailTimestamp);
        TextView detailUsername = dialog.findViewById(R.id.detailUsername);
        Button btnClose = dialog.findViewById(R.id.btnClose);

        Glide.with(context)
                .load(place.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(detailImage);

        detailTitle.setText(place.getTitle());
        detailDescription.setText(place.getDescription());

        String formattedTime = android.text.format.DateFormat.format("dd MMM yyyy, HH:mm", place.getTimestamp()).toString();
        detailTimestamp.setText("Uploaded on: " + formattedTime);

        // Load username for detail view
        loadUsername(place.getUserId(), detailUsername);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    private void deletePlace(Place place, int position) {
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(place.getImageUrl());
        imageRef.delete().addOnSuccessListener(aVoid -> {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("places").child(place.getId());
            dbRef.removeValue().addOnSuccessListener(v -> {
                Toast.makeText(context, "Place deleted successfully", Toast.LENGTH_SHORT).show();
                placeList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, placeList.size());
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to delete from database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView placeImage;
        TextView placeTitle, placeDescription, placeTimestamp, placeUsername;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            placeImage = itemView.findViewById(R.id.placeImage);
            placeTitle = itemView.findViewById(R.id.placeTitle);
            placeDescription = itemView.findViewById(R.id.placeDescription);
            placeTimestamp = itemView.findViewById(R.id.placeTimestamp);
            placeUsername = itemView.findViewById(R.id.placeUsername);
        }
    }
}