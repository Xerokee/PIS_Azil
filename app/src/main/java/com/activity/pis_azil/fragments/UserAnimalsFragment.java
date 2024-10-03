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
    private List<AnimalModel> availableAnimals;
    private ApiService apiService;
    private TextView emptyStateTextView;

    public UserAnimalsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user_animals, container, false);

        recyclerView = root.findViewById(R.id.recyclerview_user_available_animals);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyStateTextView = root.findViewById(R.id.empty_state_text_view);

        availableAnimals = new ArrayList<>();
        adapter = new UserAnimalsAdapter(getContext(), availableAnimals);
        recyclerView.setAdapter(adapter);

        fetchAvailableAnimalsForUser();

        return root;
    }

    private void fetchAvailableAnimalsForUser() {
        // Dohvati trenutnog korisnika
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);
        UserModel currentUser = new Gson().fromJson(userJson, UserModel.class);

        if (currentUser == null) {
            return;
        }

        // API poziv za dohvaćanje dostupnih životinja
        apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAllAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AnimalModel> allAnimals = response.body();
                    availableAnimals.clear();

                    // Filtriraj životinje koje su dostupne za udomljavanje
                    for (AnimalModel animal : allAnimals) {
                        if (!animal.isUdomljen() && animal.getIdUdomitelja() == 0) { // Prikaži samo životinje koje čekaju udomljavanje
                            availableAnimals.add(animal);
                        }
                    }

                    // Prikaz praznog stanja ako nema dostupnih životinja
                    if (availableAnimals.isEmpty()) {
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
