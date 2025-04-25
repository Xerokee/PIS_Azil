package com.activity.pis_azil.activities;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.activity.pis_azil.R;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

public class ImageDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        // Inicijalizacija SubsamplingScaleImageView
        SubsamplingScaleImageView imageView = findViewById(R.id.imageView);

        // Dobavljanje URL-a slike iz Intenta
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // Učitavanje slike pomoću Glide-a (asBitmap za preuzimanje slike kao Bitmap)
        Glide.with(this)
                .asBitmap()  // Preuzimanje slike kao Bitmap
                .load(imageUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        // Postavljanje slike u SubsamplingScaleImageView koristeći ImageSource
                        imageView.setImage(ImageSource.bitmap(resource));
                    }
                });
    }
}
