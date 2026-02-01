package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.finalproject.R;


public class PlaceDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        ImageView image = findViewById(R.id.detailImage);
        TextView title = findViewById(R.id.detailTitle);
        TextView desc = findViewById(R.id.detailDescription);
        TextView timestamp = findViewById(R.id.detailTimestamp);

        title.setText(getIntent().getStringExtra("title"));
        desc.setText(getIntent().getStringExtra("description"));

        long ts = getIntent().getLongExtra("timestamp", 0);
        timestamp.setText(android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", ts));

        Glide.with(this)
                .load(getIntent().getStringExtra("imageUrl"))
                .placeholder(R.drawable.placeholder)
                .into(image);

    }
}