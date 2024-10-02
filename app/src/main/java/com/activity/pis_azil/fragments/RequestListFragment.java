package com.activity.pis_azil.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private List<AnimalModel> requestList;
    private ApiService apiService;

    public RequestListFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_request_list, container, false);

        recyclerView = root.findViewById(R.id.recycler_view_request_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        requestList = new ArrayList<>();
        adapter = new RequestListAdapter(getContext(), requestList);
        recyclerView.setAdapter(adapter);

        fetchRequestList();

        return root;
    }

    private void fetchRequestList() {
        apiService = ApiClient.getClient().create(ApiService.class);

        // API call to get the list of requested animals
        apiService.getAllAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AnimalModel> allAnimals = response.body();
                    requestList.clear();

                    // Filter only animals with a pending request (in adoption process)
                    for (AnimalModel animal : allAnimals) {
                        if (animal.isZahtjevUdomljavanja()) {
                            requestList.add(animal);
                        }
                    }

                    adapter.notifyDataSetChanged();
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
}
