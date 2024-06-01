package com.activity.pis_azil;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.activity.pis_azil.ApiClient;
import com.activity.pis_azil.ApiService;
import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.MyAdoptedAnimalsAdapter;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.UserModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAdoptedFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyAdoptedAnimalsAdapter adapter;
    private List<AnimalModel> adoptedAnimalsList;
    private TextView newAnimalsTextView;
    private ImageView newAnimalsImageView;
    private boolean isSearchSuccessful = false;
    ApiService apiService;

    public MyAdoptedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_adopted, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);

        newAnimalsTextView = root.findViewById(R.id.new_animals_textview);
        newAnimalsImageView = root.findViewById(R.id.new_animals_img);

        recyclerView = root.findViewById(R.id.adopted_animals_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adoptedAnimalsList = new ArrayList<>();
        adapter = new MyAdoptedAnimalsAdapter(getContext(), adoptedAnimalsList);
        recyclerView.setAdapter(adapter);

        fetchAdoptedAnimals();
        EditText searchBox = root.findViewById(R.id.search_box);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    fetchAdoptedAnimals();
                    isSearchSuccessful = false;
                } else {
                    searchAdoptersByName(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
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
                    updateUIBasedOnSearchResults();

                    if (adoptedAnimalsList.isEmpty()) {
                        newAnimalsTextView.setVisibility(View.VISIBLE);
                        newAnimalsImageView.setVisibility(View.VISIBLE);
                    } else {
                        newAnimalsTextView.setVisibility(View.GONE);
                        newAnimalsImageView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                Log.e("MyAdoptedFragment", "Error fetching adopted animals", t);
            }
        });
    }

    private void searchAdoptersByName(String searchText) {
        if (searchText.trim().isEmpty()) {
            fetchAdoptedAnimals();
            return;
        }

        String startText = searchText.toLowerCase();
        String endText = startText + '\uf8ff';

        apiService.searchUsersByName(startText, endText).enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> adopterIds = new ArrayList<>();
                    for (UserModel userModel : response.body()) {
                        adopterIds.add(String.valueOf(userModel.getIdKorisnika()));
                    }
                    if (!adopterIds.isEmpty()) {
                        fetchAnimalsForAdopters(adopterIds);
                        isSearchSuccessful = true;
                    } else {
                        adoptedAnimalsList.clear();
                        adapter.notifyDataSetChanged();
                        isSearchSuccessful = false;
                        updateUIBasedOnSearchResults();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Log.e("MyAdoptedFragment", "Error fetching adopters by name", t);
                adoptedAnimalsList.clear();
                adapter.notifyDataSetChanged();
                updateUIBasedOnSearchResults();
            }
        });
    }

    private void fetchAnimalsForAdopters(List<String> adopterIds) {
        if (!adopterIds.isEmpty()) {
            apiService.getAnimalsForAdopters(adopterIds).enqueue(new Callback<List<AnimalModel>>() {
                @Override
                public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adoptedAnimalsList.clear();
                        adoptedAnimalsList.addAll(response.body());
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                    Log.e("MyAdoptedFragment", "Error getting animals for adopters", t);
                }
            });
        }
    }

    private void updateUIBasedOnSearchResults() {
        if (adoptedAnimalsList.isEmpty() && isSearchSuccessful) {
            newAnimalsTextView.setVisibility(View.VISIBLE);
            newAnimalsImageView.setVisibility(View.VISIBLE);
            newAnimalsTextView.setText("Nema pronađenih životinja za odabranog udomitelja.");
        } else {
            newAnimalsTextView.setVisibility(View.GONE);
            newAnimalsImageView.setVisibility(View.GONE);
        }
    }
}
