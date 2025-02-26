package com.activity.pis_azil.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.Aktivnost;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.network.HttpRequestResponseList;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AdoptedAnimalActivityFragment extends Fragment {

    int animalId;
    LinearLayout linearLayoutAktivnosti;
    TextView tvNemaAktivnosti;
    List<Aktivnost> listaAktivnosti = new ArrayList<>();
    ApiService apiService;
    private BroadcastReceiver aktivnostReceiver;

    public AdoptedAnimalActivityFragment() {
        // Required empty public constructor
    }

    public AdoptedAnimalActivityFragment(int aid){
        animalId=aid;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicijalizacija broadcast receivera
        aktivnostReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int receivedAnimalId = intent.getIntExtra("animalId", -1);

                // Provjerimo je li obavijest za ovu životinju
                if (receivedAnimalId == animalId) {
                    String aktivnost = intent.getStringExtra("aktivnost");
                    String datum = intent.getStringExtra("datum");

                    // Prikazujemo Toast obavijest
                    Toast.makeText(getContext(),
                            "Nova aktivnost za životinju: " + aktivnost + " (" + datum + ")",
                            Toast.LENGTH_LONG).show();

                    // Osvježavamo popis aktivnosti
                    refreshPopisAktivnosti();
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_adopted_animal_activity, container, false);

        linearLayoutAktivnosti = view.findViewById(R.id.linearLayoutAktivnosti);
        tvNemaAktivnosti = view.findViewById(R.id.tvNemaAktivnosti);

        apiService = ApiClient.getClient().create(ApiService.class);

        refreshPopisAktivnosti();

        return view;
    }

    public void refreshPopisAktivnosti() {
        apiService.getAktivnostiById(animalId).enqueue(new Callback<HttpRequestResponseList<Aktivnost>>() {
            @Override
            public void onResponse(Call<HttpRequestResponseList<Aktivnost>> call, Response<HttpRequestResponseList<Aktivnost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaAktivnosti = response.body().getResult();
                    if (listaAktivnosti.size() == 0) {
                        tvNemaAktivnosti.setVisibility(View.VISIBLE);
                    } else {
                        linearLayoutAktivnosti.removeAllViews();
                        for (Aktivnost a : listaAktivnosti) {
                            TextView tv = new TextView(getContext());
                            String text = a.getDatum() + " " + a.getAktivnost() + "\n" + a.getOpis();
                            tv.setText(text);
                            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
                            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            tv.setTextSize(18);
                            tv.setTextColor(Color.parseColor("#FFFFFF"));
                            linearLayoutAktivnosti.addView(tv);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<HttpRequestResponseList<Aktivnost>> call, Throwable t) {
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Registriramo receiver kad je fragment vidljiv
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(aktivnostReceiver, new IntentFilter("com.activity.pis_azil.NOVA_AKTIVNOST"));
        refreshPopisAktivnosti();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Odregistriramo receiver kad fragment nije vidljiv
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(aktivnostReceiver);
    }
}