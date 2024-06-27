package com.activity.pis_azil.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log; // Dodano za logiranje
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailedActivity extends AppCompatActivity {

    private static final String TAG = "DetailedActivity"; // Dodano za logiranje

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
                    .placeholder(R.drawable.paw)
                    .error(R.drawable.milk2)
                    .into(detailedImg);
            name.setText(animalModel.getImeLjubimca());
            description.setText(animalModel.getOpisLjubimca());
        } else {
            detailedImg.setImageResource(R.drawable.fruits);
        }

        addToCart = findViewById(R.id.add_to_cart);
        addToCart.setOnClickListener(v -> addedToCart());
    }

    private void addedToCart() {
        if (animalModel == null) {
            Log.e(TAG, "AnimalModel is null");
            return;
        }

        MyAdoptionModel adoptionModel = new MyAdoptionModel();
        adoptionModel.setIdLjubimca(animalModel.getIdLjubimca());
        adoptionModel.setImeLjubimca(animalModel.getImeLjubimca());
        adoptionModel.setTipLjubimca(animalModel.getTipLjubimca());
        adoptionModel.setOpisLjubimca(animalModel.getOpisLjubimca());
        adoptionModel.setDatum(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime()));
        adoptionModel.setImgUrl(animalModel.getImgUrl());
        adoptionModel.setIdKorisnika(1); // Pretpostavimo da je korisnik s ID 1 admin
        adoptionModel.setUdomljen(true);
        adoptionModel.setStanjeZivotinje(true);

        Log.d(TAG, "Sending adoption model to API: " + adoptionModel);

        apiService.addAdoption(adoptionModel).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Adoption added successfully");
                    Toast.makeText(DetailedActivity.this, "Dodano u listu udomljavanja!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Failed to add adoption: " + response.message());
                    Toast.makeText(DetailedActivity.this, "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error adding adoption: ", t);
                Toast.makeText(DetailedActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
