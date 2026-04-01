package com.example.finalproject.fragments;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class PlaceClusterItem implements ClusterItem {

    private LatLng position;
    private String title;
    private String snippet;

    public PlaceClusterItem(double lat, double lng, String title, String snippet) {
        position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
    }

    @Override public LatLng getPosition() { return position; }
    @Override public String getTitle() { return title; }
    @Override public String getSnippet() { return snippet; }

    @Nullable
    @Override
    public Float getZIndex() {
        return 0f;
    }
}
