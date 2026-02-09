package com.example.finalproject.utils;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

/**
 * Helper class for managing likes on places
 */
public class LikeHelper {

    private static final String LIKES_PATH = "likes";
    private static final String PLACES_PATH = "places";

    private DatabaseReference likesRef;
    private DatabaseReference placesRef;
    private FirebaseAuth firebaseAuth;

    public LikeHelper() {
        this.likesRef = FirebaseDatabase.getInstance().getReference(LIKES_PATH);
        this.placesRef = FirebaseDatabase.getInstance().getReference(PLACES_PATH);
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Check if current user has liked a place
     */
    public void isPlaceLikedByUser(String placeId, OnLikeCheckedListener listener) {
        if (firebaseAuth.getCurrentUser() == null) {
            listener.onResult(false);
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();

        likesRef.child(placeId).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listener.onResult(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onResult(false);
            }
        });
    }

    /**
     * Toggle like status for a place
     */
    public void toggleLike(String placeId, OnLikeToggledListener listener) {
        if (firebaseAuth.getCurrentUser() == null) {
            listener.onError("Please log in to like posts");
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference userLikeRef = likesRef.child(placeId).child(userId);

        userLikeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Unlike
                    unlikePlace(placeId, userId, listener);
                } else {
                    // Like
                    likePlace(placeId, userId, listener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    /**
     * Like a place
     */
    private void likePlace(String placeId, String userId, OnLikeToggledListener listener) {
        // Add user to likes list
        likesRef.child(placeId).child(userId).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    // Increment like count
                    incrementLikeCount(placeId);
                    listener.onLikeAdded();
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    /**
     * Unlike a place
     */
    private void unlikePlace(String placeId, String userId, OnLikeToggledListener listener) {
        // Remove user from likes list
        likesRef.child(placeId).child(userId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Decrement like count
                    decrementLikeCount(placeId);
                    listener.onLikeRemoved();
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    /**
     * Increment like count using transaction (atomic operation)
     */
    private void incrementLikeCount(String placeId) {
        DatabaseReference likeCountRef = placesRef.child(placeId).child("likesCount");

        likeCountRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentCount = currentData.getValue(Integer.class);
                if (currentCount == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue(currentCount + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                // Transaction complete
            }
        });
    }

    /**
     * Decrement like count using transaction (atomic operation)
     */
    private void decrementLikeCount(String placeId) {
        DatabaseReference likeCountRef = placesRef.child(placeId).child("likesCount");

        likeCountRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentCount = currentData.getValue(Integer.class);
                if (currentCount == null || currentCount <= 0) {
                    currentData.setValue(0);
                } else {
                    currentData.setValue(currentCount - 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                // Transaction complete
            }
        });
    }

    /**
     * Get like count for a place
     */
    public void getLikeCount(String placeId, OnLikeCountListener listener) {
        placesRef.child(placeId).child("likesCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer count = snapshot.getValue(Integer.class);
                listener.onLikeCount(count != null ? count : 0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onLikeCount(0);
            }
        });
    }

    // Callback interfaces
    public interface OnLikeCheckedListener {
        void onResult(boolean isLiked);
    }

    public interface OnLikeToggledListener {
        void onLikeAdded();
        void onLikeRemoved();
        void onError(String error);
    }

    public interface OnLikeCountListener {
        void onLikeCount(int count);
    }
}