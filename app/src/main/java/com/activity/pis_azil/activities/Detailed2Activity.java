package com.activity.pis_azil.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.ViewPagerAdapter;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Detailed2Activity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    ViewPagerAdapter viewPagerAdapter;
    IsBlockedAnimalModel animalModel = null;
    AnimalModel detailedAnimal;
    ApiService apiService;
    UserModel currentUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed2);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        animalModel = (IsBlockedAnimalModel) getIntent().getSerializableExtra("animal");
        apiService = ApiClient.getClient().create(ApiService.class);

        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);

        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, UserModel.class);
        } else {
            currentUser = null;
        }

        if (animalModel != null) {
            fetchAnimalDetails(animalModel.getIdLjubimca());
        } else {
            Toast.makeText(this, "Greška u dohvaćanju podataka o ljubimcu", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchAnimalDetails(int animalId) {
        apiService.getAnimalById(animalId).enqueue(new Callback<AnimalModel>() {
            @Override
            public void onResponse(Call<AnimalModel> call, Response<AnimalModel> response) {
                if (response.isSuccessful()) {
                    detailedAnimal = response.body();
                    viewPagerAdapter = new ViewPagerAdapter(Detailed2Activity.this, detailedAnimal, currentUser, animalModel);
                    viewPager.setAdapter(viewPagerAdapter);

                    new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                        switch (position) {
                            case 0:
                                tab.setText("Osnovni podaci");
                                break;
                            case 1:
                                tab.setText("Galerija");
                                break;
                        }
                    }).attach();

                } else {
                    Toast.makeText(Detailed2Activity.this, "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AnimalModel> call, Throwable t) {
                Toast.makeText(Detailed2Activity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
