package com.example.finalproject.adapters;

import android.app.Dialog;
import android.content.Context;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private Context context;
    private ArrayList<Place> placeList;

    public PlaceAdapter(Context context, ArrayList<Place> placeList) {
        this.context = context;
        this.placeList = placeList;
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

        // Format timestamp nicely
        holder.placeTimestamp.setText(android.text.format.DateFormat.format(
                "dd/MM/yyyy hh:mm a", place.getTimestamp()));

        Glide.with(context)
                .load(place.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.placeImage);

        holder.itemView.setOnClickListener(v -> showPlacePopup(place));

        // Long press for delete (existing code)
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

    private void showPlacePopup(Place place) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_place_details);

        ImageView detailImage = dialog.findViewById(R.id.detailImage);
        TextView detailTitle = dialog.findViewById(R.id.detailTitle);
        TextView detailDescription = dialog.findViewById(R.id.detailDescription);
        TextView detailTimestamp = dialog.findViewById(R.id.detailTimestamp);
        Button btnClose = dialog.findViewById(R.id.btnClose);

        Glide.with(context)
                .load(place.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(detailImage);

        detailTitle.setText(place.getTitle());
        detailDescription.setText(place.getDescription());

        String formattedTime = android.text.format.DateFormat.format("dd MMM yyyy, HH:mm", place.getTimestamp()).toString();
        detailTimestamp.setText("Uploaded on: " + formattedTime);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    @Override
    public int getItemCount() {
        return placeList.size();
    }

    private void deletePlace(Place place, int position) {
        // Remove from Firebase Storage
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(place.getImageUrl());
        imageRef.delete().addOnSuccessListener(aVoid -> {
            // Remove from Realtime Database
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("places").child(place.getId());
            dbRef.removeValue().addOnSuccessListener(v -> {
                Toast.makeText(context, "Place deleted successfully", Toast.LENGTH_SHORT).show();
                // Remove from local list and notify adapter
                placeList.remove(place);
                notifyDataSetChanged();
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to delete from database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView placeImage;
        TextView placeTitle, placeDescription, placeTimestamp;
        View detailsLayout;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            placeImage = itemView.findViewById(R.id.placeImage);
            placeTitle = itemView.findViewById(R.id.placeTitle);
            placeDescription = itemView.findViewById(R.id.placeDescription);
            placeTimestamp = itemView.findViewById(R.id.placeTimestamp);
            detailsLayout = itemView.findViewById(R.id.detailsLayout);
        }
    }
}
