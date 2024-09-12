package com.activity.pis_azil.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.RejectAdoptionModel;
import com.activity.pis_azil.models.RejectAdoptionModelRead;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RejectedAnimalsFragment extends Fragment {

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
        View view = inflater.inflate(R.layout.fragment_rejected_animals_list, container, false);

        // Prepoznaj view kao RecyclerView
        if (view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), mColumnCount));
            }

            // Postavi prazan adapter dok čekamo podatke
            adapter = new MyItemRecyclerViewAdapter(List.of());
            recyclerView.setAdapter(adapter);

            // Dohvati podatke putem API-ja
            Call<List<RejectAdoptionModelRead>> call = apiService.getOdbijeneZivotinje(); // Ovisno o API endpointu
            call.enqueue(new Callback<List<RejectAdoptionModelRead>>() {
                @Override
                public void onResponse(Call<List<RejectAdoptionModelRead>> call, Response<List<RejectAdoptionModelRead>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Ažuriraj adapter s novim podacima
                        adapter = new MyItemRecyclerViewAdapter(response.body());
                        recyclerView.setAdapter(adapter);
                    }
                }

                @Override
                public void onFailure(Call<List<RejectAdoptionModelRead>> call, Throwable t) {
                    // Obradi grešku
                }
            });
        }

        return view;
    }
}
