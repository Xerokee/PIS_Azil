package com.activity.pis_azil.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.AvailableAnimalsAdapter;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvailableAnimalsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AvailableAnimalsAdapter adapter;
    private List<AnimalModel> animalsList;
    private ApiService apiService;
    private TextView emptyStateTextView;

    public AvailableAnimalsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_available_animals, container, false);

        recyclerView = root.findViewById(R.id.recyclerview_available_animals);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyStateTextView = root.findViewById(R.id.empty_state_text_view);

        animalsList = new ArrayList<>();
        adapter = new AvailableAnimalsAdapter(getContext(), animalsList);
        recyclerView.setAdapter(adapter);

        fetchAvailableAnimals();

        return root;
    }

    private void fetchAvailableAnimals() {
        apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getAllAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AnimalModel> allAnimals = response.body();
                    animalsList.clear();

                    // Filter to include both adopted and available animals
                    for (AnimalModel animal : allAnimals) {
                        animalsList.add(animal);
                    }

                    // Show or hide the empty state
                    if (animalsList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyStateTextView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyStateTextView.setVisibility(View.GONE);
                    }

                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Greška pri dohvaćanju podataka", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
