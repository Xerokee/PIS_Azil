package com.activity.pis_azil.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.RejectAdoptionModelRead;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.network.DataRefreshListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RejectedAnimalsFragment extends Fragment implements DataRefreshListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private MyItemRecyclerViewAdapter adapter;
    ApiService apiService = ApiClient.getClient().create(ApiService.class);

    public RejectedAnimalsFragment() {
    }

    public static RejectedAnimalsFragment newInstance(int columnCount) {
        RejectedAnimalsFragment fragment = new RejectedAnimalsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflata layout za ovaj fragment
        View view = inflater.inflate(R.layout.fragment_rejected_animals_list, container, false);

        // Pronađi RecyclerView unutar layouta
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView); // Provjeri da u XML-u imaš točan ID

        // Postavi LayoutManager ovisno o broju stupaca
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), mColumnCount));
        }

        // Postavi prazan adapter dok čekamo podatke
        adapter = new MyItemRecyclerViewAdapter(new ArrayList<>(), view.getContext(), this); // Koristite this ako je fragment implementirao DataRefreshListener
        recyclerView.setAdapter(adapter);

        // Dohvati podatke putem API-ja
        Call<List<RejectAdoptionModelRead>> call = apiService.getOdbijeneZivotinje();
        call.enqueue(new Callback<List<RejectAdoptionModelRead>>() {
            @Override
            public void onResponse(Call<List<RejectAdoptionModelRead>> call, Response<List<RejectAdoptionModelRead>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SharedPreferences prefs = view.getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    String userJson = prefs.getString("current_user", null);
                    if (userJson != null) {
                        Gson gson = new Gson();
                        UserModel currentUser = gson.fromJson(userJson, UserModel.class);
                        if (currentUser != null) {
                            if (currentUser.isAdmin()) {
                                adapter.updateData(response.body());
                            } else {
                                adapter.updateData(response.body().stream().filter(
                                        item -> item.getId_korisnika() == currentUser.getIdKorisnika()
                                ).collect(Collectors.toList()));
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RejectAdoptionModelRead>> call, Throwable t) {
            }
        });

        return view;
    }

    @Override
    public void refreshData() {
        // Implementacija metode za osvježavanje podataka ako je potrebna
    }
}
