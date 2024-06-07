package com.activity.pis_azil.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.AnimalsAdapter;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

public class AnimalListFragment extends Fragment {

    private RecyclerView recyclerView;
    private AnimalsAdapter animalsAdapter;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_animal_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        apiService = ApiClient.getClient().create(ApiService.class);
        loadAnimals();

        return view;
    }

    private void loadAnimals() {
        apiService.getAllAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<AnimalModel>> call, @NonNull Response<List<AnimalModel>> response) {
                if (response.isSuccessful()) {
                    List<AnimalModel> animals = response.body();
                    animalsAdapter = new AnimalsAdapter(animals, getContext());
                    recyclerView.setAdapter(animalsAdapter);
                    Log.d("AnimalListFragment", "Animals loaded: " + animals.size());
                } else {
                    Log.e("AnimalListFragment", "Failed to load animals: " + response.code() + ", " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AnimalModel>> call, @NonNull Throwable t) {
                Log.e("AnimalListFragment", "Error loading animals", t);
            }
        });
    }
}
