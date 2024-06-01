package com.activity.pis_azil.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.activity.pis_azil.ApiClient;
import com.activity.pis_azil.ApiService;
import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.ViewAllAdapter;
import com.activity.pis_azil.models.ViewAllModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewAllActivity extends AppCompatActivity {

    ApiService apiService;
    RecyclerView recyclerView;
    ViewAllAdapter viewAllAdapter;
    List<ViewAllModel> viewAllModelList;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all);

        apiService = ApiClient.getClient().create(ApiService.class);
        String type = getIntent().getStringExtra("type");

        recyclerView = findViewById(R.id.view_all_rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewAllModelList = new ArrayList<>();
        viewAllAdapter = new ViewAllAdapter(this, viewAllModelList);
        recyclerView.setAdapter(viewAllAdapter);

        if (type != null) {
            apiService.getAnimalsByType(type).enqueue(new Callback<List<ViewAllModel>>() {
                @Override
                public void onResponse(Call<List<ViewAllModel>> call, Response<List<ViewAllModel>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        viewAllModelList.addAll(response.body());
                        viewAllAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<List<ViewAllModel>> call, Throwable t) {
                    // Handle failure
                }
            });
        } else {
            apiService.getAllAnimals().enqueue(new Callback<List<ViewAllModel>>() {
                @Override
                public void onResponse(Call<List<ViewAllModel>> call, Response<List<ViewAllModel>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        viewAllModelList.addAll(response.body());
                        viewAllAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<List<ViewAllModel>> call, Throwable t) {
                    // Handle failure
                }
            });
        }
    }
}
