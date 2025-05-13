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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
    private TextView emptyListMessageTextView;
    private EditText searchBox;
    private ImageButton filterButton;

    private List<Integer> listaOdbijenih = new ArrayList<>();
    private List<SifrTipLjubimca> tipLjubimcaList = new ArrayList<>();
    private List<MyAdoptionModel> cartModelList = new ArrayList<>();

    private String lastSearchedType = "";

    private boolean isLoadingData = false;
    private boolean initialLoadDone = false;

    private void refreshAllData() {
        if (!isAdded() || getContext() == null) {
            return;
        }
        if (isLoadingData) {
            Log.d(TAG, "refreshAllData: Data is already loading, skipping.");
            return;
        }
        if (apiService == null) {
            Log.e(TAG, "refreshAllData: ApiService is null. Cannot refresh.");
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            updateUIBasedOnDataState(true, "Greška pri inicijalizaciji. Osvježite ručno pomoću gumba resetiraj u filterima.");
            return;
        }

        Log.d(TAG, "refreshAllData: Initiating data refresh sequence...");
        isLoadingData = true;
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (emptyListMessageTextView != null) emptyListMessageTextView.setVisibility(View.GONE);


        getTipoveLjubimaca();
    }

    private void stopLoadingSequence(boolean wasSuccessful) {
        isLoadingData = false;
        initialLoadDone = true;
        if (!isAdded() || getContext() == null) return;

        if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
        }

        if (animalModelList.isEmpty()) {
            if (wasSuccessful) {
                updateUIBasedOnDataState(true, "Nema dostupnih životinja za prikaz. Osvježite ručno pomoću gumba ''resetiraj'' u filterima.");
            } else {
                updateUIBasedOnDataState(true, "Podaci se nisu mogli učitati. Osvježite ručno pomoću gumba resetiraj u filterima.");
            }
        } else {
            updateUIBasedOnDataState(false, null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Fragment resumed.");
        if (!isLoadingData) {
            refreshAllData();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Initializing fragment view.");
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = root.findViewById(R.id.recycler_view);
        progressBar = root.findViewById(R.id.progressbar);
        searchBox = root.findViewById(R.id.search_box);
        filterButton = root.findViewById(R.id.filter_button);
        emptyListMessageTextView = root.findViewById(R.id.empty_list_message_textview);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        animalModelList = new ArrayList<>();
        animalsAdapter = new AnimalsAdapter(animalModelList, getContext());
        recyclerView.setAdapter(animalsAdapter);

        if (getContext() != null) {
            apiService = ApiClient.getClient().create(ApiService.class);
        } else {
            Log.e(TAG, "onCreateView: getContext() is null, ApiService not initialized!");
        }
        initialLoadDone = false;
        refreshAllData();

        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String keyword = searchBox.getText().toString().trim();
                searchAnimalsByType(keyword);
                return true;
            }
            return false;
        });
        filterButton.setOnClickListener(v -> openFilterDialog());
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADOPT_ANIMAL && resultCode == FragmentActivity.RESULT_OK && data != null) {
            int adoptedAnimalId = data.getIntExtra("udomljena_zivotinja_id", -1);
            int rejectedAnimalId = data.getIntExtra("odbijena_zivotinja_id", -1);

            if (adoptedAnimalId != -1) {
                Toast.makeText(getContext(), "Životinja je uspješno udomljena!", Toast.LENGTH_SHORT).show();
            } else if (rejectedAnimalId != -1) {
                Toast.makeText(getContext(), "Zahtjev za udomljavanje je odbijen!", Toast.LENGTH_SHORT).show();
            }
            refreshAllData();
        }
    }

    private void openFilterDialog() {
        if (getContext() == null) return;
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
        filterView.findViewById(R.id.cancel_button).setOnClickListener(v -> dialog.dismiss());
        filterView.findViewById(R.id.reset_button).setOnClickListener(v -> {
            typeSpinner.setSelection(0);
            ageSpinner.setSelection(0);
            colorSpinner.setSelection(0);
            resetFilter();
            dialog.dismiss();
        });
        dialog.show();
    }
    private void selectSpinnerValue(Spinner spinner, String value) {
        if (spinner == null || spinner.getAdapter() == null) return;
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i); return;
            }
        }
    }

    private void resetFilter() {
        Log.d(TAG, "Resetting filters.");
        if(searchBox != null) searchBox.setText("");
        lastSearchedType = "";
        refreshAllData();
    }

    private String getTypeNameById(int id) {
        if (tipLjubimcaList == null || tipLjubimcaList.isEmpty()) return "Nepoznat tip";
        for (SifrTipLjubimca tip : tipLjubimcaList) if (tip.getId() == id) return tip.getNaziv();
        return "pas";
    }

    private String getAnimalTypeName(AnimalModel animal) {
        String typeName = "Nepoznat tip";
        if (animal == null || animal.getTipLjubimca() == null) return typeName;
        try {
            if (tipLjubimcaList != null && !tipLjubimcaList.isEmpty()) {
                typeName = getTypeNameById(Integer.parseInt(animal.getTipLjubimca()));
            } else { typeName = animal.getTipLjubimca(); }
        } catch (NumberFormatException e) { typeName = animal.getTipLjubimca(); }
        return typeName;
    }

    private int getCurrentUserId() {
        if (getContext() == null || getActivity() == null) return -1;
        try {
            SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            String userIdStr = sharedViewModel.getUserId().getValue();
            if (userIdStr != null && !userIdStr.isEmpty()) return Integer.parseInt(userIdStr);
        } catch (Exception e) { Log.e(TAG, "getCurrentUserId: Error with ViewModel.", e); }
        SharedPreferences userPrefs = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = userPrefs.getString("current_user", null);
        if (userJson != null) {
            UserModel currentUserModel = new Gson().fromJson(userJson, UserModel.class);
            if (currentUserModel != null) return currentUserModel.getIdKorisnika();
        }
        return -1;
    }

    private void getTipoveLjubimaca() {
        if (!isAdded() || apiService == null) {
            stopLoadingSequence(false);
            return;
        }
        Log.d(TAG, "getTipoveLjubimaca: Fetching...");
        apiService.getAnimalTypes().enqueue(new Callback<List<SifrTipLjubimca>>() {
            @Override
            public void onResponse(Call<List<SifrTipLjubimca>> call, Response<List<SifrTipLjubimca>> response) {
                if (!isAdded() || getContext() == null) { stopLoadingSequence(false); return; }
                if (response.isSuccessful() && response.body() != null) {
                    tipLjubimcaList = response.body();
                } else {
                    tipLjubimcaList.clear();
                }
                getOdbijeneZivotinje();
            }
            @Override
            public void onFailure(Call<List<SifrTipLjubimca>> call, Throwable t) {
                if (!isAdded() || getContext() == null) { stopLoadingSequence(false); return; }
                tipLjubimcaList.clear();
                getOdbijeneZivotinje();
            }
        });
    }

    private void getOdbijeneZivotinje() {
        if (!isAdded() || apiService == null) {
            stopLoadingSequence(false);
            return;
        }
        final int currentUserId = getCurrentUserId();
        if (currentUserId == -1) {
            Log.w(TAG, "getOdbijeneZivotinje: Invalid user ID. Using empty rejected list.");
            listaOdbijenih.clear();
            loadAllAdoptedAnimals();
            return;
        }
        Log.d(TAG, "getOdbijeneZivotinje: Fetching for user ID: " + currentUserId);
        apiService.getOdbijeneZivotinje().enqueue(new Callback<List<RejectAdoptionModelRead>>() {
            @Override
            public void onResponse(Call<List<RejectAdoptionModelRead>> call, Response<List<RejectAdoptionModelRead>> response) {
                if (!isAdded() || getContext() == null) { stopLoadingSequence(false); return; }
                listaOdbijenih.clear();
                if (response.isSuccessful() && response.body() != null) {
                    for (RejectAdoptionModelRead odbijena : response.body()) {
                        if (Objects.equals(odbijena.getId_korisnika(), currentUserId) && odbijena.getId_ljubimca() != null) {
                            if (!listaOdbijenih.contains(odbijena.getId_ljubimca())) listaOdbijenih.add(odbijena.getId_ljubimca());
                        }
                    }
                }
                Log.d(TAG, "getOdbijeneZivotinje: Updated listaOdbijenih: " + listaOdbijenih);
                loadAllAdoptedAnimals();
            }
            @Override
            public void onFailure(Call<List<RejectAdoptionModelRead>> call, Throwable t) {
                if (!isAdded() || getContext() == null) { stopLoadingSequence(false); return; }
                listaOdbijenih.clear();
                loadAllAdoptedAnimals();
            }
        });
    }

    private void loadAllAdoptedAnimals() {
        if (!isAdded() || apiService == null) {
            stopLoadingSequence(false);
            return;
        }
        Log.d(TAG, "loadAllAdoptedAnimals: Fetching all animals from server...");
        apiService.getAllAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                if (!isAdded() || getContext() == null) { stopLoadingSequence(false); return; }
                if (response.isSuccessful() && response.body() != null) {
                    fetchAdoptedAnimals(response.body());
                } else {
                    Log.e(TAG, "loadAllAdoptedAnimals: Error fetching. Code: " + response.code());
                    fetchBlokiranjeZivotinje(new ArrayList<>());
                }
            }
            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                if (!isAdded() || getContext() == null) { stopLoadingSequence(false); return; }
                Log.e(TAG, "loadAllAdoptedAnimals: API call failed.", t);
                fetchBlokiranjeZivotinje(new ArrayList<>());
            }
        });
    }

    private void fetchAdoptedAnimals(List<AnimalModel> animalsFromServer) {
        if (!isAdded() || apiService == null) {
            stopLoadingSequence(false);
            return;
        }
        Log.d(TAG, "fetchAdoptedAnimals: Processing " + animalsFromServer.size() + " animals, fetching adoption diary...");
        apiService.getDnevnikUdomljavanja().enqueue(new Callback<List<UpdateDnevnikModel>>() {
            @Override
            public void onResponse(Call<List<UpdateDnevnikModel>> call, Response<List<UpdateDnevnikModel>> response) {
                if (!isAdded() || getContext() == null) { stopLoadingSequence(false); return; }
                List<AnimalModel> animalsForBlockingCheck = new ArrayList<>(animalsFromServer);
                if (response.isSuccessful() && response.body() != null) {
                    cartModelList.clear();
                    for (UpdateDnevnikModel entry : response.body()) {
                        if (entry.isUdomljen() && entry.getIme_ljubimca() != null) {
                            MyAdoptionModel temp = new MyAdoptionModel();
                            temp.setImeLjubimca(entry.getIme_ljubimca());
                            cartModelList.add(temp);
                        }
                    }
                    final List<String> adoptedNamesFromDiary = cartModelList.stream()
                            .map(MyAdoptionModel::getImeLjubimca)
                            .collect(Collectors.toList());
                    animalsForBlockingCheck.removeIf(animal -> adoptedNamesFromDiary.contains(animal.getImeLjubimca()));
                } else {
                    Log.w(TAG, "fetchAdoptedAnimals: Error fetching diary. Code: " + response.code());
                }
                fetchBlokiranjeZivotinje(animalsForBlockingCheck);
            }
            @Override
            public void onFailure(Call<List<UpdateDnevnikModel>> call, Throwable t) {
                if (!isAdded() || getContext() == null) { stopLoadingSequence(false); return; }
                Log.e(TAG, "fetchAdoptedAnimals: Diary API call failed.", t);
                fetchBlokiranjeZivotinje(animalsFromServer);
            }
        });
    }

    private void fetchBlokiranjeZivotinje(List<AnimalModel> animalsToProcess) {
        if (!isAdded()) {
            stopLoadingSequence(false);
            return;
        }
        Log.d(TAG, "fetchBlokiranjeZivotinje: Final processing of " + animalsToProcess.size() + " animals. Using listaOdbijenih: " + listaOdbijenih);
        animalModelList.clear();

        for (AnimalModel animal : animalsToProcess) {
            boolean isAdopted = animal.isUdomljen();
            boolean isPendingRequest = animal.isZahtjevUdomljavanja();

            if (isAdopted || isPendingRequest) {
                continue;
            }
            boolean isBlockedDueToPriorRejection = listaOdbijenih.contains(animal.getIdLjubimca());
            String tipLjubimcaNaziv = getAnimalTypeName(animal);

            animalModelList.add(new IsBlockedAnimalModel(
                    animal.getIdLjubimca(), animal.getIdUdomitelja(), animal.getImeLjubimca(),
                    tipLjubimcaNaziv, animal.getOpisLjubimca(), false, false,
                    animal.getDatum(), animal.getVrijeme(), animal.getImgUrl(),
                    animal.StanjeZivotinje(), isBlockedDueToPriorRejection,
                    animal.getDob(), animal.getBoja()
            ));
        }
        if (animalsAdapter != null) animalsAdapter.notifyDataSetChanged();
        Log.d(TAG, "fetchBlokiranjeZivotinje: List populated. Final size: " + animalModelList.size());
        stopLoadingSequence(true);
    }

    private void applyFilters(String typeName, String age, String color) {
        Integer minDob = null;
        Integer maxDob = null;
        if (!age.equals("Sve")) {
            switch (age) {
                case "0 do 1 godina": minDob = 0; maxDob = 1; break;
                case "2 do 5 godina": minDob = 2; maxDob = 5; break;
                case "5+ godina": minDob = 6; maxDob = 20; break;
            }
        }
        Log.d(TAG, "applyFilters: Type=" + typeName + ", Age=" + age + ", Color=" + color);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (emptyListMessageTextView != null) emptyListMessageTextView.setVisibility(View.GONE);


        Set<Integer> blockedAnimalIds = new HashSet<>();
        for (IsBlockedAnimalModel animal : animalModelList) {
            if (animal.isBlocked()) blockedAnimalIds.add(animal.getIdLjubimca());
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
                if (!isAdded() || getContext() == null) { if (progressBar != null) progressBar.setVisibility(View.GONE); return;}
                boolean success = false;
                if (response.isSuccessful() && response.body() != null) {
                    success = true;
                    animalModelList.clear();
                    List<AnimalModel> filteredFromServer = response.body();
                    for(AnimalModel animal : filteredFromServer) {
                        boolean isAdopted = animal.isUdomljen();
                        boolean isPendingRequest = animal.isZahtjevUdomljavanja();
                        if (isAdopted || isPendingRequest) continue;

                        boolean wasBlocked = blockedAnimalIds.contains(animal.getIdLjubimca());
                        boolean isActuallyBlocked = wasBlocked || listaOdbijenih.contains(animal.getIdLjubimca());

                        animalModelList.add(new IsBlockedAnimalModel(
                                animal.getIdLjubimca(), animal.getIdUdomitelja(), animal.getImeLjubimca(),
                                getTypeNameById(Integer.parseInt(animal.getTipLjubimca())),
                                animal.getOpisLjubimca(), false, false,
                                animal.getDatum(), animal.getVrijeme(), animal.getImgUrl(),
                                animal.StanjeZivotinje(), isActuallyBlocked,
                                animal.getDob(), animal.getBoja()
                        ));
                    }
                } else {
                    animalModelList.clear();
                }
                if (animalsAdapter != null) animalsAdapter.notifyDataSetChanged();

                if (progressBar != null) progressBar.setVisibility(View.GONE);
                updateUIBasedOnDataState(animalModelList.isEmpty(), success ? "Nema životinja za odabrane filtere." : "Greška pri filtriranju.");
            }
            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                if (!isAdded() || getContext() == null) { if (progressBar != null) progressBar.setVisibility(View.GONE); return;}
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                animalModelList.clear();
                if (animalsAdapter != null) animalsAdapter.notifyDataSetChanged();
                updateUIBasedOnDataState(true, "Greška pri filtriranju.");
            }
        });
    }

    private void searchAnimalsByType(String type) {
        Integer typeId = null;
        if (type == null || type.isEmpty()) {
            lastSearchedType = "";
            refreshAllData();
            return;
        }
        lastSearchedType = type;
        if (type.equalsIgnoreCase("pas")) typeId = 1;
        else if (type.equalsIgnoreCase("macka") || type.equalsIgnoreCase("mačka")) typeId = 2;
        else { Toast.makeText(getContext(), "Nepoznat tip životinje", Toast.LENGTH_SHORT).show(); return; }

        Log.d(TAG, "searchAnimalsByType: TypeID=" + typeId);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (emptyListMessageTextView != null) emptyListMessageTextView.setVisibility(View.GONE);


        Set<Integer> blockedAnimalIds = new HashSet<>();
        for (IsBlockedAnimalModel animal : animalModelList) {
            if (animal.isBlocked()) blockedAnimalIds.add(animal.getIdLjubimca());
        }
        String finalType = type;
        apiService.getAnimalsByType(typeId).enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                if (!isAdded() || getContext() == null) { if (progressBar != null) progressBar.setVisibility(View.GONE); return;}
                boolean success = false;
                if (response.isSuccessful() && response.body() != null) {
                    success = true;
                    animalModelList.clear();
                    List<AnimalModel> searchedFromServer = response.body();
                    for(AnimalModel animal : searchedFromServer){
                        boolean isAdopted = animal.isUdomljen();
                        boolean isPendingRequest = animal.isZahtjevUdomljavanja();
                        if(isAdopted || isPendingRequest) continue;

                        boolean wasBlocked = blockedAnimalIds.contains(animal.getIdLjubimca());
                        boolean isActuallyBlocked = wasBlocked || listaOdbijenih.contains(animal.getIdLjubimca());

                        animalModelList.add(new IsBlockedAnimalModel(
                                animal.getIdLjubimca(), animal.getIdUdomitelja(), animal.getImeLjubimca(),
                                finalType, animal.getOpisLjubimca(),
                                false, false,
                                animal.getDatum(), animal.getVrijeme(), animal.getImgUrl(),
                                animal.StanjeZivotinje(), isActuallyBlocked,
                                animal.getDob(), animal.getBoja()
                        ));
                    }
                } else {
                    animalModelList.clear();
                }
                if (animalsAdapter != null) animalsAdapter.notifyDataSetChanged();
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                updateUIBasedOnDataState(animalModelList.isEmpty(), success ? "Nema životinja za traženi tip." : "Greška pri pretrazi.");
            }
            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                if (!isAdded() || getContext() == null) { if (progressBar != null) progressBar.setVisibility(View.GONE); return;}
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                animalModelList.clear();
                if (animalsAdapter != null) animalsAdapter.notifyDataSetChanged();
                updateUIBasedOnDataState(true, "Greška pri pretrazi.");
            }
        });
    }

    private void updateUIBasedOnDataState(boolean showEmptyMessage, String message) {
        if (!isAdded() || getView() == null) return;

        RecyclerView currentRecyclerView = getView().findViewById(R.id.recycler_view);
        TextView currentEmptyListMessageTextView = getView().findViewById(R.id.empty_list_message_textview);

        if (currentRecyclerView == null) {
            Log.e(TAG, "updateUIBasedOnDataState: RecyclerView is null. UI update skipped.");
            return;
        }

        if (showEmptyMessage) {
            currentRecyclerView.setVisibility(View.GONE);
            if (currentEmptyListMessageTextView != null) {
                currentEmptyListMessageTextView.setText(message);
                currentEmptyListMessageTextView.setVisibility(View.VISIBLE);
            } else {
                Log.w(TAG, "updateUIBasedOnDataState: emptyListMessageTextView is not found in layout, cannot show message: " + message);
            }
        } else {
            currentRecyclerView.setVisibility(View.VISIBLE);
            if (currentEmptyListMessageTextView != null) {
                currentEmptyListMessageTextView.setVisibility(View.GONE);
            }
        }
    }

    private void loadAllAnimals() {
        refreshAllData();
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
}