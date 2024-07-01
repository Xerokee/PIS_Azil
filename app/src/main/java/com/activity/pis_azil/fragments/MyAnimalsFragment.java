package com.activity.pis_azil.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log; // Dodano za logiranje
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.activity.pis_azil.R;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.adapters.MyAdoptionAdapter;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.network.DataRefreshListener;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAnimalsFragment extends Fragment implements DataRefreshListener {
    private static final String TAG = "MyAnimalsFragment"; // Dodano za logiranje

    ApiService apiService;
    TextView overTotalAmount;
    RecyclerView recyclerView;
    MyAdoptionAdapter cartAdapter;
    List<MyAdoptionModel> cartModelList;
    TextView emptyStateTextView; // Dodano za praznu listu

    public MyAnimalsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_adoptions, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);

        recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        overTotalAmount = root.findViewById(R.id.textView7);
        emptyStateTextView = root.findViewById(R.id.empty_state_text_view); // Inicijalizacija praznog prikaza

        cartModelList = new ArrayList<>();
        cartAdapter = new MyAdoptionAdapter(getActivity(), cartModelList, this); // Proslijedi listener
        recyclerView.setAdapter(cartAdapter);

        fetchAdoptedAnimals();

        return root;
    }

    @Override
    public void refreshData() {
        fetchAdoptedAnimals();
    }

    private void fetchAdoptedAnimals() {
        Log.d(TAG, "Fetching adopted animals");

        apiService.getAdoptedAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully fetched adopted animals, size: " + response.body().size());
                    cartModelList.clear();
                    for (AnimalModel animal : response.body()) {
                        Log.d(TAG, "Animal fetched: " + animal.toString());
                        MyAdoptionModel adoption = new MyAdoptionModel();
                        adoption.setIdLjubimca(animal.getIdLjubimca());
                        adoption.setImeLjubimca(animal.getImeLjubimca() != null ? animal.getImeLjubimca() : "N/A");
                        adoption.setTipLjubimca(animal.getTipLjubimca() != null ? animal.getTipLjubimca() : "N/A");
                        adoption.setDatum(animal.getDatum());
                        adoption.setVrijeme(animal.getVrijeme());
                        adoption.setImgUrl(animal.getImgUrl());
                        adoption.setStanjeZivotinje(animal.StanjeZivotinje());
                        adoption.setIdKorisnika(animal.getIdUdomitelja()); // Dodavanje korisnika iz API odgovora
                        cartModelList.add(adoption);
                    }

                    // Provjera i prikazivanje prazne liste ili podataka
                    if (cartModelList.isEmpty()) {
                        Log.d(TAG, "No animals to display, showing empty state message");
                        recyclerView.setVisibility(View.GONE);
                        emptyStateTextView.setVisibility(View.VISIBLE); // Prikaz poruke kada je lista prazna
                    } else {
                        Log.d(TAG, "Displaying fetched animals in RecyclerView");
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyStateTextView.setVisibility(View.GONE);
                    }

                    cartAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Failed to fetch adopted animals: " + response.message());
                    Toast.makeText(getActivity(), "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                Log.e(TAG, "Error fetching adopted animals: ", t);
                Toast.makeText(getActivity(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
