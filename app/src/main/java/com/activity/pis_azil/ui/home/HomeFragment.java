package com.activity.pis_azil.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private AnimalsAdapter animalsAdapter;
    private List<AnimalModel> animalModelList = new ArrayList<>();
    private ApiService apiService;
    private ProgressBar progressBar;
    private EditText searchBox;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = root.findViewById(R.id.recycler_view);
        progressBar = root.findViewById(R.id.progressbar);
        searchBox = root.findViewById(R.id.search_box);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        animalsAdapter = new AnimalsAdapter(animalModelList, getContext());
        recyclerView.setAdapter(animalsAdapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        loadAllAnimals();

        return root;
    }

    private void loadAllAnimals() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getAllAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    animalModelList.clear(); // Ovo će osigurati da se lista osvježi
                    List<AnimalModel> animals = response.body();
                    for (AnimalModel animal : animals) {
                        // Ensure the URL is properly formatted
                        if (!animal.getImgUrl().startsWith("http")) {
                            animal.setImgUrl("http://192.168.75.1:8000" + animal.getImgUrl().substring(animal.getImgUrl().lastIndexOf('/')));
                        }
                    }
                    animalModelList.addAll(animals);
                    animalsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "No animals found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load animals", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchAnimals(String keyword) {
        progressBar.setVisibility(View.VISIBLE);
        apiService.searchAnimals(keyword).enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    animalModelList.clear();
                    animalModelList.addAll(response.body());
                    animalsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "No animals found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to search animals", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
