package com.activity.pis_azil.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvailableAnimalsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AvailableAnimalsAdapter adapter;
    private List<AnimalModel> animalsList, filteredAnimalsList;
    private ApiService apiService;
    private TextView emptyStateTextView;
    private Spinner filterTypeSpinner, filterStatusSpinner;

    public AvailableAnimalsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_available_animals, container, false);

        recyclerView = root.findViewById(R.id.recyclerview_available_animals);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyStateTextView = root.findViewById(R.id.empty_state_text_view);
        filterTypeSpinner = root.findViewById(R.id.filter_type_spinner);
        filterStatusSpinner = root.findViewById(R.id.filter_status_spinner);

        animalsList = new ArrayList<>();
        filteredAnimalsList = new ArrayList<>();
        adapter = new AvailableAnimalsAdapter(getContext(), filteredAnimalsList);
        recyclerView.setAdapter(adapter);

        setupFilterSpinners();
        fetchAvailableAnimals();

        return root;
    }

    private void setupFilterSpinners() {
        ArrayAdapter<CharSequence> typeSpinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.animal_types2, android.R.layout.simple_spinner_item);
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterTypeSpinner.setAdapter(typeSpinnerAdapter);

        ArrayAdapter<CharSequence> statusSpinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.animal_statuses2, android.R.layout.simple_spinner_item);
        statusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterStatusSpinner.setAdapter(statusSpinnerAdapter);

        filterTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        filterStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void fetchAvailableAnimals() {
        apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getAllAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    animalsList.clear();
                    animalsList.addAll(response.body());
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
        String selectedStatus = filterStatusSpinner.getSelectedItem().toString();

        filteredAnimalsList.clear();

        for (AnimalModel animal : animalsList) {
            boolean matchesType = selectedType.equals("Svi") || animal.getTipLjubimca().equalsIgnoreCase(selectedType);
            boolean matchesStatus = selectedStatus.equals("Svi") ||
                    (selectedStatus.equals("Dostupno") && !animal.isZahtjevUdomljavanja()) ||
                            (selectedStatus.equals("Nedostupno") && animal.isZahtjevUdomljavanja());

            if (matchesType && matchesStatus) {
                filteredAnimalsList.add(animal);
            }
        }

        if (filteredAnimalsList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateTextView.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }
}
