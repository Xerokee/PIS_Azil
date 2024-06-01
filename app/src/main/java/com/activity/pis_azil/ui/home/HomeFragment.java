package com.activity.pis_azil.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.ApiClient;
import com.activity.pis_azil.ApiService;
import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.HomeAdapter;
import com.activity.pis_azil.adapters.PopularAdapter;
import com.activity.pis_azil.adapters.RecommendedAdapter;
import com.activity.pis_azil.adapters.ViewAllAdapter;
import com.activity.pis_azil.models.HomeCategory;
import com.activity.pis_azil.models.PopularModel;
import com.activity.pis_azil.models.RecommendedModel;
import com.activity.pis_azil.models.ViewAllModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    ScrollView scrollView;
    ProgressBar progressBar;
    RecyclerView popularRec, homeCatRec, recommendedRec;
    ApiService apiService;

    List<PopularModel> popularModelList;
    PopularAdapter popularAdapters;

    EditText searchBox;
    private List<ViewAllModel> viewAllModelList;
    private RecyclerView recyclerViewSearch;
    private ViewAllAdapter viewAllAdapter;

    List<HomeCategory> categoryList;
    HomeAdapter homeAdapter;

    List<RecommendedModel> recommendedModelList;
    RecommendedAdapter recommendedAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        apiService = ApiClient.getClient().create(ApiService.class);

        popularRec = root.findViewById(R.id.pop_rec);
        homeCatRec = root.findViewById(R.id.explore_rec_);
        recommendedRec = root.findViewById(R.id.recommended_rec);
        scrollView = root.findViewById(R.id.scroll_view);
        progressBar = root.findViewById(R.id.progressbar);

        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);

        popularRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        popularModelList = new ArrayList<>();
        popularAdapters = new PopularAdapter(getActivity(), popularModelList);
        popularRec.setAdapter(popularAdapters);

        fetchPopularAnimals();

        homeCatRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        categoryList = new ArrayList<>();
        homeAdapter = new HomeAdapter(getActivity(), categoryList);
        homeCatRec.setAdapter(homeAdapter);

        fetchHomeCategories();

        recommendedRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        recommendedModelList = new ArrayList<>();
        recommendedAdapter = new RecommendedAdapter(getActivity(), recommendedModelList);
        recommendedRec.setAdapter(recommendedAdapter);

        fetchRecommendedAnimals();

        recyclerViewSearch = root.findViewById(R.id.search_rec);
        searchBox = root.findViewById(R.id.search_box);
        viewAllModelList = new ArrayList<>();
        viewAllAdapter = new ViewAllAdapter(getContext(), viewAllModelList);
        recyclerViewSearch.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSearch.setAdapter(viewAllAdapter);
        recyclerViewSearch.setHasFixedSize(true);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    viewAllModelList.clear();
                    viewAllAdapter.notifyDataSetChanged();
                } else {
                    searchAnimal(s.toString());
                }
            }
        });

        return root;
    }

    private void fetchPopularAnimals() {
        apiService.getPopularAnimals().enqueue(new Callback<List<PopularModel>>() {
            @Override
            public void onResponse(Call<List<PopularModel>> call, Response<List<PopularModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    popularModelList.addAll(response.body());
                    popularAdapters.notifyDataSetChanged();

                    progressBar.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getActivity(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PopularModel>> call, Throwable t) {
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchHomeCategories() {
        apiService.getHomeCategories().enqueue(new Callback<List<HomeCategory>>() {
            @Override
            public void onResponse(Call<List<HomeCategory>> call, Response<List<HomeCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList.addAll(response.body());
                    homeAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<HomeCategory>> call, Throwable t) {
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecommendedAnimals() {
        apiService.getRecommendedAnimals().enqueue(new Callback<List<RecommendedModel>>() {
            @Override
            public void onResponse(Call<List<RecommendedModel>> call, Response<List<RecommendedModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recommendedModelList.addAll(response.body());
                    recommendedAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RecommendedModel>> call, Throwable t) {
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchAnimal(String keyword) {
        apiService.searchAnimals(keyword).enqueue(new Callback<List<ViewAllModel>>() {
            @Override
            public void onResponse(Call<List<ViewAllModel>> call, Response<List<ViewAllModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    viewAllModelList.clear();
                    viewAllModelList.addAll(response.body());
                    viewAllAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ViewAllModel>> call, Throwable t) {
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
