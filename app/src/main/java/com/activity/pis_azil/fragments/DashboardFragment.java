package com.activity.pis_azil.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.StatistikaModel;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.models.UserByEmailResponseModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.network.HttpRequestResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private TextView tvTotalAnimals, tvAdoptedAnimals, tvAvailableAnimals, tvRequest, tvDeclineRequest;
    private ApiService apiService;

    public DashboardFragment() {}

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        tvTotalAnimals = root.findViewById(R.id.tvTotalAnimals);
        tvAdoptedAnimals = root.findViewById(R.id.tvAdoptedAnimals);
        tvAvailableAnimals = root.findViewById(R.id.tvAvailableAnimals);
        tvRequest = root.findViewById(R.id.tvRequest);
        tvDeclineRequest = root.findViewById(R.id.tvDeclineRequest);

        apiService = ApiClient.getClient().create(ApiService.class);

        fetchAnimalStatistics();

        return root;
    }

    private void fetchAnimalStatistics() {
        apiService.getStatistika().enqueue(new Callback<HttpRequestResponse<StatistikaModel>>() {
            @Override
            public void onResponse(Call<HttpRequestResponse<StatistikaModel>> call, Response<HttpRequestResponse<StatistikaModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StatistikaModel statistika = response.body().getResult();
                    Log.i("statistika",String.valueOf(statistika));
                    tvTotalAnimals.setText(String.valueOf(statistika.getBroj_zivotinja()));
                    tvAdoptedAnimals.setText(String.valueOf(statistika.getUdomljene_zivotinje()));
                    tvAvailableAnimals.setText(String.valueOf(statistika.getRaspolozive_zivotinje()));
                    tvRequest.setText(String.valueOf(statistika.getBroj_zahtjeva()));
                    tvDeclineRequest.setText(String.valueOf(statistika.getBroj_odbijenih_zahtjeva()));

                } else {
                    Toast.makeText(getContext(), "Greška pri učitavanju statistike.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HttpRequestResponse<StatistikaModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Greška pri učitavanju statistike.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isAnimalAdoptedInUpdateDnevnik(int animalId) {
        // Proverite da li je dnevnička lista null ili prazna pre nego što pokušate da je iterirate
        List<UpdateDnevnikModel> dnevnikList = getUpdateDnevnikData();

        if (dnevnikList == null || dnevnikList.isEmpty()) {
            return false;  // Ako lista nije dostupna ili je prazna, pretpostavljamo da životinja nije udomljena
        }

        // Ako je lista validna, nastavite sa proverom
        for (UpdateDnevnikModel dnevnik : dnevnikList) {
            if (dnevnik.getId_ljubimca() == animalId && dnevnik.isUdomljen()) {
                return true;
            }
        }

        return false;  // Ako ne pronađemo udomljenog ljubimca, vraćamo false
    }

    private List<UpdateDnevnikModel> getUpdateDnevnikData() {
        final List<UpdateDnevnikModel> dnevnikList = new ArrayList<>();

        apiService.getDnevnikUdomljavanja().enqueue(new Callback<List<UpdateDnevnikModel>>() {
            @Override
            public void onResponse(Call<List<UpdateDnevnikModel>> call, Response<List<UpdateDnevnikModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dnevnikList.addAll(response.body());
                } else {
                    // Greška u odgovoru
                    // Toast.makeText(getContext(), "Greška pri učitavanju dnevničkih podataka", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UpdateDnevnikModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Greška pri povezivanju s serverom", Toast.LENGTH_SHORT).show();
            }
        });

        return dnevnikList; // Vraćamo listu koja je ažurirana iz servera
    }
}
