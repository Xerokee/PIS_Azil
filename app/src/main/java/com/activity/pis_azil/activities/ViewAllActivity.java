package com.activity.pis_azil.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.AnimalsAdapter;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewAllActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AnimalsAdapter animalsAdapter;
    private List<AnimalModel> animalModelList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all);

        recyclerView = findViewById(R.id.view_all_rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        animalsAdapter = new AnimalsAdapter(animalModelList, this);
        recyclerView.setAdapter(animalsAdapter);

        apiService = ApiClient.getClient().create(ApiService.class);

        String type = getIntent().getStringExtra("type");
        if (type != null) {
            apiService.getAnimalsByType(type).enqueue(new Callback<List<AnimalModel>>() {
                @Override
                public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        animalModelList.addAll(response.body());
                        animalsAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ViewAllActivity.this, "No animals found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                    Toast.makeText(ViewAllActivity.this, "Failed to load animals", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            loadAllAnimals();
        }
    }

    private void loadAllAnimals() {
        apiService.getAllAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    animalModelList.addAll(response.body());
                    animalsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ViewAllActivity.this, "No animals found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                Toast.makeText(ViewAllActivity.this, "Failed to load animals", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
