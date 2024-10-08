package com.activity.pis_azil.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.RequestListAdapter;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestListFragment extends Fragment {

    private RecyclerView recyclerView;
    private RequestListAdapter adapter;
    private List<AnimalModel> requestList, filteredRequestList;
    private ApiService apiService;
    private Spinner filterRequestSpinner;
    private TextView emptyStateTextView;

    public RequestListFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_request_list, container, false);

        recyclerView = root.findViewById(R.id.recycler_view_request_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        filterRequestSpinner = root.findViewById(R.id.filter_request_spinner);
        emptyStateTextView = root.findViewById(R.id.empty_state_text_view);

        requestList = new ArrayList<>();
        filteredRequestList = new ArrayList<>();
        adapter = new RequestListAdapter(getContext(), filteredRequestList);
        recyclerView.setAdapter(adapter);

        setupFilterSpinner();
        fetchRequestList();

        return root;
    }

    private void setupFilterSpinner() {
        // Postavi adapter za Spinner za filtriranje prema tipu životinje
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.animal_types2, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterRequestSpinner.setAdapter(spinnerAdapter);

        // Postavi listener za promjenu odabira filtera
        filterRequestSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ne radi ništa kada ništa nije odabrano
            }
        });
    }

    private void fetchRequestList() {
        apiService = ApiClient.getClient().create(ApiService.class);

        // API poziv za dohvaćanje liste zahtjeva za životinje
        apiService.getAllAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    requestList.clear();

                    // Filtriraj samo životinje s otvorenim zahtjevom (u tijeku)
                    for (AnimalModel animal : response.body()) {
                        if (animal.isZahtjevUdomljavanja()) {
                            requestList.add(animal);
                        }
                    }

                    // Primijeni filter na trenutno dohvaćene zahtjeve
                    applyFilter();

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

    private void applyFilter() {
        String selectedType = filterRequestSpinner.getSelectedItem().toString();

        filteredRequestList.clear();

        // Ako je odabrano "Svi", prikazujemo sve zahtjeve
        if (selectedType.equals("Svi")) {
            filteredRequestList.addAll(requestList);
        } else {
            // Inače filtriramo po tipu životinje
            for (AnimalModel animal : requestList) {
                if (animal.getTipLjubimca().equalsIgnoreCase(selectedType)) {
                    filteredRequestList.add(animal);
                }
            }
        }

        // Prikaz praznog stanja ako nema zahtjeva za prikaz
        if (filteredRequestList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateTextView.setVisibility(View.GONE);
        }

        // Ažuriraj prikaz pomoću adaptera
        adapter.notifyDataSetChanged();
    }
}
