package com.activity.pis_azil.fragments;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.MyAnimalsAdapter;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
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

public class MyAnimalsFragment extends Fragment implements MyAnimalsAdapter.OnFetchAnimalsCallback {

    private RecyclerView recyclerView;
    private MyAnimalsAdapter adapter;
    private List<UpdateDnevnikModel> animalsList, filteredAnimalsList;
    private List<IsBlockedAnimalModel> animalsList2;
    private ApiService apiService;
    private TextView emptyStateTextView;
    private EditText searchAnimalBox;
    private Button filterButton;

    public MyAnimalsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_animals, container, false);

        recyclerView = root.findViewById(R.id.recyclerview_my_animals);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyStateTextView = root.findViewById(R.id.empty_state_text_view);
        searchAnimalBox = root.findViewById(R.id.search_adopter_box);
        ImageButton filterButton = root.findViewById(R.id.filter_button);

        animalsList = new ArrayList<>();
        filteredAnimalsList = new ArrayList<>();
        animalsList2 = new ArrayList<>();
        adapter = new MyAnimalsAdapter(getContext(), filteredAnimalsList, animalsList2, activityResultLauncher, this);
        recyclerView.setAdapter(adapter);

        // Search functionality
        searchAnimalBox.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString().toLowerCase();
                filterAnimalsByName(query);
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {}
        });

        filterButton.setOnClickListener(v -> showFilterDialog());

        fetchMyAnimals();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchMyAnimals();
    }

    public void fetchMyAnimals() {
        Log.i("pozvano","pozvano");
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
                    applyFilters();
                }
            }

            @Override
            public void onFailure(Call<List<UpdateDnevnikModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAnimalsByName(String query) {
        filteredAnimalsList.clear();

        if (query.isEmpty()) {
            filteredAnimalsList.addAll(animalsList);
        } else {
            for (UpdateDnevnikModel animal : animalsList) {
                if (animal.getIme_ljubimca() != null && animal.getIme_ljubimca().toLowerCase().contains(query.toLowerCase())) {
                    filteredAnimalsList.add(animal);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filters3, null);

        android.widget.Spinner typeFilter = dialogView.findViewById(R.id.spinner_type);
        android.widget.Spinner statusFilter = dialogView.findViewById(R.id.spinner_status);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button confirmButton = dialogView.findViewById(R.id.confirm_button);
        Button resetButton = dialogView.findViewById(R.id.reset_button);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.animal_types2, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeFilter.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.animal_statuses, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusFilter.setAdapter(statusAdapter);

        builder.setView(dialogView).setTitle("Filtriraj životinje");

        AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        confirmButton.setOnClickListener(v -> {
            String type = typeFilter.getSelectedItem().toString();
            String status = statusFilter.getSelectedItem().toString();
            applyFilters(type, status);
            dialog.dismiss();
        });

        resetButton.setOnClickListener(v -> {
            applyFilters("Svi", "Svi");
            dialog.dismiss();
        });

        dialog.show();
    }

    private void applyFilters() {
        String selectedType = "Svi";
        String selectedStatus = "Svi";

        applyFilters(selectedType, selectedStatus);
    }

    private void applyFilters(String type, String status) {
        filteredAnimalsList.clear();

        for (UpdateDnevnikModel animal : animalsList) {
            boolean matchesType = type.equals("Svi") || animal.getTip_ljubimca().equalsIgnoreCase(type);
            boolean matchesStatus = status.equals("Svi") || (status.equals("Udomljen") && animal.isUdomljen() || (status.equals("Rezerviran") && animal.isStatus_udomljavanja()));

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

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
            }
    );
}
