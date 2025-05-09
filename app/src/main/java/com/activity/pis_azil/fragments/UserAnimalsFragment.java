package com.activity.pis_azil.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.UserAnimalsAdapter;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserAnimalsFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAnimalsAdapter adapter;
    private List<AnimalModel> availableAnimals, filteredAnimals;
    private ApiService apiService;
    private TextView emptyStateTextView;
    private Spinner filterTypeSpinner, filterAgeSpinner, filterColorSpinner;

    public UserAnimalsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user_animals, container, false);

        recyclerView = root.findViewById(R.id.recyclerview_user_available_animals);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyStateTextView = root.findViewById(R.id.empty_state_text_view);
        filterTypeSpinner = root.findViewById(R.id.filter_type_spinner);
        filterAgeSpinner = root.findViewById(R.id.filter_age_spinner);
        filterColorSpinner = root.findViewById(R.id.filter_color_spinner);

        availableAnimals = new ArrayList<>();
        filteredAnimals = new ArrayList<>();
        adapter = new UserAnimalsAdapter(getContext(), filteredAnimals);
        recyclerView.setAdapter(adapter);

        setupFilterSpinners();
        fetchAvailableAnimalsForUser();

        return root;
    }

    private void setupFilterSpinners() {
        ArrayAdapter<CharSequence> typeSpinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.animal_types, android.R.layout.simple_spinner_item);
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterTypeSpinner.setAdapter(typeSpinnerAdapter);

        ArrayAdapter<CharSequence> ageSpinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.animal_ages, android.R.layout.simple_spinner_item);
        ageSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterAgeSpinner.setAdapter(ageSpinnerAdapter);

        ArrayAdapter<CharSequence> colorSpinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.animal_colors, android.R.layout.simple_spinner_item);
        colorSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterColorSpinner.setAdapter(colorSpinnerAdapter);

        filterTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        filterAgeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        filterColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchAvailableAnimalsForUser() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);
        UserModel currentUser = new Gson().fromJson(userJson, UserModel.class);

        if (currentUser == null) {
            return;
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAllAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    availableAnimals.clear();
                    availableAnimals.addAll(response.body());
                    applyFilter();
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

    private void applyFilter() {
        String selectedType = filterTypeSpinner.getSelectedItem().toString();
        String selectedAge = filterAgeSpinner.getSelectedItem().toString();
        String selectedColor = filterColorSpinner.getSelectedItem().toString();

        filteredAnimals.clear();

        for (AnimalModel animal : availableAnimals) {
            boolean matchesType = selectedType.equals("Sve") || animal.getTipLjubimca().equalsIgnoreCase(selectedType);
            boolean matchesAge = selectedAge.equals("Sve") || animal.getDobCategory().equalsIgnoreCase(selectedAge);
            boolean matchesColor = selectedColor.equals("Sve") || animal.getBoja().equalsIgnoreCase(selectedColor);

            if (matchesType && matchesAge && matchesColor) {
                filteredAnimals.add(animal);
            }
        }

        if (filteredAnimals.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateTextView.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }
}
