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
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.MyAnimalsAdapter;
import com.activity.pis_azil.models.RejectAdoptionModel;
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
    private List<UpdateDnevnikModel> animalsList;
    private ApiService apiService;
    private TextView emptyStateTextView;

    public MyAnimalsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_animals, container, false);

        recyclerView = root.findViewById(R.id.recyclerview_my_animals);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyStateTextView = root.findViewById(R.id.empty_state_text_view);

        animalsList = new ArrayList<>();
        adapter = new MyAnimalsAdapter(getContext(), animalsList);
        recyclerView.setAdapter(adapter);

        fetchMyAnimals();

        return root;
    }

    private void fetchMyAnimals() {
        // Get current user
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);
        UserModel currentUser = new Gson().fromJson(userJson, UserModel.class);

        if (currentUser == null) {
            return;
        }

        // API call to fetch animals related to the current user
        apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getDnevnikUdomljavanja().enqueue(new Callback<List<UpdateDnevnikModel>>() {
            @Override
            public void onResponse(Call<List<UpdateDnevnikModel>> call, Response<List<UpdateDnevnikModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UpdateDnevnikModel> allAnimals = response.body();
                    animalsList.clear();

                    // Filter animals for the current user
                    for (UpdateDnevnikModel animal : allAnimals) {
                        if (animal.getId_korisnika() == currentUser.getIdKorisnika()) {
                            if (!animal.isUdomljen() && !animal.isStatus_udomljavanja()) {
                                // Notify user that the request was rejected
                                Toast.makeText(getContext(), "Zahtjev za udomljavanje " + animal.getIme_ljubimca() + " je odbijen.", Toast.LENGTH_LONG).show();
                            } else {
                                // Only show animals with requests still pending or approved
                                animalsList.add(animal);
                            }
                        }
                    }

                    // API call to fetch rejected animals for current user
                    apiService.getOdbijeneZivotinje().enqueue(new Callback<List<RejectAdoptionModelRead>>() {
                        @Override
                        public void onResponse(Call<List<RejectAdoptionModelRead>> call, Response<List<RejectAdoptionModelRead>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<RejectAdoptionModelRead> rejectedAnimals = response.body();
                                for (RejectAdoptionModelRead rejectedAnimal : rejectedAnimals) {
                                    if (rejectedAnimal.getId_korisnika().equals(currentUser.getIdKorisnika())) {
                                        // Notify user that the request was rejected
                                        Toast.makeText(getContext(), "Admin je odbio zahtjev za " + rejectedAnimal.getIme_ljubimca(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            } else {
                                Toast.makeText(getContext(), "Greška pri dohvaćanju odbijenih zahtjeva", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<RejectAdoptionModelRead>> call, Throwable t) {
                            Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Display empty state if no animals
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
            public void onFailure(Call<List<UpdateDnevnikModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
