package com.activity.pis_azil.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.activity.pis_azil.models.RejectAdoptionModelRead;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private RecyclerView recyclerView;
    private AnimalsAdapter animalsAdapter;
    private List<IsBlockedAnimalModel> animalModelList = new ArrayList<>();
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

    // TODO
    private void searchAnimalsByType(String type) {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Searching animals by type: " + type);
        apiService.getAnimalsByType(type).enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    animalModelList.clear();
                    List<IsBlockedAnimalModel> animals = response.body().stream().map(animal -> {
                        return new IsBlockedAnimalModel(
                                animal.getIdLjubimca(),
                                animal.getIdUdomitelja(),
                                animal.getImeLjubimca(),
                                animal.getTipLjubimca(),
                                animal.getOpisLjubimca(),
                                animal.isUdomljen(),
                                animal.getDatum(),
                                animal.getVrijeme(),
                                animal.getImgUrl(),
                                animal.StanjeZivotinje(),
                                false
                        );
                    }).collect(Collectors.toList());
                    Log.d(TAG, "Found " + animals.size() + " animals of type: " + type);
                    for (IsBlockedAnimalModel animal : animals) {
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

    List<MyAdoptionModel> cartModelList = new ArrayList<>();

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

                    // Dohvati posvojene životinje, a filtriranje izvrši unutar odgovora tog API poziva
                    fetchAdoptedAnimals(animals);

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

    private void fetchAdoptedAnimals(List<AnimalModel> animals) {
        Log.d(TAG, "Fetching adopted animals");

        apiService.getDnevnikUdomljavanja().enqueue(new Callback<List<UpdateDnevnikModel>>() {
            @Override
            public void onResponse(Call<List<UpdateDnevnikModel>> call, Response<List<UpdateDnevnikModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully fetched adopted animals, size: " + response.body().size());
                    cartModelList.clear();
                    for (UpdateDnevnikModel animal : response.body()) {
                        if (animal.isUdomljen()) {
                            MyAdoptionModel adoption = new MyAdoptionModel();
                            adoption.setImeLjubimca(animal.getIme_ljubimca());
                            cartModelList.add(adoption);
                        }
                    }

                    // Filtriraj sve životinje tek nakon što je dohvaćen popis posvojenih
                    List<AnimalModel> filteredAnimals = animals.stream()
                            .filter(item -> !cartModelList.stream()
                                    .map(MyAdoptionModel::getImeLjubimca)
                                    .collect(Collectors.toList())
                                    .contains(item.getImeLjubimca()))
                            .collect(Collectors.toList());


                    fetchBlokiranjeZivotinje(filteredAnimals);

                } else {
                    Log.e(TAG, "Failed to fetch adopted animals: " + response.message());
                    Toast.makeText(getActivity(), "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UpdateDnevnikModel>> call, Throwable t) {
                Log.e(TAG, "Error fetching adopted animals: ", t);
                Toast.makeText(getActivity(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchBlokiranjeZivotinje(List<AnimalModel> animals) {
        Log.d(TAG, "Fetching blocked animals");

        Call<List<RejectAdoptionModelRead>> call = apiService.getOdbijeneZivotinje();
        call.enqueue(new Callback<List<RejectAdoptionModelRead>>() {
            @Override
            public void onResponse(Call<List<RejectAdoptionModelRead>> call, Response<List<RejectAdoptionModelRead>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SharedPreferences prefs = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    String userJson = prefs.getString("current_user", null);
                    if (userJson != null) {
                        Gson gson = new Gson();
                        UserModel currentUser = gson.fromJson(userJson, UserModel.class);
                        if (currentUser != null) {

                            if (currentUser.isAdmin()) {

                                animalModelList.clear();
                                animalModelList.addAll(animals.stream()
                                        .map(animal -> {
                                            return new IsBlockedAnimalModel(
                                                    animal.getIdLjubimca(),
                                                    animal.getIdUdomitelja(),
                                                    animal.getImeLjubimca(),
                                                    animal.getTipLjubimca(),
                                                    animal.getOpisLjubimca(),
                                                    animal.isUdomljen(),
                                                    animal.getDatum(),
                                                    animal.getVrijeme(),
                                                    animal.getImgUrl(),
                                                    animal.StanjeZivotinje(),
                                                    false
                                            );
                                        }).collect(Collectors.toList()));
                                animalsAdapter.notifyDataSetChanged();


                            } else {
                                List<IsBlockedAnimalModel> mappedAnimals = animals.stream()
                                        .map(animal -> {
                                            boolean isBlocked = response.body().stream()
                                                    .anyMatch(blocked -> blocked.getIme_ljubimca().equals(animal.getImeLjubimca())
                                                            && blocked.getId_korisnika() == currentUser.getIdKorisnika());

                                            // Mapiraj AnimalModel u IsBlockedAnimalModel
                                            return new IsBlockedAnimalModel(
                                                    animal.getIdLjubimca(),
                                                    animal.getIdUdomitelja(),
                                                    animal.getImeLjubimca(),
                                                    animal.getTipLjubimca(),
                                                    animal.getOpisLjubimca(),
                                                    animal.isUdomljen(),
                                                    animal.getDatum(),
                                                    animal.getVrijeme(),
                                                    animal.getImgUrl(),
                                                    animal.StanjeZivotinje(),
                                                    isBlocked
                                            );
                                        })
                                        .collect(Collectors.toList());

                                animalModelList.clear();
                                animalModelList.addAll(mappedAnimals);
                                animalsAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RejectAdoptionModelRead>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch blocked animals", t);
            }
        });
    }


}
