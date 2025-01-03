package com.activity.pis_azil.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AdoptedAnimalDetailFragment extends Fragment {

    int animalId;
    String udomitelj;
    AnimalModel detailedAnimal;
    ApiService apiService;
    TextView animalName, animalType, dateAdd, timeAdd, animalState, adoptioUser;
    public AdoptedAnimalDetailFragment() {

    }

    public AdoptedAnimalDetailFragment(int aid, AnimalModel da, String u) {
        animalId=aid;
        detailedAnimal=da;
        udomitelj = u;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adopted_animal_detail, container, false);

        animalName  = view.findViewById(R.id.animalName);
        animalType = view.findViewById(R.id.animalType);
        animalState = view.findViewById(R.id.animalState);
        dateAdd = view.findViewById(R.id.dateAdd);
        timeAdd = view.findViewById(R.id.timeAdd);
        adoptioUser = view.findViewById(R.id.adoptionUser);

        apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getDnevnikUdomljavanja().enqueue(new Callback<List<UpdateDnevnikModel>>() {
            @Override
            public void onResponse(Call<List<UpdateDnevnikModel>> call, Response<List<UpdateDnevnikModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UpdateDnevnikModel> allAdoptions = response.body();
                    for (UpdateDnevnikModel adoption : allAdoptions) {
                        if (Objects.equals(adoption.getId(),animalId)) {
                            dateAdd.setText(adoption.getDatum());
                            timeAdd.setText(adoption.getVrijeme());
                            //Log.i("datum i vrijeme", String.valueOf(adoption.getDatum()));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<UpdateDnevnikModel>> call, Throwable t) {
            }
        });

        animalName.setText(detailedAnimal.getImeLjubimca());
        animalType.setText(detailedAnimal.getTipLjubimca());
        adoptioUser.setText(udomitelj);
        animalState.setText(detailedAnimal.StanjeZivotinje() ? "Dobro" : "Loše");

        return view;
    }


}