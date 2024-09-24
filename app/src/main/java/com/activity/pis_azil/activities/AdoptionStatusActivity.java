package com.activity.pis_azil.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.activity.pis_azil.R;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdoptionStatusActivity extends AppCompatActivity {
    private ApiService apiService;
    public int idLjubimca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adoption_status);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Preuzimanje ID-a ljubimca iz Intent-a, ovo treba biti idLjubimca, ne idKorisnika
        idLjubimca = getIntent().getIntExtra("idLjubimca", 0); // 0 je defaultna vrijednost ako nije postavljeno

        Log.d("AdoptionStatusActivity", "Preuzeti idLjubimca: " + idLjubimca);

        // Provjeri je li idLjubimca ispravan
        if (idLjubimca == 0) {
            Toast.makeText(this, "Greška: ID ljubimca nije ispravan!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Poziv metode za dohvaćanje statusa udomljavanja
        fetchAdoptionStatus();

        Button confirmButton = findViewById(R.id.confirm_but);
        Button rejectButton = findViewById(R.id.reject_but);

        // Kada se pritisne na prihvati/odbij gumb
        confirmButton.setOnClickListener(v -> updateAdoptionStatus(true));
        rejectButton.setOnClickListener(v -> updateAdoptionStatus(false));
    }

    // Metoda za dohvaćanje statusa udomljavanja
    private void fetchAdoptionStatus() {
        apiService.getAdoptionStatus(idLjubimca).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean status = response.body();
                    // Ažuriraj UI ili pohrani status
                    Log.d("AdoptionStatus", "Trenutni status udomljavanja: " + status);
                } else {
                    Log.e("AdoptionStatus", "Greška pri dohvaćanju statusa: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("AdoptionStatus", "API poziv nije uspio: ", t);
            }
        });
    }

    private void updateAdoptionStatus(boolean status) {
        // Kreiramo mapu sa statusom udomljavanja
        Map<String, Boolean> statusUpdate = new HashMap<>();
        statusUpdate.put("status_udomljavanja", status);

        // Logiranje prije slanja zahtjeva
        Log.d("API_CALL", "Request Body: " + statusUpdate.toString());
        Log.d("API_CALL", "Updating adoption status for ID_ljubimca: " + idLjubimca);

        // PUT zahtjev za ažuriranje statusa udomljavanja
        apiService.updateAdoptionStatus(idLjubimca, statusUpdate).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdoptionStatusActivity.this, "Status uspješno ažuriran", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        int statusCode = response.code();

                        Log.d("API_CALL", "Updating adoption status with ID_ljubimca: " + idLjubimca + " and status: " + status);
                        Log.e("API_ERROR", "Greška pri ažuriranju statusa. Kod: " + statusCode + ", Odgovor: " + errorBody);
                        Toast.makeText(AdoptionStatusActivity.this, "Greška pri ažuriranju statusa: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e("API_ERROR", "Greška pri parsiranju tijela pogreške", e);
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("API_FAILURE", "API poziv nije uspio: ", t);
                t.printStackTrace();
                Toast.makeText(AdoptionStatusActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
