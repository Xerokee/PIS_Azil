package com.activity.pis_azil.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.R;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailedActivity extends AppCompatActivity {

    ImageView detailedImg;
    TextView name, description;
    Button addToCart;
    Toolbar toolbar;
    ApiService apiService;
    AnimalModel animalModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        apiService = ApiClient.getClient().create(ApiService.class);

        animalModel = (AnimalModel) getIntent().getSerializableExtra("animal");

        detailedImg = findViewById(R.id.detailed_img);
        name = findViewById(R.id.detailed_name);
        description = findViewById(R.id.detailed_dec);

        if (animalModel != null) {
            Glide.with(getApplicationContext())
                    .load(animalModel.getImgUrl())
                    .placeholder(R.drawable.paw) // Placeholder image
                    .error(R.drawable.milk2) // Error image
                    .into(detailedImg);
            name.setText(animalModel.getImeLjubimca());
            description.setText(animalModel.getOpisLjubimca());
        } else {
            // Set placeholder image if animalModel is null
            detailedImg.setImageResource(R.drawable.fruits);
        }

        addToCart = findViewById(R.id.add_to_cart);
        addToCart.setOnClickListener(v -> addedToCart());
    }

    private void addedToCart() {
        String saveCurrentDate, saveCurrentTime;
        Calendar calForDate = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd. MM. yyyy", new Locale("hr", "HR"));
        saveCurrentDate = currentDate.format(calForDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss", new Locale("hr", "HR"));
        saveCurrentTime = currentTime.format(calForDate.getTime()) + " sati";

        MyAdoptionModel cartModel = new MyAdoptionModel();
        cartModel.setAnimalId(UUID.randomUUID().toString());
        cartModel.setAnimalName(animalModel.getImeLjubimca());
        cartModel.setAnimalType(animalModel.getTipLjubimca());
        cartModel.setCurrentDate(saveCurrentDate);
        cartModel.setCurrentTime(saveCurrentTime);
        cartModel.setImg_url(animalModel.getImgUrl());

        apiService.addAdoption(cartModel).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetailedActivity.this, "Dodano u listu udomljavanja!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(DetailedActivity.this, "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DetailedActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
