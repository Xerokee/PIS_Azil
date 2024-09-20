package com.activity.pis_azil.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private ImageButton filterButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = root.findViewById(R.id.recycler_view);
        progressBar = root.findViewById(R.id.progressbar);
        searchBox = root.findViewById(R.id.search_box);
        filterButton = root.findViewById(R.id.filter_button);

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

        // Listener za otvaranje filter dijaloga
        filterButton.setOnClickListener(v -> openFilterDialog());

        return root;
    }

    // Unutar metode openFilterDialog
    private void openFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Filtriraj životinje");

        View filterView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filters, null);
        builder.setView(filterView);

        // Polja za unos filtera
        EditText nameFilter = filterView.findViewById(R.id.filter_name);
        EditText typeFilter = filterView.findViewById(R.id.filter_type);

        // Kreiranje AlertDialoga
        AlertDialog dialog = builder.create();

        // Gumb za resetiranje filtera (gore desno)
        ImageButton resetButton = filterView.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(v -> {
            resetFilter();
            dialog.dismiss(); // Zatvori dijalog nakon resetiranja filtera
        });

        // Pronađi prilagođene gumbe i postavi event listenere
        Button confirmButton = filterView.findViewById(R.id.confirm_button);
        Button cancelButton = filterView.findViewById(R.id.cancel_button);

        // Postavi klik event za potvrdu
        confirmButton.setOnClickListener(v -> {
            String name = nameFilter.getText().toString().trim();
            String type = typeFilter.getText().toString().trim();
            Log.d(TAG, "Filters applied: Name = " + name + ", Type = " + type);
            applyFilters(name, type);
            dialog.dismiss(); // Zatvori dijalog nakon primjene filtera
        });

        // Postavi klik event za odustajanje
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Prikaži dijalog
        dialog.show();
    }

    // Metoda za resetiranje filtera
    private void resetFilter() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Resetting filters and reloading all animals");
        loadAllAnimals(); // Ponovno učitaj sve životinje
        progressBar.setVisibility(View.GONE);
    }

    // Metoda za primjenu filtera
    private void applyFilters(String name, String type) {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Applying filters: Name = " + name + ", Type = " + type);

        List<IsBlockedAnimalModel> filteredList = animalModelList.stream()
                .filter(animal -> (name.isEmpty() || animal.getImeLjubimca().toLowerCase().contains(name.toLowerCase())) &&
                        (type.isEmpty() || animal.getTipLjubimca().toLowerCase().contains(type.toLowerCase())))
                .collect(Collectors.toList());

        Log.d(TAG, "Number of animals after filtering: " + filteredList.size());
        animalModelList.clear();
        animalModelList.addAll(filteredList);
        animalsAdapter.notifyDataSetChanged();

        progressBar.setVisibility(View.GONE);
    }

    private void searchAnimalsByType(String type) {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Searching animals by type: " + type);
        apiService.getAnimalsByType(type).enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Search animals by type - Response received");
                Log.d(TAG, "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Response body size: " + response.body().size());
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
                Log.d(TAG, "Fetch all animals - Response received");
                Log.d(TAG, "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Number of animals fetched: " + response.body().size());
                    animalModelList.clear();
                    List<AnimalModel> animals = response.body();
                    Log.d(TAG, "Animals received from server: " + animals.toString());
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
        Log.d(TAG, "Fetching adopted animals...");
        apiService.getDnevnikUdomljavanja().enqueue(new Callback<List<UpdateDnevnikModel>>() {
            @Override
            public void onResponse(Call<List<UpdateDnevnikModel>> call, Response<List<UpdateDnevnikModel>> response) {
                Log.d(TAG, "Fetch adopted animals - Response received");
                Log.d(TAG, "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Number of adopted animals fetched: " + response.body().size());
                    cartModelList.clear();
                    for (UpdateDnevnikModel animal : response.body()) {
                        if (animal.isUdomljen()) {
                            MyAdoptionModel adoption = new MyAdoptionModel();
                            adoption.setImeLjubimca(animal.getIme_ljubimca());
                            cartModelList.add(adoption);
                        }
                    }
                    List<AnimalModel> filteredAnimals = animals.stream()
                            .filter(item -> !cartModelList.stream()
                                    .map(MyAdoptionModel::getImeLjubimca)
                                    .collect(Collectors.toList())
                                    .contains(item.getImeLjubimca()))
                            .collect(Collectors.toList());

                    Log.d(TAG, "Number of animals after filtering adopted ones: " + filteredAnimals.size());
                    fetchBlokiranjeZivotinje(filteredAnimals);
                } else {
                    Log.e(TAG, "Failed to fetch adopted animals. Response code: " + response.code() + ", Message: " + response.message());
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
        Log.d(TAG, "Fetching blocked animals...");
        Call<List<RejectAdoptionModelRead>> call = apiService.getOdbijeneZivotinje();
        call.enqueue(new Callback<List<RejectAdoptionModelRead>>() {
            @Override
            public void onResponse(Call<List<RejectAdoptionModelRead>> call, Response<List<RejectAdoptionModelRead>> response) {
                Log.d(TAG, "Fetch blocked animals - Response received");
                Log.d(TAG, "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Number of blocked animals fetched: " + response.body().size());
                    SharedPreferences prefs = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    String userJson = prefs.getString("current_user", null);
                    Log.d(TAG, "Current user JSON: " + userJson);
                    if (userJson != null) {
                        Gson gson = new Gson();
                        UserModel currentUser = gson.fromJson(userJson, UserModel.class);
                        if (currentUser != null) {
                            Log.d(TAG, "Current user ID: " + currentUser.getIdKorisnika() + ", Is Admin: " + currentUser.isAdmin());
                            if (currentUser.isAdmin()) {
                                animalModelList.clear();
                                animalModelList.addAll(animals.stream()
                                        .map(animal -> new IsBlockedAnimalModel(
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
                                                false))
                                        .collect(Collectors.toList()));
                                animalsAdapter.notifyDataSetChanged();
                            } else {
                                List<IsBlockedAnimalModel> mappedAnimals = animals.stream()
                                        .map(animal -> {
                                            boolean isBlocked = response.body().stream()
                                                    .anyMatch(blocked -> blocked.getIme_ljubimca().equals(animal.getImeLjubimca())
                                                            && blocked.getId_korisnika() == currentUser.getIdKorisnika());
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

                                Log.d(TAG, "Number of animals after filtering blocked ones: " + mappedAnimals.size());
                                animalModelList.clear();
                                animalModelList.addAll(mappedAnimals);
                                animalsAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to fetch blocked animals. Response code: " + response.code() + ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<RejectAdoptionModelRead>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch blocked animals", t);
            }
        });
    }
}
