package com.activity.pis_azil.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.AnimalsAdapter;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.activity.pis_azil.models.RejectAdoptionModelRead;
import com.activity.pis_azil.models.SharedViewModel;
import com.activity.pis_azil.models.SifrTipLjubimca;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final int REQUEST_ADOPT_ANIMAL = 1;

    private RecyclerView recyclerView;
    private AnimalsAdapter animalsAdapter;
    private List<IsBlockedAnimalModel> animalModelList = new ArrayList<>();
    private ApiService apiService;
    private ProgressBar progressBar;
    private EditText searchBox;
    private ImageButton filterButton;
    List<Integer> listaOdbijenih = new ArrayList<>();
    private List<SifrTipLjubimca> tipLjubimcaList = new ArrayList<>();
    private String lastSearchedType = "";

    @Override
    public void onResume() {
        super.onResume();

        if (animalModelList != null && animalsAdapter != null) {
            Log.d(TAG, "Test");
            loadAllAnimals();
            animalsAdapter.notifyDataSetChanged();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Kreiran view");
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = root.findViewById(R.id.recycler_view);
        progressBar = root.findViewById(R.id.progressbar);
        searchBox = root.findViewById(R.id.search_box);
        filterButton = root.findViewById(R.id.filter_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        animalsAdapter = new AnimalsAdapter(animalModelList, getContext());
        recyclerView.setAdapter(animalsAdapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        Log.d(TAG,"fsdfsdd");
        getTipoveLjubimaca();
        getOdbijeneZivotinje();
        //loadAllAnimals();
        // loadAllAdoptedAnimals();

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

    // Osluškivanje rezultata u HomeFragmentu
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult called with requestCode: " + requestCode + " resultCode: " + resultCode);

        if (requestCode == REQUEST_ADOPT_ANIMAL && resultCode == FragmentActivity.RESULT_OK && data != null) {
            int adoptedAnimalId = data.getIntExtra("udomljena_zivotinja_id", -1);
            int rejectedAnimalId = data.getIntExtra("odbijena_zivotinja_id", -1);

            if (adoptedAnimalId != -1) {
                // Ukloni udomljenu životinju iz liste i osveži adapter
                animalModelList.removeIf(animal -> animal.getIdLjubimca() == adoptedAnimalId);
                Log.d(TAG, "Broj preostalih životinja nakon uklanjanja: " + animalModelList.size());
                animalsAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Životinja je uspješno udomljena!", Toast.LENGTH_SHORT).show();
            }

            if (rejectedAnimalId != -1) {
                // Odbijeni zahtjev - postavi status udomljavanja na false
                animalModelList.stream()
                        .filter(animal -> animal.getIdLjubimca() == rejectedAnimalId)
                        .forEach(animal -> animal.setStatusUdomljavanja(false));
                loadAllAnimals();
                animalsAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Zahtjev za udomljavanje je odbijen!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Otvaranje filter dijaloga
    private void openFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Filtriraj životinje");

        View filterView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filters, null);
        builder.setView(filterView);

        Spinner typeSpinner = filterView.findViewById(R.id.spinner_type);
        Spinner ageSpinner = filterView.findViewById(R.id.spinner_age);
        Spinner colorSpinner = filterView.findViewById(R.id.spinner_color);

        // Postavljanje opcija u Spinner
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.animal_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> ageAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.animal_ages, android.R.layout.simple_spinner_item);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ageSpinner.setAdapter(ageAdapter);

        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.animal_colors, android.R.layout.simple_spinner_item);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);

        AlertDialog dialog = builder.create();

        // Potvrda filtera
        Button confirmButton = filterView.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(v -> {
            String selectedType = typeSpinner.getSelectedItem().toString();
            String selectedAge = ageSpinner.getSelectedItem().toString();
            String selectedColor = colorSpinner.getSelectedItem().toString();

            // Find the corresponding integer ID for the selected type
            applyFilters(selectedType, selectedAge, selectedColor); // Prosljeđivanje naziva, a ne ID-a
            dialog.dismiss();
        });

        // Odustajanje
        Button cancelButton = filterView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Resetiranje filtera
        Button resetButton = filterView.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(v -> {
            // Resetiraj spinner-e na prvu opciju (koja predstavlja "Sve" ili default)
            typeSpinner.setSelection(0);
            ageSpinner.setSelection(0);
            colorSpinner.setSelection(0);

            // Resetiraj filtrirane podatke i ponovno učitaj sve životinje
            resetFilter();
            dialog.dismiss();
        });

        dialog.show();
    }

    // Metoda za resetiranje filtera
    private void resetFilter() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Resetting filters and reloading all animals");
        loadAllAnimals(); // Ponovno učitaj sve životinje
        progressBar.setVisibility(View.GONE);
    }

    // Method to get the integer ID of the pet type based on the name
    private Integer getTypeIdByName(String name) {
        for (SifrTipLjubimca tip : tipLjubimcaList) {
            if (tip.getNaziv().equalsIgnoreCase(name)) {
                return tip.getId(); // Assuming 'getSifra()' returns the integer ID for the type
            }
        }
        return null; // Return null if no match found, indicating "All types"
    }

    private String getTypeNameById(int id) {
        for (SifrTipLjubimca tip : tipLjubimcaList) {
            if (tip.getId() == id) {
                return tip.getNaziv(); // Vraća naziv (npr. "Pas" ili "Mačka")
            }
        }
        return "Nepoznato"; // Ako ID ne postoji
    }


    // Metoda za primjenu filtera
    private void applyFilters(String typeName, String age, String color) {
        Integer minDob = null;
        Integer maxDob = null;

        // Postavljanje dobi na osnovu izabrane opcije
        if (!age.equals("Sve")) {
            switch (age) {
                case "0 do 1 godina":
                    minDob = 0;
                    maxDob = 1;
                    break;
                case "2 do 5 godina":
                    minDob = 2;
                    maxDob = 5;
                    break;
                case "5+ godina":
                    minDob = 6;
                    maxDob = 20; // Maksimalna starost
                    break;
            }
        }

        Log.d(TAG, "Filtriraj: tip=" + (typeName != null ? typeName : "Sve") + ", dobMin=" + minDob + ", dobMax=" + maxDob + ", boja=" + color);

        progressBar.setVisibility(View.VISIBLE);

        // API poziv koji koristi naziv tipa umjesto ID-a
        apiService.getFilteredAnimalsByAgeRange(
                typeName.equals("Sve") ? null : typeName,  // Prosljeđivanje naziva umjesto ID-a
                minDob == null ? 0 : minDob,
                maxDob == null ? 100 : maxDob,
                null,
                color.equals("Sve") ? null : color
        ).enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    animalModelList.clear();
                    List<IsBlockedAnimalModel> mappedAnimals = response.body().stream()
                            .map(animal -> new IsBlockedAnimalModel(
                                    animal.getIdLjubimca(),
                                    animal.getIdUdomitelja(),
                                    animal.getImeLjubimca(),
                                    getTypeNameById(Integer.parseInt(animal.getTipLjubimca())), // Ovdje će sada biti naziv umjesto ID-a
                                    animal.getOpisLjubimca(),
                                    animal.isUdomljen(),
                                    animal.isZahtjevUdomljavanja(),
                                    animal.getDatum(),
                                    animal.getVrijeme(),
                                    animal.getImgUrl(),
                                    animal.StanjeZivotinje(),
                                    false,
                                    animal.getDob(),
                                    animal.getBoja()
                            ))
                            .collect(Collectors.toList());

                    animalModelList.addAll(mappedAnimals);
                    animalsAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Greška u filtriranju životinja");
                }
            }

            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Greška u filtriranju životinja", t);
            }
        });
    }


    // Metoda za dohvat tipova ljubimaca
    private void getTipoveLjubimaca() {
        apiService.getAnimalTypes().enqueue(new Callback<List<SifrTipLjubimca>>() {
            @Override
            public void onResponse(Call<List<SifrTipLjubimca>> call, Response<List<SifrTipLjubimca>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tipLjubimcaList = response.body();
                    Log.d(TAG, "Dohvaćeni tipovi ljubimaca: " + tipLjubimcaList.size());
                } else {
                    // Toast.makeText(getContext(), "Greška u dohvaćanju tipova ljubimaca", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SifrTipLjubimca>> call, Throwable t) {
                // Toast.makeText(getContext(), "Greška u dohvaćanju tipova ljubimaca", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchAnimalsByType(String type) {
        if (type == null || type.isEmpty()) {
            type = lastSearchedType;
            loadAllAnimals();
        } else {
            lastSearchedType = type;
        }

        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Searching animals by type: " + type);
        String finalType = type;

        apiService.getAnimalsByType(type).enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Search animals by type - Response received");
                Log.d(TAG, "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Response body size: " + response.body().size());

                    // Filtriraj životinje koje su udomljene
                    List<IsBlockedAnimalModel> availableAnimals = response.body().stream()
                            .filter(animal -> !animal.isUdomljen() && !animal.isZahtjevUdomljavanja()) // Samo neudomljene životinje
                            .map(animal -> new IsBlockedAnimalModel(
                                    animal.getIdLjubimca(),
                                    animal.getIdUdomitelja(),
                                    animal.getImeLjubimca(),
                                    finalType,
                                    animal.getOpisLjubimca(),
                                    animal.isUdomljen(),
                                    animal.isZahtjevUdomljavanja(),
                                    animal.getDatum(),
                                    animal.getVrijeme(),
                                    animal.getImgUrl(),
                                    animal.StanjeZivotinje(),
                                    false,
                                    animal.getDob(),
                                    animal.getBoja()
                            ))
                            .collect(Collectors.toList());

                    animalModelList.clear();
                    animalModelList.addAll(availableAnimals);
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
                Log.d(TAG, "Fetch all animals - Response received NOVO");
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response is successful: " + response.isSuccessful());
                Log.d(TAG, "Response body: " + response.body());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Number of animals fetched: " + response.body().size());
                    animalModelList.clear();

                    // Filtriraj životinje koje su već udomljene
                    List<AnimalModel> availableAnimals = response.body().stream()
                            .filter(animal -> !animal.isUdomljen() && !animal.isStatusUdomljavanja())
                            .collect(Collectors.toList());

                    for (AnimalModel animal : availableAnimals) {
                        Log.i("zivotinja",animal.getImeLjubimca()+String.valueOf(animal.getIdLjubimca())+String.valueOf(listaOdbijenih.contains(animal.getIdLjubimca())));
                        if (animal.isZahtjevUdomljavanja() == false && listaOdbijenih.contains(animal.getIdLjubimca())){
                            animalModelList.add(new IsBlockedAnimalModel(
                                    animal.getIdLjubimca(),
                                    animal.getIdUdomitelja(),
                                    animal.getImeLjubimca(),
                                    animal.getTipLjubimca(),
                                    animal.getOpisLjubimca(),
                                    animal.isUdomljen(),
                                    animal.isZahtjevUdomljavanja(),
                                    animal.getDatum(),
                                    animal.getVrijeme(),
                                    animal.getImgUrl(),
                                    animal.StanjeZivotinje(),
                                    true,
                                    animal.getDob(),
                                    animal.getBoja()));
                            animal.isZahtjevUdomljavanja();
                            Log.d(TAG, "Test" + animal.isZahtjevUdomljavanja());
                        } else if (animal.isZahtjevUdomljavanja() == false) {
                            animalModelList.add(new IsBlockedAnimalModel(
                                    animal.getIdLjubimca(),
                                    animal.getIdUdomitelja(),
                                    animal.getImeLjubimca(),
                                    animal.getTipLjubimca(),
                                    animal.getOpisLjubimca(),
                                    animal.isUdomljen(),
                                    animal.isZahtjevUdomljavanja(),
                                    animal.getDatum(),
                                    animal.getVrijeme(),
                                    animal.getImgUrl(),
                                    animal.StanjeZivotinje(),
                                    false,
                                    animal.getDob(),
                                    animal.getBoja()));
                            animal.isZahtjevUdomljavanja();
                            Log.d(TAG, "Test" + animal.isZahtjevUdomljavanja());
                        }
                    }
                    animalsAdapter.notifyDataSetChanged();
                    List<AnimalModel> animals = response.body();
                    Log.d(TAG, "Animals received from server: " + animals.toString());
                    // fetchAdoptedAnimals(animals);
                    Log.d(TAG, "Test123" + animalModelList.size());
                    // animalModelList.removeIf(animal -> animal.getDob() != 18);
                    Log.d(TAG, "Test1243" + animalModelList.size());
                    animalsAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error fetching animals. Response code: " + response.code() + ", Message: " + response.message());
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

    private void loadAllAdoptedAnimals() {
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

                    // Filtriraj životinje koje su već udomljene
                    List<AnimalModel> availableAnimals = response.body().stream()
                            .filter(animal -> !animal.isUdomljen() && !animal.isStatusUdomljavanja())
                            .collect(Collectors.toList());

                    for (AnimalModel animal : availableAnimals) {
                        animalModelList.add(new IsBlockedAnimalModel(
                                animal.getIdLjubimca(),
                                animal.getIdUdomitelja(),
                                animal.getImeLjubimca(),
                                animal.getTipLjubimca(),
                                animal.getOpisLjubimca(),
                                animal.isUdomljen(),
                                animal.isZahtjevUdomljavanja(),
                                animal.getDatum(),
                                animal.getVrijeme(),
                                animal.getImgUrl(),
                                animal.StanjeZivotinje(),
                                false,
                                animal.getDob(),
                                animal.getBoja()
                        ));
                        Log.d(TAG,"Ispis2" + animal.isZahtjevUdomljavanja());
                    }
                    animalsAdapter.notifyDataSetChanged();
                    List<AnimalModel> animals = response.body();
                    Log.d(TAG, "Animals received from server: " + animals.toString());
                    fetchAdoptedAnimals(animals);
                } else {
                    Log.e(TAG, "Error fetching animals. Response code: " + response.code() + ", Message: " + response.message());
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
                                                animal.isZahtjevUdomljavanja(),
                                                animal.getDatum(),
                                                animal.getVrijeme(),
                                                animal.getImgUrl(),
                                                animal.StanjeZivotinje(),
                                                false,
                                                animal.getDob(),
                                                animal.getBoja()))
                                        .collect(Collectors.toList()));
                                animalsAdapter.notifyDataSetChanged();
                            } else {
                                List<IsBlockedAnimalModel> mappedAnimals = animals.stream()
                                        .map(animal -> {
                                            boolean isBlocked = response.body().stream()
                                                    .anyMatch(blocked -> blocked.getIme_ljubimca().equals(animal.getImeLjubimca())
                                                            && blocked.getId_korisnika() == currentUser.getIdKorisnika());
                                            return new IsBlockedAnimalModel(animal.getIdLjubimca(), animal.getIdUdomitelja(), animal.getImeLjubimca(), animal.getTipLjubimca(), animal.getOpisLjubimca(), animal.isUdomljen(), animal.isZahtjevUdomljavanja(), animal.getDatum(), animal.getVrijeme(), animal.getImgUrl(), animal.StanjeZivotinje(), false, animal.getDob(), animal.getBoja());

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

    private void getOdbijeneZivotinje() {
        Call<List<RejectAdoptionModelRead>> call = apiService.getOdbijeneZivotinje();
        call.enqueue(new Callback<List<RejectAdoptionModelRead>>() {
            @Override
            public void onResponse(Call<List<RejectAdoptionModelRead>> call, Response<List<RejectAdoptionModelRead>> response) {
                Log.d(TAG, "Fetch blocked animals - Response received");
                Log.d(TAG, "Response code: " + response.code());
                listaOdbijenih.clear();
                if (response.isSuccessful() && response.body() != null) {
                    List<RejectAdoptionModelRead> odbijeneZivotinje = response.body();
                    for (RejectAdoptionModelRead zivotinja : odbijeneZivotinje){
                        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
                        sharedViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
                            if (userId != null && !userId.isEmpty()) { // Proverite da li userId nije null i nije prazan
                                try {
                                    if (Objects.equals(Integer.parseInt(userId), zivotinja.getId_korisnika())) {
                                        listaOdbijenih.add(zivotinja.getId_ljubimca());
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Invalid userId format: " + userId, e);
                                }
                            } else {
                                Log.w(TAG, "userId is null or empty, skipping comparison");
                            }
                        });
                    }
                    loadAllAnimals();
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