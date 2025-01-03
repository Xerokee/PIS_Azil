package com.activity.pis_azil.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.ViewPagerAdapter2;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.UpdateDnevnikModel; // Promjena: Koristimo UpdateDnevnikModel
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimalDetail2Activity extends AppCompatActivity {

    String sAnimalId;
    int animalId;
    private static final String TAG = "AnimalDetail";
    TabLayout tabLayout;
    ViewPager2 viewPager;
    ViewPagerAdapter2 viewPagerAdapter;
    UpdateDnevnikModel animalModel = null;
    AnimalModel detailedAnimal;
    ApiService apiService;
    UserModel currentUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_detail2);

        // Inicijalizacija UI komponenti
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Preuzimamo objekt UpdateDnevnikModel
        //animalModel = (UpdateDnevnikModel) getIntent().getSerializableExtra("animal"); // Promjena

        final Bundle oExtras= getIntent().getExtras();
        sAnimalId= oExtras.getString("id");
        animalId= Integer.parseInt(sAnimalId);
        apiService = ApiClient.getClient().create(ApiService.class);

        // Dohvaćanje trenutnog korisnika iz SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);

        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, UserModel.class);
            Log.d(TAG, "Current User: " + new Gson().toJson(currentUser)); // Logiranje korisničkih podataka
        } else {
            currentUser = null;
            Log.e(TAG, "Current User is null!");
        }

        fetchAnimalDetails(animalId);
    }

    private void fetchAnimalDetails(int animalId) {
        apiService.getAnimalById(animalId).enqueue(new Callback<AnimalModel>() {
            @Override
            public void onResponse(Call<AnimalModel> call, Response<AnimalModel> response) {
                if (response.isSuccessful()) {
                    detailedAnimal = response.body();
                    if (detailedAnimal == null) {
                        Log.e(TAG, "Detailed animal object is still null after response.");
                        Toast.makeText(AnimalDetail2Activity.this, "Podaci o ljubimcu nisu dostupni.", Toast.LENGTH_SHORT).show();
                    } else {
                        viewPagerAdapter = new ViewPagerAdapter2(AnimalDetail2Activity.this, detailedAnimal, currentUser, animalId);
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
                    Toast.makeText(AnimalDetail2Activity.this, "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AnimalModel> call, Throwable t) {
                Log.e(TAG, "Failure: " + t.getMessage(), t);
                Toast.makeText(AnimalDetail2Activity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
