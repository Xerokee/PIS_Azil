package com.activity.pis_azil.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.ImagePagerAdapter;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.GalleryImageModel;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class GalerijaFragment extends Fragment {

    AnimalModel detailedAnimal;
    ApiService apiService;


    public GalerijaFragment() {
        // Required empty public constructor
    }

    public GalerijaFragment(AnimalModel da) {
        detailedAnimal=da;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_galerija, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);


        List<String> galleryUrls = new ArrayList<>();
        for (GalleryImageModel galleryImage : detailedAnimal.getGalerijaZivotinja()) {
            galleryUrls.add(galleryImage.getImgUrl());
            Log.d("GalerijaFragment", "Image URL: " + galleryImage.getImgUrl());
        }

        //Log.d(TAG, "Broj slika u galeriji: " + galleryUrls.size());
        if (!galleryUrls.isEmpty()) {
            Log.i("broj slika", String.valueOf(galleryUrls.size()));
            RecyclerView recyclerView = view.findViewById(R.id.recyclerView);


            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);


            ImagePagerAdapter adapter = new ImagePagerAdapter(getContext(), galleryUrls);
            recyclerView.setAdapter(adapter);
        } else {
            //Log.d(TAG, "Nema dostupnih slika u galeriji za ovog ljubimca.");
        }


        return view;
    }
}