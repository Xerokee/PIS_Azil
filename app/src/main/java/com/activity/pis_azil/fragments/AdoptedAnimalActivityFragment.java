package com.activity.pis_azil.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public AdoptedAnimalActivityFragment(int aid) {
        animalId = aid;
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
        View view = inflater.inflate(R.layout.fragment_adopted_animal_activity, container, false);

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
                    linearLayoutAktivnosti.removeAllViews();

                    if (listaAktivnosti.isEmpty()) {
                        tvNemaAktivnosti.setVisibility(View.VISIBLE);
                    } else {
                        tvNemaAktivnosti.setVisibility(View.GONE);
                        for (Aktivnost a : listaAktivnosti) {
                            // CardView setup
                            CardView cardView = new CardView(getContext());
                            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            cardParams.setMargins(0, 0, 0, 30); // razmak između kartica
                            cardView.setLayoutParams(cardParams);
                            cardView.setRadius(20); // više zaobljenja
                            cardView.setCardElevation(8f);
                            cardView.setCardBackgroundColor(Color.WHITE);

                            // Glavni layout unutar CardView
                            LinearLayout verticalLayout = new LinearLayout(getContext());
                            verticalLayout.setOrientation(LinearLayout.VERTICAL);
                            verticalLayout.setPadding(40, 30, 40, 30); // unutarnji padding

                            // Horizontalni red za naslov i tip aktivnosti
                            LinearLayout topRow = new LinearLayout(getContext());
                            topRow.setOrientation(LinearLayout.HORIZONTAL);
                            topRow.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                            // Naslov (opis aktivnosti)
                            TextView tvOpis = new TextView(getContext());
                            tvOpis.setText(a.getOpis());
                            tvOpis.setTextSize(16);
                            tvOpis.setTypeface(null, Typeface.BOLD);
                            tvOpis.setTextColor(Color.parseColor("#000000"));
                            tvOpis.setLayoutParams(new LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)); // zauzima 1f prostora

                            // Tip aktivnosti (desno)
                            TextView tvAktivnost = new TextView(getContext());
                            tvAktivnost.setText(a.getAktivnost());
                            tvAktivnost.setTextSize(14);
                            tvAktivnost.setTypeface(null, Typeface.NORMAL);
                            tvAktivnost.setTextColor(Color.parseColor("#000000"));

                            topRow.addView(tvOpis);
                            topRow.addView(tvAktivnost);

                            // Datum ispod
                            TextView tvDatum = new TextView(getContext());
                            tvDatum.setText(a.getDatum());
                            tvDatum.setTextSize(14);
                            tvDatum.setTextColor(Color.parseColor("#000000"));
                            tvDatum.setPadding(0, 20, 0, 0);

                            // Sve zajedno
                            verticalLayout.addView(topRow);
                            verticalLayout.addView(tvDatum);

                            cardView.addView(verticalLayout);
                            linearLayoutAktivnosti.addView(cardView);
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

