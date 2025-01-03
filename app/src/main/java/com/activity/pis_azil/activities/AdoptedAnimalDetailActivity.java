package com.activity.pis_azil.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.ViewPagerAdapter2;
import com.activity.pis_azil.adapters.ViewPagerAdapter3;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdoptedAnimalDetailActivity extends AppCompatActivity {

    String sAnimalId;
    int animalId;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    ViewPagerAdapter3 viewPagerAdapter;
    AnimalModel detailedAnimal;
    ApiService apiService;
    String udomitelj;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adopted_animal_detail);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        final Bundle oExtras= getIntent().getExtras();
        sAnimalId= oExtras.getString("id");
        udomitelj = oExtras.getString("udomitelj");
        animalId= Integer.parseInt(sAnimalId);
        apiService = ApiClient.getClient().create(ApiService.class);

        fetchAnimalDetails(animalId);
    }


    private void fetchAnimalDetails(int animalId) {
        apiService.getAnimalById(animalId).enqueue(new Callback<AnimalModel>() {
            @Override
            public void onResponse(Call<AnimalModel> call, Response<AnimalModel> response) {
                if (response.isSuccessful()) {
                    detailedAnimal = response.body();
                    if (detailedAnimal == null) {
                        Toast.makeText(AdoptedAnimalDetailActivity.this, "Podaci o ljubimcu nisu dostupni.", Toast.LENGTH_SHORT).show();
                    } else {
                        viewPagerAdapter = new ViewPagerAdapter3(AdoptedAnimalDetailActivity.this, detailedAnimal, animalId, udomitelj);
                        viewPager.setAdapter(viewPagerAdapter);
                        // Postavke za tabove
                        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                            switch (position) {
                                case 0:
                                    tab.setText("Osnovni podaci");
                                    break;
                                case 1:
                                    tab.setText("Galerija");
                                    break;
                                case 2:
                                    tab.setText("Dnevnik aktivnosti");
                                    break;
                            }
                        }).attach();
                    }
                } else {
                    Toast.makeText(AdoptedAnimalDetailActivity.this, "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AnimalModel> call, Throwable t) {
                Toast.makeText(AdoptedAnimalDetailActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}