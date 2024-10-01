package com.activity.pis_azil.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.adapters.MyAdoptionAdapter;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.network.DataRefreshListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimalsFragment extends Fragment implements DataRefreshListener {
    @Override
    public void refreshData() {
        fetchAdoptedAnimals();
    }
    private static final String TAG = "AnimalsFragment";

    ApiService apiService;
    TextView overTotalAmount;
    RecyclerView recyclerView;
    MyAdoptionAdapter cartAdapter;
    List<MyAdoptionModel> cartModelList;
    TextView emptyStateTextView;

    public AnimalsFragment() {
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
        cartAdapter = new MyAdoptionAdapter(requireContext(), cartModelList, this); // Proslijedi listener
        recyclerView.setAdapter(cartAdapter);

        fetchAdoptedAnimals();

        return root;
    }

    private void fetchAdoptedAnimals() {
        Log.d(TAG, "Fetching adopted animals");

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);
        final UserModel currentUser;

        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, UserModel.class);
            Log.d(TAG, "Nesto" + userJson);
        } else {
            Log.e(TAG, "Usli u else");
            userJson = "{\"admin\":true,\"email\":\"matija.margeta@vuv.hr\",\"id_korisnika\":1,\"ime\":\"Matija Margeta\",\"lozinka\":\"Matija123\",\"profileImg\":\"content://media/external/images/media/1000011340\"}";
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, UserModel.class);
            Log.d(TAG, "Nesto" + userJson);

            return;
        }

        apiService.getDnevnikUdomljavanja().enqueue(new Callback<List<UpdateDnevnikModel>>() {
            @Override
            public void onResponse(Call<List<UpdateDnevnikModel>> call, Response<List<UpdateDnevnikModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Linija 89: " + response.body().size());
                    cartModelList.clear();
                    List<UpdateDnevnikModel> allAdoptions = response.body();
                    for (UpdateDnevnikModel animal : allAdoptions) {
                        // Provjera je li korisnik admin ili prikazivanje samo svojih zahtjeva
                        if (currentUser.isAdmin() || animal.getId_korisnika() == currentUser.getIdKorisnika()) {
                            if (animal.isUdomljen()) {
                                continue;
                            }

                            Log.d(TAG, "Animal fetched: " + animal.toString());
                            MyAdoptionModel adoption = new MyAdoptionModel();
                            adoption.setIdLjubimca(animal.getId_ljubimca());
                            adoption.setImeLjubimca(animal.getIme_ljubimca() != null ? animal.getIme_ljubimca() : "N/A");
                            adoption.setTipLjubimca(animal.getTip_ljubimca() != null ? animal.getTip_ljubimca() : "N/A");
                            adoption.setDatum(animal.getDatum());
                            adoption.setVrijeme(animal.getVrijeme());
                            adoption.setImgUrl(animal.getImgUrl());
                            adoption.setStanjeZivotinje(animal.isStanje_zivotinje());
                            adoption.setIdKorisnika(animal.getId_korisnika()); // Dodavanje korisnika iz API odgovora
                            cartModelList.add(adoption);
                        }
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
                }
            }

            @Override
            public void onFailure(Call<List<UpdateDnevnikModel>> call, Throwable t) {
                Log.e(TAG, "Error fetching adopted animals: ", t);
                Toast.makeText(getActivity(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
