package com.activity.pis_azil.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.MyAdoptedAnimalsAdapter;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAdoptedFragment extends Fragment {

    private ApiService apiService;
    private MyAdoptedAnimalsAdapter adapter;
    private EditText searchAdopterBox;
    private RecyclerView recyclerView;
    private List<UpdateDnevnikModel> adoptedAnimalsList;
    public List<UpdateDnevnikModel> filteredAdoptedAnimalsList;
    private TextView newAnimalsTextView;
    private ImageView newAnimalsImageView;
    private Map<Integer, String> userMap = new HashMap<>();

    public MyAdoptedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_adopted, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);

        searchAdopterBox = root.findViewById(R.id.search_adopter_box);
        recyclerView = root.findViewById(R.id.adopted_animals_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adoptedAnimalsList = new ArrayList<>();
        filteredAdoptedAnimalsList = new ArrayList<>();

        adapter = new MyAdoptedAnimalsAdapter(getActivity(), filteredAdoptedAnimalsList);
        recyclerView.setAdapter(adapter);

        newAnimalsTextView = root.findViewById(R.id.new_animals_textview);
        newAnimalsImageView = root.findViewById(R.id.new_animals_img);

        fetchUsers();

        searchAdopterBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString().toLowerCase();
                filterAdoptedAnimals(query);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        return root;
    }

    private void fetchUsers() {
        apiService.getAllUsers().enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (UserModel user : response.body()) {
                        userMap.put(user.getIdKorisnika(), user.getIme()); // Spremi ID korisnika i ime u mapu
                    }
                    fetchAdoptedAnimals(); // Zatim dohvati udomljene životinje
                } else {
                    Toast.makeText(getActivity(), "Greška u dohvaćanju korisnika: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Log.e("MyAdoptedFragment", "Error fetching users: ", t);
                Toast.makeText(getActivity(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Metoda za filtriranje udomljenih životinja prema imenu udomitelja
    private void filterAdoptedAnimals(String query) {
        filteredAdoptedAnimalsList.clear();
        if (query.isEmpty()) {
            // Ako je upit prazan, prikaži sve udomljene životinje
            filteredAdoptedAnimalsList.addAll(adoptedAnimalsList);
        } else {
            // Filtriraj listu prema imenu udomitelja iz modela
            for (UpdateDnevnikModel animal : adoptedAnimalsList) {
                if (animal.getImeUdomitelja() != null && animal.getImeUdomitelja().toLowerCase().contains(query.toLowerCase())) {
                    filteredAdoptedAnimalsList.add(animal);
                }
            }
        }
        // Ažurirajte adapter sa novom listom
        adapter.notifyDataSetChanged();
    }

    private void fetchAdoptedAnimals() {
        apiService.getDnevnikUdomljavanja().enqueue(new Callback<List<UpdateDnevnikModel>>() {
            @Override
            public void onResponse(Call<List<UpdateDnevnikModel>> call, Response<List<UpdateDnevnikModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adoptedAnimalsList.clear();
                    filteredAdoptedAnimalsList.clear();

                    List<UpdateDnevnikModel> list = response.body();

                    // Dohvati podatke o trenutnom korisniku iz SharedPreferences
                    SharedPreferences prefs = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    String userJson = prefs.getString("current_user", null);
                    UserModel currentUser;
                    if (userJson != null) {
                        Gson gson = new Gson();
                        currentUser = gson.fromJson(userJson, UserModel.class);
                    } else {
                        currentUser = null;
                    }

                    if (currentUser != null) {
                        if (!currentUser.isAdmin()) {
                            // Filtriraj samo životinje koje je udomio trenutni korisnik
                            list = list.stream()
                                    .filter(animal -> animal.isUdomljen() && animal.getId_korisnika() == currentUser.getIdKorisnika())
                                    .collect(Collectors.toList());
                        } else {
                            // Admin vidi sve udomljene životinje
                            list = list.stream().filter(UpdateDnevnikModel::isUdomljen).collect(Collectors.toList());
                        }
                    }

                    // Dodajte životinje u listu
                    for (UpdateDnevnikModel animal : list) {
                        String imeUdomitelja = userMap.getOrDefault(animal.getId_korisnika(), "Nepoznato");
                        animal.setImeUdomitelja(imeUdomitelja);
                        adoptedAnimalsList.add(animal);
                    }

                    filteredAdoptedAnimalsList.addAll(adoptedAnimalsList);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                } else {
                    Toast.makeText(getActivity(), "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UpdateDnevnikModel>> call, Throwable t) {
                Log.e("MyAdoptedFragment", "Error fetching documents: ", t);
                Toast.makeText(getActivity(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (filteredAdoptedAnimalsList.isEmpty()) {
            newAnimalsTextView.setVisibility(View.VISIBLE);
            newAnimalsImageView.setVisibility(View.VISIBLE);
        } else {
            newAnimalsTextView.setVisibility(View.GONE);
            newAnimalsImageView.setVisibility(View.GONE);
        }
    }
}
