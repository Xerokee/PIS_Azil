package com.activity.pis_azil.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;  // Import Fragment instead of AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.SlikeAdapter;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.SlikaModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.network.HttpRequestResponseList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimalGalleryFragment extends Fragment {  // Extend Fragment instead of AppCompatActivity
    RecyclerView rvSlike;
    SlikeAdapter slikeAdapter;
    List<SlikaModel> listaSlika = new ArrayList<>();
    private static final int SELECT_IMAGE_CODE = 1;
    AnimalModel detailedImage;
    ApiService apiService;
    int animalId;

    public AnimalGalleryFragment() {
        // Required empty public constructor
    }

    public AnimalGalleryFragment(AnimalModel di, int aid) {
        detailedImage = di;
        animalId= aid;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_galerija_slika, container, false);

        rvSlike = view.findViewById(R.id.rvSlike); // Use view.findViewById instead of findViewById
        apiService = ApiClient.getClient().create(ApiService.class);

        initializeRecyclerView();
        loadGallery();
        getSlike();

        return view;
    }

    private void initializeRecyclerView() {
        slikeAdapter = new SlikeAdapter((Activity) getContext(), listaSlika); // Pass context instead of activity
        rvSlike.setAdapter(slikeAdapter);
        rvSlike.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));  // Use getContext() instead of this
    }

    private void loadGallery() {
        apiService.getSlikeById(animalId).enqueue(new Callback<HttpRequestResponseList<SlikaModel>>() {
            @Override
            public void onResponse(Call<HttpRequestResponseList<SlikaModel>> call, Response<HttpRequestResponseList<SlikaModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaSlika.clear();
                    listaSlika.addAll(response.body().getResult());
                    slikeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<HttpRequestResponseList<SlikaModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                addImage(selectedImageUri);
            }
        }
    }

    private void addImage(Uri imageUri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(imageUri, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imagePath = cursor.getString(columnIndex);
            cursor.close();

            File file = new File(imagePath);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            apiService.addSlika(animalId, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Uspješno dodana slika.", Toast.LENGTH_SHORT).show();
                        getSlike();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Greška u API pozivu.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getSlike() {
        apiService.getSlikeById(animalId).enqueue(new Callback<HttpRequestResponseList<SlikaModel>>() {
            @Override
            public void onResponse(Call<HttpRequestResponseList<SlikaModel>> call, Response<HttpRequestResponseList<SlikaModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaSlika.clear();
                    listaSlika = response.body().getResult();
                    slikeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<HttpRequestResponseList<SlikaModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getSlike();
    }
}
