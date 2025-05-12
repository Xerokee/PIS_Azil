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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private boolean isLoadingData = false;

    @Override
    public void onResume() {
        super.onResume();

        if (animalModelList != null && animalsAdapter != null) {
            Log.d(TAG, "Test");
            Log.d(TAG, "onResume: Fragment resumed. Refreshing data.");
            refreshAllData();
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
        animalModelList = new ArrayList<>();
        animalsAdapter = new AnimalsAdapter(animalModelList, getContext());
        recyclerView.setAdapter(animalsAdapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        Log.d(TAG,"fsdfsdd");
        getTipoveLjubimaca();
        refreshAllData();
        loadAllAnimals();
        // loadAllAdoptedAnimals();
        // getToken();

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

        filterButton.setOnClickListener(v -> openFilterDialog());

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult called with requestCode: " + requestCode + " resultCode: " + resultCode);

        if (requestCode == REQUEST_ADOPT_ANIMAL && resultCode == FragmentActivity.RESULT_OK && data != null) {
            int adoptedAnimalId = data.getIntExtra("udomljena_zivotinja_id", -1);
            int rejectedAnimalId = data.getIntExtra("odbijena_zivotinja_id", -1);

            if (adoptedAnimalId != -1) {
                animalModelList.removeIf(animal -> animal.getIdLjubimca() == adoptedAnimalId);
                Log.d(TAG, "Broj preostalih životinja nakon uklanjanja: " + animalModelList.size());
                animalsAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Životinja je uspješno udomljena!", Toast.LENGTH_SHORT).show();
            } else if (rejectedAnimalId != -1) {
                Log.d(TAG, "onActivityResult: Adoption rejected for animal ID: " + rejectedAnimalId + ". Refreshing all data.");
                refreshAllData();
                Toast.makeText(getContext(), "Zahtjev za udomljavanje je odbijen!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Filtriraj životinje");

        View filterView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filters, null);
        builder.setView(filterView);

        Spinner typeSpinner = filterView.findViewById(R.id.spinner_type);
        Spinner ageSpinner = filterView.findViewById(R.id.spinner_age);
        Spinner colorSpinner = filterView.findViewById(R.id.spinner_color);

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

        Button confirmButton = filterView.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(v -> {
            String selectedType = typeSpinner.getSelectedItem().toString();
            String selectedAge = ageSpinner.getSelectedItem().toString();
            String selectedColor = colorSpinner.getSelectedItem().toString();

            applyFilters(selectedType, selectedAge, selectedColor);
            dialog.dismiss();
        });

        Button cancelButton = filterView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        Button resetButton = filterView.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(v -> {
            typeSpinner.setSelection(0);
            ageSpinner.setSelection(0);
            colorSpinner.setSelection(0);

            resetFilter();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void resetFilter() {
        Log.d(TAG, "Resetting filters and reloading all animals");
        if(searchBox != null) searchBox.setText("");
        lastSearchedType = "";
        refreshAllData();
    }

    private String getTypeNameById(int id) {
        for (SifrTipLjubimca tip : tipLjubimcaList) {
            if (tip.getId() == id) {
                return tip.getNaziv();
            }
        }
        return "pas";
    }

    private void applyFilters(String typeName, String age, String color) {
        Integer minDob = null;
        Integer maxDob = null;

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
                    maxDob = 20;
                    break;
            }
        }
        Log.d(TAG, "Filtriraj: tip=" + (typeName != null ? typeName : "Sve") + ", dobMin=" + minDob + ", dobMax=" + maxDob + ", boja=" + color);
        progressBar.setVisibility(View.VISIBLE);

        Set<Integer> blockedAnimalIds = new HashSet<>();
        for (IsBlockedAnimalModel animal : animalModelList) {
            if (animal.isBlocked()) {
                blockedAnimalIds.add(animal.getIdLjubimca());
            }
        }

        apiService.getFilteredAnimalsByAgeRange(
                typeName.equals("Sve") ? null : typeName,
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
                            .map(animal -> {
                                boolean wasBlocked = blockedAnimalIds.contains(animal.getIdLjubimca());
                                return new IsBlockedAnimalModel(
                                        animal.getIdLjubimca(),
                                        animal.getIdUdomitelja(),
                                        animal.getImeLjubimca(),
                                        getTypeNameById(Integer.parseInt(animal.getTipLjubimca())),
                                        animal.getOpisLjubimca(),
                                        animal.isUdomljen(),
                                        animal.isZahtjevUdomljavanja(),
                                        animal.getDatum(),
                                        animal.getVrijeme(),
                                        animal.getImgUrl(),
                                        animal.StanjeZivotinje(),
                                        wasBlocked,
                                        animal.getDob(),
                                        animal.getBoja()
                                );
                            })
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

    private void getTipoveLjubimaca() {
        if (apiService == null) {
            Log.e(TAG, "getTipoveLjubimaca: apiService is null! Aborting chain.");
            if(progressBar != null) progressBar.setVisibility(View.GONE);
            isLoadingData = false;
            return;
        }
        Log.d(TAG, "getTipoveLjubimaca: Fetching animal types...");
        apiService.getAnimalTypes().enqueue(new Callback<List<SifrTipLjubimca>>() {
            @Override
            public void onResponse(Call<List<SifrTipLjubimca>> call, Response<List<SifrTipLjubimca>> response) {
                if (getContext() == null) { isLoadingData = false; return; }
                if (response.isSuccessful() && response.body() != null) {
                    tipLjubimcaList = response.body();
                    Log.d(TAG, "getTipoveLjubimaca: Success. Count: " + tipLjubimcaList.size());
                } else {
                    Log.w(TAG, "getTipoveLjubimaca: Error fetching types. Code: " + response.code());
                    tipLjubimcaList.clear();
                }
                getOdbijeneZivotinje();
            }

            @Override
            public void onFailure(Call<List<SifrTipLjubimca>> call, Throwable t) {
                if (getContext() == null) { isLoadingData = false; return; }
                Log.e(TAG, "getTipoveLjubimaca: API call failed.", t);
                tipLjubimcaList.clear();
                getOdbijeneZivotinje();
            }
        });
    }

    private void searchAnimalsByType(String type) {
        Integer typeId = null;

        if (type == null || type.isEmpty()) {
            type = lastSearchedType;
            loadAllAnimals();
            return;
        } else {
            lastSearchedType = type;
            if (type.equalsIgnoreCase("pas")) {
                typeId = 1;
            } else if (type.equalsIgnoreCase("macka") || type.equalsIgnoreCase("mačka")) {
                typeId = 2;
            } else {
                Toast.makeText(getContext(), "Nepoznat tip životinje", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Searching animals by type ID: " + typeId);

        Set<Integer> blockedAnimalIds = new HashSet<>();
        for (IsBlockedAnimalModel animal : animalModelList) {
            if (animal.isBlocked()) {
                blockedAnimalIds.add(animal.getIdLjubimca());
            }
        }

        String finalType = type;
        apiService.getAnimalsByType(typeId).enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Search animals by type - Response received");
                Log.d(TAG, "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    List<IsBlockedAnimalModel> availableAnimals = response.body().stream()
                            .filter(animal -> !animal.isUdomljen() && !animal.isZahtjevUdomljavanja())
                            .map(animal -> {
                                boolean wasBlocked = blockedAnimalIds.contains(animal.getIdLjubimca());
                                return new IsBlockedAnimalModel(
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
                                        wasBlocked,
                                        animal.getDob(),
                                        animal.getBoja()
                                );
                            })
                            .collect(Collectors.toList());

                    animalModelList.clear();
                    animalModelList.addAll(availableAnimals);
                    animalsAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error searching animals by type. Code: " + response.code());
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
        if (getContext() == null || apiService == null) {
            Log.w(TAG, "loadAllAnimals - Context or apiService is null, aborting.");
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            isLoadingData = false;
            return;
        }
        if (progressBar != null && progressBar.getVisibility() == View.GONE) {
            progressBar.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "Fetching all animals (loadAllAnimals)...");
        Log.d(TAG, "Current listaOdbijenih at start of loadAllAnimals: " + listaOdbijenih.toString());

        apiService.getAllAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                if (getContext() == null) {
                    Log.w(TAG, "loadAllAnimals onResponse - getContext() is null, aborting.");
                    isLoadingData = false; // Resetiraj zastavicu
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    return;
                }
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Fetched all animals - Response successful. Count: " + response.body().size());
                    animalModelList.clear();

                    List<AnimalModel> animalsToConsiderForDisplay = response.body().stream()
                            .filter(animal -> {
                                boolean isAdopted = animal.isUdomljen();
                                boolean isPendingRequest = animal.isZahtjevUdomljavanja();
                                return !isAdopted && !isPendingRequest;
                            })
                            .collect(Collectors.toList());
                    Log.d(TAG, "Animals to consider for display (after hiding adopted/pending): " + animalsToConsiderForDisplay.size());

                    for (AnimalModel animal : animalsToConsiderForDisplay) {
                        boolean isBlockedDueToPriorRejection = listaOdbijenih.contains(animal.getIdLjubimca());
                        String tipLjubimcaNaziv;
                        try {
                            if (tipLjubimcaList != null && !tipLjubimcaList.isEmpty()) {
                                tipLjubimcaNaziv = getTypeNameById(Integer.parseInt(animal.getTipLjubimca()));
                            } else {
                                tipLjubimcaNaziv = animal.getTipLjubimca();
                            }
                        } catch (NumberFormatException e) {
                            tipLjubimcaNaziv = animal.getTipLjubimca();
                        }

                        animalModelList.add(new IsBlockedAnimalModel(
                                animal.getIdLjubimca(), animal.getIdUdomitelja(), animal.getImeLjubimca(),
                                tipLjubimcaNaziv, animal.getOpisLjubimca(), false, false,
                                animal.getDatum(), animal.getVrijeme(), animal.getImgUrl(),
                                animal.StanjeZivotinje(), isBlockedDueToPriorRejection,
                                animal.getDob(), animal.getBoja()
                        ));
                    }
                    if (animalsAdapter != null) {
                        animalsAdapter.notifyDataSetChanged();
                    }
                    Log.d(TAG, "Animal list updated for display. Final size: " + animalModelList.size());

                } else {
                    Log.e(TAG, "Error fetching animals. Code: " + response.code() + ", Message: " + response.message());
                    if (getContext() != null) {
                    }
                }
                isLoadingData = false;
            }

            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                if (getContext() == null) {
                    Log.w(TAG, "loadAllAnimals onFailure - getContext() is null, aborting.");
                    isLoadingData = false;
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    return;
                }
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Failed to fetch animals", t);
                isLoadingData = false;
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
                }
            }

            @Override
            public void onFailure(Call<List<UpdateDnevnikModel>> call, Throwable t) {
                Log.e(TAG, "Error fetching adopted animals: ", t);
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

    private int getCurrentUserId() {
        if (getContext() == null) {
            Log.w(TAG, "getCurrentUserId: getContext() is null.");
            return -1;
        }
        if (getActivity() != null) {
            SharedViewModel sharedViewModel = null;
            try {
                sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
                String userIdStr = sharedViewModel.getUserId().getValue();
                if (userIdStr != null && !userIdStr.isEmpty()) {
                    return Integer.parseInt(userIdStr);
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "getCurrentUserId: Error getting ViewModel, activity might be destroying.", e);
            } catch (NumberFormatException e) {
                Log.e(TAG, "getCurrentUserId: Invalid userId format in ViewModel: " + sharedViewModel.getUserId().getValue(), e);
            }
        }
        SharedPreferences userPrefs = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = userPrefs.getString("current_user", null);
        if (userJson != null) {
            UserModel currentUser = new Gson().fromJson(userJson, UserModel.class);
            if (currentUser != null) {
                return currentUser.getIdKorisnika();
            }
        }
        Log.w(TAG, "getCurrentUserId: Nije moguće dohvatiti ID korisnika.");
        return -1;
    }

    private void getOdbijeneZivotinje() {
        if (getContext() == null || apiService == null) {
            Log.w(TAG, "getOdbijeneZivotinje - Context or apiService is null, aborting.");
            listaOdbijenih.clear();
            if(progressBar != null) progressBar.setVisibility(View.GONE);
            isLoadingData = false;
            return;
        }

        final int currentUserId = getCurrentUserId();
        if (currentUserId == -1) {
            Log.w(TAG, "getOdbijeneZivotinje: Invalid user ID (-1). Proceeding with empty listaOdbijenih.");
            listaOdbijenih.clear();
            loadAllAnimals();
            return;
        }
        Log.d(TAG, "getOdbijeneZivotinje: Fetching rejected animals for user ID: " + currentUserId);
        apiService.getOdbijeneZivotinje().enqueue(new Callback<List<RejectAdoptionModelRead>>() {
            @Override
            public void onResponse(Call<List<RejectAdoptionModelRead>> call, Response<List<RejectAdoptionModelRead>> response) {
                if (getContext() == null) { isLoadingData = false; if(progressBar != null) progressBar.setVisibility(View.GONE); return; }
                listaOdbijenih.clear();
                if (response.isSuccessful() && response.body() != null) {
                    List<RejectAdoptionModelRead> sveOdbijeneSaServera = response.body();
                    Log.d(TAG, "getOdbijeneZivotinje - Uspješno dohvaćeno " + sveOdbijeneSaServera.size() + " odbijenih zapisa s servera.");
                    for (RejectAdoptionModelRead odbijenaZivotinja : sveOdbijeneSaServera) {
                        if (odbijenaZivotinja.getId_korisnika() != null && odbijenaZivotinja.getId_korisnika() == currentUserId) {
                            if (odbijenaZivotinja.getId_ljubimca() != null && !listaOdbijenih.contains(odbijenaZivotinja.getId_ljubimca())) {
                                listaOdbijenih.add(odbijenaZivotinja.getId_ljubimca());
                            }
                        }
                    }
                    Log.d(TAG, "getOdbijeneZivotinje - Popunjena listaOdbijenih za korisnika " + currentUserId + ": " + listaOdbijenih.toString());
                } else {
                    Log.e(TAG, "getOdbijeneZivotinje: Failed to fetch. Code: " + response.code());
                }
                loadAllAnimals();
            }
            @Override
            public void onFailure(Call<List<RejectAdoptionModelRead>> call, Throwable t) {
                if (getContext() == null) { isLoadingData = false; if(progressBar != null) progressBar.setVisibility(View.GONE); return; }
                Log.e(TAG, "getOdbijeneZivotinje: API call failed.", t);
                listaOdbijenih.clear();
                loadAllAnimals();
            }
        });
    }

    /*
    private void getToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.i("FCM", "Token: " + token);
                });
    }
    */

    private void refreshAllData() {
        if (isLoadingData) {
            Log.d(TAG, "refreshAllData: Data is already loading, skipping new request.");
            return;
        }
        if (getContext() == null || apiService == null) {
            Log.w(TAG, "refreshAllData: Context or apiService is null, cannot refresh.");
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            isLoadingData = false;
            return;
        }
        Log.d(TAG, "refreshAllData: Initiating data refresh sequence...");
        isLoadingData = true;
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        getTipoveLjubimaca();
    }
}