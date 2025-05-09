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
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdoptedAnimalDetailFragment extends Fragment {

    private static final String TAG = "AdoptedAnimalDetail";

    int animalId;
    String udomitelj;
    AnimalModel detailedAnimal;
    ApiService apiService;
    TextView animalName, animalType, dateAdd, timeAdd, adoptioUser;

    private Call<List<UpdateDnevnikModel>> dnevnikCall;

    public AdoptedAnimalDetailFragment() {
    }

    public AdoptedAnimalDetailFragment(int aid, AnimalModel da, String u) {
        animalId = aid;
        detailedAnimal = da;
        udomitelj = u;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adopted_animal_detail, container, false);

        animalName = view.findViewById(R.id.animalName);
        animalType = view.findViewById(R.id.animalType);
        dateAdd = view.findViewById(R.id.dateAdd);
        timeAdd = view.findViewById(R.id.timeAdd);
        adoptioUser = view.findViewById(R.id.adoptionUser);

        if (detailedAnimal != null) {
            animalName.setText(detailedAnimal.getImeLjubimca());
            animalType.setText(detailedAnimal.getTipLjubimca());
        } else {
            Log.w(TAG, "detailedAnimal je null pri kreiranju view-a.");
        }

        if (udomitelj != null) {
            adoptioUser.setText(udomitelj);
        } else {
            Log.w(TAG, "udomitelj je null pri kreiranju view-a.");
        }

        loadAdoptionDiaryData();

        return view;
    }

    private void loadAdoptionDiaryData() {
        if (dnevnikCall != null) {
            dnevnikCall.cancel();
        }

        dnevnikCall = apiService.getDnevnikUdomljavanja();
        dnevnikCall.enqueue(new Callback<List<UpdateDnevnikModel>>() {
            @Override
            public void onResponse(Call<List<UpdateDnevnikModel>> call, Response<List<UpdateDnevnikModel>> response) {
                if (!isAdded() || getView() == null) {
                    Log.d(TAG, "Fragment nije dodan ili je view uništen, preskačem UI update za dnevnik.");
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<UpdateDnevnikModel> allAdoptions = response.body();
                    boolean foundEntry = false;
                    for (UpdateDnevnikModel adoption : allAdoptions) {
                        if (Objects.equals(adoption.getId(), animalId)) {
                            dateAdd.setText(adoption.getDatum());
                            timeAdd.setText(adoption.getVrijeme());
                            foundEntry = true;
                            Log.i(TAG, "Pronađen dnevnik za animalId: " + animalId + ", Datum: " + adoption.getDatum() + ", Vrijeme: " + adoption.getVrijeme());
                            break;
                        }
                    }
                    if (!foundEntry) {
                        Log.w(TAG, "Nije pronađen unos u dnevniku za animalId: " + animalId);
                    }
                } else {
                    Log.e(TAG, "Neuspješno dohvaćanje dnevnika udomljavanja: " + response.code() + " - " + response.message());
                    if (isAdded() && getContext() != null) {
                    }
                }
            }

            @Override
            public void onFailure(Call<List<UpdateDnevnikModel>> call, Throwable t) {
                if (!isAdded() || getView() == null) {
                    return;
                }

                if (call.isCanceled()) {
                    Log.d(TAG, "Poziv za dnevnik udomljavanja je otkazan.");
                } else {
                    if (isAdded() && getContext() != null) {
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView pozvan za animalId: " + animalId);
        if (dnevnikCall != null) {
            dnevnikCall.cancel();
            dnevnikCall = null;
        }
        animalName = null;
        animalType = null;
        dateAdd = null;
        timeAdd = null;
        adoptioUser = null;
    }
}