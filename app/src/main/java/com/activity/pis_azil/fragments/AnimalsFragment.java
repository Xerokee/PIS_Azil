package com.activity.pis_azil.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.MyAdoptionAdapter;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.network.DataRefreshListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimalsFragment extends Fragment implements DataRefreshListener {

    @Override
    public void refreshData() {
        fetchAdoptedAnimals();
    }

    private static final String TAG = "AnimalsFragment";

    ApiService apiService;
    RecyclerView recyclerView;
    private MyAdoptionAdapter cartAdapter;
    List<MyAdoptionModel> cartModelList;
    private EditText searchAnimalBox;
    private String currentSearchQuery = "";
    private String currentTypeFilter = "Svi";
    private TextView animalsTextView;
    List<MyAdoptionModel> allAnimalsList = new ArrayList<>();

    public AnimalsFragment() {
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_adoptions, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);

        recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        searchAnimalBox = root.findViewById(R.id.search_animal_box);
        ImageButton filterButton = root.findViewById(R.id.filter_button);

        cartModelList = new ArrayList<>();

        cartAdapter = new MyAdoptionAdapter(requireContext(), cartModelList, this); // Pass full list initially
        recyclerView.setAdapter(cartAdapter);

        animalsTextView = root.findViewById(R.id.animals_textview);

        searchAnimalBox.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                currentSearchQuery = charSequence.toString().toLowerCase();
                applyFilters(currentTypeFilter);
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {}
        });

        filterButton.setOnClickListener(v -> showFilterDialog());

        fetchAdoptedAnimals();

        return root;
    }

    private void fetchAdoptedAnimals() {
        Log.d(TAG, "Fetching adopted animals");

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);
        final UserModel currentUser;

        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, UserModel.class);
        } else {
            return;
        }

        apiService.getDnevnikUdomljavanja().enqueue(new Callback<List<UpdateDnevnikModel>>() {
            @Override
            public void onResponse(Call<List<UpdateDnevnikModel>> call, Response<List<UpdateDnevnikModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cartModelList.clear();
                    allAnimalsList.clear();

                    List<UpdateDnevnikModel> allAdoptions = response.body();
                    for (UpdateDnevnikModel animal : allAdoptions) {
                        if (currentUser.isAdmin() || animal.getId_korisnika() == currentUser.getIdKorisnika()) {
                            if (animal.isUdomljen()) {
                                continue;
                            }

                            MyAdoptionModel adoption = new MyAdoptionModel();
                            adoption.setId(animal.getId());
                            adoption.setIdLjubimca(animal.getId_ljubimca());
                            adoption.setImeLjubimca(animal.getIme_ljubimca() != null ? animal.getIme_ljubimca() : "N/A");
                            adoption.setTipLjubimca(animal.getTip_ljubimca() != null ? animal.getTip_ljubimca() : "N/A");
                            adoption.setDatum(animal.getDatum());
                            adoption.setVrijeme(animal.getVrijeme());
                            adoption.setImgUrl(animal.getImgUrl());
                            adoption.setStanjeZivotinje(animal.isStanje_zivotinje());
                            adoption.setIdKorisnika(animal.getId_korisnika());
                            cartModelList.add(adoption);
                            allAnimalsList.add(adoption);
                        }
                    }
                    updateEmptyState();
                    applyFilters(currentTypeFilter);
                } else {
                    Log.e(TAG, "Failed to fetch adopted animals: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<UpdateDnevnikModel>> call, Throwable t) {
                Log.e(TAG, "Error fetching adopted animals: ", t);
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filters2, null);

        android.widget.Spinner typeFilter = dialogView.findViewById(R.id.spinner_type);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button confirmButton = dialogView.findViewById(R.id.confirm_button);
        Button resetButton = dialogView.findViewById(R.id.reset_button);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.animal_types2, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeFilter.setAdapter(typeAdapter);

        builder.setView(dialogView).setTitle("Filtriraj životinje");

        AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        confirmButton.setOnClickListener(v -> {
            String type = typeFilter.getSelectedItem().toString();
            applyFilters(type);
            dialog.dismiss();
        });

        resetButton.setOnClickListener(v -> {
            applyFilters("Svi");
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateEmptyState() {
        if (allAnimalsList.isEmpty()) {
            animalsTextView.setVisibility(View.VISIBLE);
        } else {
            animalsTextView.setVisibility(View.GONE);
        }
    }

    private void applyFilters(String type) {
        currentTypeFilter = type;

        if (cartModelList == null || cartModelList.isEmpty() || cartAdapter == null) {
            return;
        }

        List<MyAdoptionModel> filteredList = new ArrayList<>();

        for (MyAdoptionModel animal : allAnimalsList) {
            boolean matchesType = type.equals("Svi") ||
                    (animal.getTipLjubimca() != null && animal.getTipLjubimca().equalsIgnoreCase(type));

            boolean matchesName = currentSearchQuery.isEmpty() ||
                    (animal.getImeLjubimca() != null && animal.getImeLjubimca().toLowerCase().contains(currentSearchQuery));

            if (matchesType && matchesName) {
                filteredList.add(animal);
            }
        }

        cartAdapter.updateData(filteredList);
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchAdoptedAnimals();
    }
}
