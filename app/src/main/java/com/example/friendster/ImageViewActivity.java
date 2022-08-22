package com.example.friendster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageViewActivity extends AppCompatActivity {

    private ImageView ivImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        String imageUri = intent.getStringExtra("image_uri");

        ivImage = (ImageView) findViewById(R.id.xivFeedImage);
        Glide.with(this)
                .load(imageUri)
                .into(ivImage);
    }

}