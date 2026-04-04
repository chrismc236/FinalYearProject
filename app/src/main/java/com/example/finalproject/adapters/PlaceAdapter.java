package com.example.finalproject.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalproject.AddPlaceActivity;
import com.example.finalproject.R;
import com.example.finalproject.models.Place;
import com.example.finalproject.models.User;
import com.example.finalproject.utils.LikeHelper;
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

    private final Context context;
    private final ArrayList<Place> placeList;
    private final Map<String, String> userNamesCache;
    private final LikeHelper likeHelper;

    public PlaceAdapter(Context context, ArrayList<Place> placeList) {
        this.context        = context;
        this.placeList      = placeList;
        this.userNamesCache = new HashMap<>();
        this.likeHelper     = new LikeHelper();
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

        holder.placeTimestamp.setText(
                android.text.format.DateFormat.format("dd/MM/yyyy hh:mm a", place.getTimestamp()));

        // Optional meta badges
        if (place.getLocationTag() != null && !place.getLocationTag().isEmpty()) {
            holder.placeLocation.setVisibility(View.VISIBLE);
            holder.placeLocation.setText("📍 " + place.getLocationTag());
        } else {
            holder.placeLocation.setVisibility(View.GONE);
        }

        if (place.getCategory() != null && !place.getCategory().isEmpty()) {
            holder.placeCategory.setVisibility(View.VISIBLE);
            holder.placeCategory.setText(place.getCategory());
        } else {
            holder.placeCategory.setVisibility(View.GONE);
        }

        holder.placeUsername.setText(
                place.getUserName() != null ? place.getUserName() : "Unknown User"
        );

        // Likes
        holder.placeLikesCount.setText(String.valueOf(place.getLikesCount()));
        likeHelper.isPlaceLikedByUser(place.getId(), isLiked -> {
            if (isLiked) {
                holder.btnLike.setImageResource(R.drawable.ic_heart_filled);
                holder.btnLike.setColorFilter(ContextCompat.getColor(context, R.color.primary_coral));
            } else {
                holder.btnLike.setImageResource(R.drawable.ic_heart_outline);
                holder.btnLike.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary));
            }
        });

        holder.btnLike.setOnClickListener(v -> {
            likeHelper.toggleLike(place.getId(), new LikeHelper.OnLikeToggledListener() {
                int currentPos = holder.getBindingAdapterPosition();
                @Override public void onLikeAdded() {
                    holder.btnLike.setImageResource(R.drawable.ic_heart_filled);
                    holder.btnLike.setColorFilter(ContextCompat.getColor(context, R.color.primary_coral));
                    int n = place.getLikesCount() + 1;
                    place.setLikesCount(n);
                    holder.placeLikesCount.setText(String.valueOf(n));
                    if (currentPos != RecyclerView.NO_ID) notifyItemChanged(currentPos);
                }
                @Override public void onLikeRemoved() {
                    holder.btnLike.setImageResource(R.drawable.ic_heart_outline);
                    holder.btnLike.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary));
                    int n = Math.max(0, place.getLikesCount() - 1);
                    place.setLikesCount(n);
                    holder.placeLikesCount.setText(String.valueOf(n));
                    if (currentPos != RecyclerView.NO_ID) notifyItemChanged(currentPos);
                }
                @Override public void onError(String error) {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Image
        Glide.with(context).load(place.getImageUrl())
                .placeholder(R.drawable.placeholder).into(holder.placeImage);

        // Tap card → full-screen detail dialog
        holder.itemView.setOnClickListener(v -> showPlacePopup(place));

        // Ask AI button
        holder.btnAskAI.setOnClickListener(v -> {
            Intent intent = AddPlaceActivity.buildAiIntent(context, place);
            context.startActivity(intent);
        });

        // Long-press → delete
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Place")
                    .setMessage("Are you sure you want to delete \"" + place.getTitle() + "\"?")
                    .setPositiveButton("Delete", (dialog, which) ->
                            deletePlace(place, holder.getBindingAdapterPosition()))
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    // ─── Full-screen popup ───────────────────────
    private void showPlacePopup(Place place) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_place_details);

        ImageView detailImage       = dialog.findViewById(R.id.detailImage);
        TextView  detailTitle       = dialog.findViewById(R.id.detailTitle);
        TextView  detailDescription = dialog.findViewById(R.id.detailDescription);
        TextView  detailTimestamp   = dialog.findViewById(R.id.detailTimestamp);
        TextView  detailUsername    = dialog.findViewById(R.id.detailUsername);
        TextView  detailLikesCount  = dialog.findViewById(R.id.detailLikesCount);
        ImageView btnDetailLike     = dialog.findViewById(R.id.btnDetailLike);
        Button    btnClose          = dialog.findViewById(R.id.btnClose);
        Button    btnDialogAskAI    = dialog.findViewById(R.id.btnDialogAskAI);

        Glide.with(context).load(place.getImageUrl())
                .placeholder(R.drawable.placeholder).into(detailImage);

        detailTitle.setText(place.getTitle());
        detailDescription.setText(place.getDescription());
        detailTimestamp.setText("Uploaded on: " +
                android.text.format.DateFormat.format("dd MMM yyyy, HH:mm", place.getTimestamp()));
        detailLikesCount.setText(place.getLikesCount() + " likes");

        likeHelper.isPlaceLikedByUser(place.getId(), isLiked -> {
            if (isLiked) {
                btnDetailLike.setImageResource(R.drawable.ic_heart_filled);
                btnDetailLike.setColorFilter(ContextCompat.getColor(context, R.color.primary_coral));
            } else {
                btnDetailLike.setImageResource(R.drawable.ic_heart_outline);
                btnDetailLike.setColorFilter(ContextCompat.getColor(context, android.R.color.white));
            }
        });

        btnDetailLike.setOnClickListener(v ->
                likeHelper.toggleLike(place.getId(), new LikeHelper.OnLikeToggledListener() {
                    @Override public void onLikeAdded() {
                        btnDetailLike.setImageResource(R.drawable.ic_heart_filled);
                        btnDetailLike.setColorFilter(ContextCompat.getColor(context, R.color.primary_coral));
                        int n = place.getLikesCount() + 1; place.setLikesCount(n);
                        detailLikesCount.setText(n + " likes");
                    }
                    @Override public void onLikeRemoved() {
                        btnDetailLike.setImageResource(R.drawable.ic_heart_outline);
                        btnDetailLike.setColorFilter(ContextCompat.getColor(context, android.R.color.white));
                        int n = Math.max(0, place.getLikesCount() - 1); place.setLikesCount(n);
                        detailLikesCount.setText(n + " likes");
                    }
                    @Override public void onError(String error) {
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                    }
                })
        );

        // Ask AI from the popup dialog
        if (btnDialogAskAI != null) {
            btnDialogAskAI.setOnClickListener(v -> {
                dialog.dismiss();
                context.startActivity(AddPlaceActivity.buildAiIntent(context, place));
            });
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ─── Delete ──────────────────────────────────
    private void deletePlace(Place place, int position) {
        StorageReference imageRef =
                FirebaseStorage.getInstance().getReferenceFromUrl(place.getImageUrl());
        imageRef.delete().addOnSuccessListener(aVoid -> {
            FirebaseDatabase.getInstance().getReference("places").child(place.getId())
                    .removeValue()
                    .addOnSuccessListener(v -> {
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                        placeList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, placeList.size());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "DB delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e ->
                Toast.makeText(context, "Image delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override public int getItemCount() { return placeList.size(); }

    // ─── ViewHolder ──────────────────────────────
    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView placeImage, btnLike;
        TextView  placeTitle, placeDescription, placeTimestamp,
                placeUsername, placeLikesCount, placeLocation, placeCategory;
        Button    btnAskAI;

        PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            placeImage        = itemView.findViewById(R.id.placeImage);
            placeTitle        = itemView.findViewById(R.id.placeTitle);
            placeDescription  = itemView.findViewById(R.id.placeDescription);
            placeTimestamp    = itemView.findViewById(R.id.placeTimestamp);
            placeUsername     = itemView.findViewById(R.id.placeUsername);
            placeLikesCount   = itemView.findViewById(R.id.placeLikesCount);
            btnLike           = itemView.findViewById(R.id.btnLike);
            btnAskAI          = itemView.findViewById(R.id.btnAskAI);
            placeLocation     = itemView.findViewById(R.id.placeLocation);
            placeCategory     = itemView.findViewById(R.id.placeCategory);
        }
    }
}