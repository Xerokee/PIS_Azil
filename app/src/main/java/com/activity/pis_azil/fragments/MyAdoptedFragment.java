package com.activity.pis_azil.fragments;

import android.app.AlertDialog;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
        root.findViewById(R.id.filter_button).setOnClickListener(v -> showFilterDialog());
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

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filters2, null);

        Spinner typeFilter = dialogView.findViewById(R.id.spinner_type);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button confirmButton = dialogView.findViewById(R.id.confirm_button);
        Button resetButton = dialogView.findViewById(R.id.reset_button);

        // Popuni Spinner sa podacima iz resursa strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.animal_types2, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeFilter.setAdapter(adapter);

        builder.setView(dialogView)
                .setTitle("Filtriraj životinje")
                .setCancelable(false);  // Disable outside dialog dismiss

        AlertDialog dialog = builder.create();

        // Odustani button (dismiss the dialog without doing anything)
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Filtriraj button (apply selected filter)
        confirmButton.setOnClickListener(v -> {
            String type = typeFilter.getSelectedItem().toString();
            applyFilters(type);
            dialog.dismiss();  // Dismiss the dialog after applying the filter
        });

        // Resetiraj button (reset filter to "Svi" and show all animals)
        resetButton.setOnClickListener(v -> {
            typeFilter.setSelection(0);  // Set the spinner to "Svi" (first item)
            applyFilters("Svi");         // Apply "Svi" filter to show all animals
            dialog.dismiss();            // Dismiss the dialog after resetting
        });

        dialog.show();
    }

    private void applyFilters(String type) {
        filteredAdoptedAnimalsList.clear();

        // Filtriraj životinje prema odabranom tipu
        if (type.equals("Svi")) {
            filteredAdoptedAnimalsList.addAll(adoptedAnimalsList); // Show all animals
        } else {
            for (UpdateDnevnikModel animal : adoptedAnimalsList) {
                if (animal.getTip_ljubimca().equalsIgnoreCase(type)) {
                    filteredAdoptedAnimalsList.add(animal); // Filter by selected type
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void fetchUsers() {
        apiService.getAllUsers().enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (UserModel user : response.body()) {
                        // Spremi ID korisnika, ime i prezime u mapu kao jedan formatirani string
                        String fullName = user.getIme() + " " + user.getPrezime();
                        userMap.put(user.getIdKorisnika(), fullName); // Dodaj ime i prezime u mapu
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
        filteredAdoptedAnimalsList.clear();  // Clear the previous filter results
        if (query.isEmpty()) {
            filteredAdoptedAnimalsList.addAll(adoptedAnimalsList);
        } else {
            for (UpdateDnevnikModel animal : adoptedAnimalsList) {
                boolean matches = false;

                if (animal.getImeUdomitelja() != null && animal.getImeUdomitelja().toLowerCase().contains(query.toLowerCase())) {
                    matches = true;
                }
                if (animal.getPrezimeUdomitelja() != null && animal.getPrezimeUdomitelja().toLowerCase().contains(query.toLowerCase())) {
                    matches = true;
                }

                if (matches && !filteredAdoptedAnimalsList.contains(animal)) {
                    filteredAdoptedAnimalsList.add(animal);
                }
            }
        }

        // Notify adapter that the data has changed
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
                        String fullName = userMap.getOrDefault(animal.getId_korisnika(), "Nepoznato");
                        animal.setImeUdomitelja(fullName); // Postavljamo cijelo ime (ime + prezime)
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
