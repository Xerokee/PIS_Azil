package com.activity.pis_azil.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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

    private static final String TAG = "HomeFragment";

    private RecyclerView recyclerView;
    private AnimalsAdapter animalsAdapter;
    private List<AnimalModel> animalModelList = new ArrayList<>();
    private ApiService apiService;
    private ProgressBar progressBar;
    private EditText searchBox;

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

        // Add listener for search box
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(TAG, "onEditorAction called with actionId: " + actionId + " and event: " + event);
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String keyword = searchBox.getText().toString().trim();
                    Log.d(TAG, "Search initiated with keyword: " + keyword);
                    searchAnimalsByType(keyword);
                    return true;
                }
                return false;
            }
        });

        return root;
    }

    private void loadAllAnimals() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Fetching all animals...");
        apiService.getAllAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    animalModelList.clear(); // Ovo će osigurati da se lista osvježi
                    List<AnimalModel> animals = response.body();
                    Log.d(TAG, "Received " + animals.size() + " animals from the server.");
                    for (AnimalModel animal : animals) {
                        Log.d(TAG, "Animal: " + animal.getImeLjubimca() + ", Type: " + animal.getTipLjubimca() + ", Image URL: " + animal.getImgUrl());
                    }
                    animalModelList.addAll(animals);
                    animalsAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error fetching animals. Response code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(getContext(), "Nema pronađenih životinja", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Failed to fetch animals", t);
                Toast.makeText(getContext(), "Greška u učitavanju životinja", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchAnimalsByType(String type) {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Searching animals by type: " + type);
        apiService.getAnimalsByType(type).enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    animalModelList.clear();
                    List<AnimalModel> animals = response.body();
                    Log.d(TAG, "Found " + animals.size() + " animals of type: " + type);
                    for (AnimalModel animal : animals) {
                        Log.d(TAG, "Animal: " + animal.getImeLjubimca() + ", Type: " + animal.getTipLjubimca() + ", Image URL: " + animal.getImgUrl());
                    }
                    animalModelList.addAll(animals);
                    animalsAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error searching animals by type. Response code: " + response.code() + ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Failed to search animals by type", t);
                Toast.makeText(getContext(), "Greška u pretrazi životinja", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
