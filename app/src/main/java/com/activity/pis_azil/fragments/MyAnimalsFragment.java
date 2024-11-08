package com.activity.pis_azil.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.MyAnimalsAdapter;
import com.activity.pis_azil.models.RejectAdoptionModelRead;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAnimalsFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyAnimalsAdapter adapter;
    private List<UpdateDnevnikModel> animalsList, filteredAnimalsList;
    private ApiService apiService;
    private TextView emptyStateTextView;
    private Spinner filterTypeSpinner, filterStatusSpinner;

    public MyAnimalsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_animals, container, false);

        recyclerView = root.findViewById(R.id.recyclerview_my_animals);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyStateTextView = root.findViewById(R.id.empty_state_text_view);
        filterTypeSpinner = root.findViewById(R.id.filter_type_spinner);
        filterStatusSpinner = root.findViewById(R.id.filter_status_spinner);

        animalsList = new ArrayList<>();
        filteredAnimalsList = new ArrayList<>();
        adapter = new MyAnimalsAdapter(getContext(), filteredAnimalsList, activityResultLauncher);
        recyclerView.setAdapter(adapter);

        setupFilterSpinners();
        fetchRejectedAnimals();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchMyAnimals();
    }

    private void setupFilterSpinners() {
        // Setup the adapter for the animal type filter spinner
        ArrayAdapter<CharSequence> typeSpinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.animal_types2, android.R.layout.simple_spinner_item);
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterTypeSpinner.setAdapter(typeSpinnerAdapter);

        // Setup the adapter for the status filter spinner
        ArrayAdapter<CharSequence> statusSpinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.animal_statuses, android.R.layout.simple_spinner_item);
        statusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterStatusSpinner.setAdapter(statusSpinnerAdapter);

        // Add listeners for the spinners
        filterTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        filterStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchMyAnimals() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);
        UserModel currentUser = new Gson().fromJson(userJson, UserModel.class);

        if (currentUser == null) {
            return;
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getDnevnikUdomljavanja().enqueue(new Callback<List<UpdateDnevnikModel>>() {
            @Override
            public void onResponse(Call<List<UpdateDnevnikModel>> call, Response<List<UpdateDnevnikModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UpdateDnevnikModel> allAnimals = response.body();
                    animalsList.clear();

                    for (UpdateDnevnikModel animal : allAnimals) {
                        if (animal.getId_korisnika() == currentUser.getIdKorisnika()) {
                            animalsList.add(animal);
                        }
                    }
                    applyFilter();
                }
            }

            @Override
            public void onFailure(Call<List<UpdateDnevnikModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRejectedAnimals() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);
        UserModel currentUser = new Gson().fromJson(userJson, UserModel.class);

        if (currentUser == null) {
            return;
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getOdbijeneZivotinje().enqueue(new Callback<List<RejectAdoptionModelRead>>() {
            @Override
            public void onResponse(Call<List<RejectAdoptionModelRead>> call, Response<List<RejectAdoptionModelRead>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RejectAdoptionModelRead> rejectedAnimals = response.body();

                    for (RejectAdoptionModelRead rejectedAnimal : rejectedAnimals) {
                        if (rejectedAnimal.getId_korisnika().equals(currentUser.getIdKorisnika())) {
                            Toast.makeText(getContext(), "Admin je odbio zahtjev za " + rejectedAnimal.getIme_ljubimca(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RejectAdoptionModelRead>> call, Throwable t) {
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter() {
        String selectedType = filterTypeSpinner.getSelectedItem().toString();
        String selectedStatus = filterStatusSpinner.getSelectedItem().toString();

        filteredAnimalsList.clear();

        for (UpdateDnevnikModel animal : animalsList) {
            boolean matchesType = selectedType.equals("Svi") || animal.getTip_ljubimca().equalsIgnoreCase(selectedType);
            boolean matchesStatus = selectedStatus.equals("Svi") ||
                    (selectedStatus.equals("Udomljeno") && animal.isUdomljen()) ||
                    (selectedStatus.equals("Zahtjev u tijeku") && animal.isStatus_udomljavanja());

            if (matchesType && matchesStatus) {
                filteredAnimalsList.add(animal);
            }
        }

        // Update UI for empty state
        if (filteredAnimalsList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateTextView.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

            }
    );
}