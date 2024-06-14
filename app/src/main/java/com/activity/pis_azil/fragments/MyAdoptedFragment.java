package com.activity.pis_azil.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.MyAdoptedAnimalsAdapter;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAdoptedFragment extends Fragment {

    private ApiService apiService;
    private MyAdoptedAnimalsAdapter adapter;
    private RecyclerView recyclerView;
    private List<AnimalModel> adoptedAnimalsList;
    private TextView newAnimalsTextView;
    private ImageView newAnimalsImageView;

    public MyAdoptedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_adopted, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);

        recyclerView = root.findViewById(R.id.adopted_animals_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adoptedAnimalsList = new ArrayList<>();
        adapter = new MyAdoptedAnimalsAdapter(getActivity(), adoptedAnimalsList);
        recyclerView.setAdapter(adapter);

        newAnimalsTextView = root.findViewById(R.id.new_animals_textview);
        newAnimalsImageView = root.findViewById(R.id.new_animals_img);

        fetchAdoptedAnimals();

        return root;
    }

    private void fetchAdoptedAnimals() {
        apiService.getAdoptedAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adoptedAnimalsList.clear();
                    adoptedAnimalsList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                } else {
                    Toast.makeText(getActivity(), "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                Log.e("MyAdoptedFragment", "Error fetching documents: ", t);
                Toast.makeText(getActivity(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (adoptedAnimalsList.isEmpty()) {
            newAnimalsTextView.setVisibility(View.VISIBLE);
            newAnimalsImageView.setVisibility(View.VISIBLE);
        } else {
            newAnimalsTextView.setVisibility(View.GONE);
            newAnimalsImageView.setVisibility(View.GONE);
        }
    }
}
