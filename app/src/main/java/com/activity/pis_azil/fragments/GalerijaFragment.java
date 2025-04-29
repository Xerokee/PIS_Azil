package com.activity.pis_azil.fragments;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.ImagePagerAdapter;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.GalleryImageModel;

import java.util.ArrayList;
import java.util.List;

public class GalerijaFragment extends Fragment {

    AnimalModel detailedAnimal;

    public GalerijaFragment() {
        // Required empty public constructor
    }

    public GalerijaFragment(AnimalModel da) {
        detailedAnimal = da;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_galerija, container, false);

        List<String> galleryUrls = new ArrayList<>();
        for (GalleryImageModel galleryImage : detailedAnimal.getGalerijaZivotinja()) {
            galleryUrls.add(galleryImage.getImgUrl());
        }

        if (!galleryUrls.isEmpty()) {
            RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

            // Postavljanje GridLayoutManager sa 3 kolone
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);

            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return (position >= 6) ? 2 : 1;
                }
            });

            recyclerView.setLayoutManager(layoutManager);

            // Dodavanje custom itemDecoration za razmak
            recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    super.getItemOffsets(outRect, view, parent, state);

                    int position = parent.getChildAdapterPosition(view);

                    // Razmak između slika u horizontalnom pravcu
                    if (position % 3 != 2) {
                        outRect.right = 4;  // Razmak između slika u horizontalnom pravcu
                    }

                    // Razmak između slika u vertikalnom pravcu
                    if (position < 3) {
                        outRect.top = 4;
                    }
                    outRect.bottom = 2;  // Razmak između redova
                }
            });

            // Postavljanje adaptera
            ImagePagerAdapter adapter = new ImagePagerAdapter(getContext(), galleryUrls);
            recyclerView.setAdapter(adapter);
        }

        return view;
    }
}
