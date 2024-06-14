package com.activity.pis_azil.fragments;

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
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.adapters.MyAdoptionAdapter;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.network.ApiService;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAnimalsFragment extends Fragment {

    ApiService apiService;
    TextView overTotalAmount;
    RecyclerView recyclerView;
    MyAdoptionAdapter cartAdapter;
    List<MyAdoptionModel> cartModelList;

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

        cartModelList = new ArrayList<>();
        cartAdapter = new MyAdoptionAdapter(getActivity(), cartModelList);
        recyclerView.setAdapter(cartAdapter);

        fetchAdoptedAnimals();

        return root;
    }

    private void fetchAdoptedAnimals() {
        apiService.getAdoptedAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cartModelList.clear();
                    for (AnimalModel animal : response.body()) {
                        MyAdoptionModel adoption = new MyAdoptionModel();
                        adoption.setIdLjubimca(animal.getIdLjubimca());
                        adoption.setImeLjubimca(animal.getImeLjubimca());
                        adoption.setTipLjubimca(animal.getTipLjubimca());
                        adoption.setDatum(animal.getDatum());
                        adoption.setOpisLjubimca(animal.getOpisLjubimca());
                        adoption.setImgUrl(animal.getImgUrl());
                        cartModelList.add(adoption);
                    }
                    cartAdapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getActivity(), "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                Log.e("MyAnimalsFragment", "Error fetching documents: ", t);
                Toast.makeText(getActivity(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
