package com.activity.pis_azil.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import androidx.viewpager2.widget.ViewPager2;
import com.activity.pis_azil.adapters.ImagePagerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.GalleryImageModel;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.R;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailedActivity extends AppCompatActivity {

    private static final String TAG = "DetailedActivity"; // Dodano za logiranje

    ImageView detailedImg;
    ViewPager2 viewPager;
    TextView name, description;
    Button addToCart;
    Toolbar toolbar;
    ApiService apiService;
    IsBlockedAnimalModel animalModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        // Inicijalizacija ViewPager2
        viewPager = findViewById(R.id.viewPager);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Retrieve the animal ID passed from HomeFragment
        animalModel = (IsBlockedAnimalModel) getIntent().getSerializableExtra("animal");

        // Logiram vrijednost proslijeđenog modela životinje
        Log.d(TAG, "Proslijeđeni model životinje: " + animalModel);

        detailedImg = findViewById(R.id.detailed_img);
        name = findViewById(R.id.detailed_name);
        description = findViewById(R.id.detailed_dec);

        if (animalModel != null) {
            // Fetch full details of the animal
            Log.d(TAG, "ID ljubimca: " + animalModel.getIdLjubimca());
            fetchAnimalDetails(animalModel.getIdLjubimca());
        } else {
            Log.e(TAG, "AnimalModel je null, završavam aktivnost.");
            Toast.makeText(this, "Greška u dohvaćanju podataka o ljubimcu", Toast.LENGTH_SHORT).show();
            finish();
        }

        addToCart = findViewById(R.id.add_to_cart);
        addToCart.setOnClickListener(v -> addedToCart());
    }

    private void fetchAnimalDetails(int animalId) {
        Log.d(TAG, "Počinjem dohvat detalja za ljubimca s ID-em: " + animalId);
        apiService.getAnimalById(animalId).enqueue(new Callback<AnimalModel>() {
            @Override
            public void onResponse(Call<AnimalModel> call, Response<AnimalModel> response) {
                if (response.isSuccessful()) {
                    AnimalModel detailedAnimal = response.body();
                    if (detailedAnimal != null) {
                        Log.d(TAG, "Dohvaćeni detalji životinje: " + detailedAnimal.toString());
                        // Update UI with the fetched details
                        updateUIWithDetails(detailedAnimal);
                    } else {
                        Log.e(TAG, "Nema detalja za ljubimca.");
                        Toast.makeText(DetailedActivity.this, "No animal details found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Neuspješan dohvat detalja, kod: " + response.code() + ", poruka: " + response.message());
                    Toast.makeText(DetailedActivity.this, "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AnimalModel> call, Throwable t) {
                Log.e(TAG, "Greška prilikom dohvata detalja ljubimca: ", t);
                Toast.makeText(DetailedActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithDetails(AnimalModel detailedAnimal) {
        Log.d(TAG, "Ažuriram UI s detaljima ljubimca.");

        name.setText(detailedAnimal.getImeLjubimca());
        description.setText(detailedAnimal.getOpisLjubimca());

        // Update Image
        Log.d(TAG, "Učitavam sliku iz URL-a: " + detailedAnimal.getImgUrl());
        Glide.with(getApplicationContext())
                .load(detailedAnimal.getImgUrl())
                .placeholder(R.drawable.paw)
                .error(R.drawable.milk2)
                .into(detailedImg);

        // Update Gallery in ViewPager
        List<String> galleryUrls = new ArrayList<>();
        for (GalleryImageModel galleryImage : detailedAnimal.getGalerijaZivotinja()) {
            galleryUrls.add(galleryImage.getImgUrl());
        }

        Log.d(TAG, "Broj slika u galeriji: " + galleryUrls.size());
        if (!galleryUrls.isEmpty()) {
            ImagePagerAdapter adapter = new ImagePagerAdapter(this, galleryUrls);
            viewPager.setAdapter(adapter);
        } else {
            Log.d(TAG, "Nema dostupnih slika u galeriji za ovog ljubimca.");
        }
    }

    private void addedToCart() {
        Log.d(TAG, "Dodavanje ljubimca u listu udomljavanja...");

        if (animalModel == null) {
            Log.e(TAG, "AnimalModel je null, prekidam dodavanje.");
            return;
        }

        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);
        Log.d(TAG, "Podaci o korisniku iz SharedPreferences: " + userJson);
        UserModel currentUser = null;

        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, UserModel.class);
        }

        if (currentUser == null) {
            Log.e(TAG, "Trenutni korisnik nije prepoznat.");
            Toast.makeText(this, "Nije moguće prepoznati trenutnog korisnika", Toast.LENGTH_SHORT).show();
            return;
        }

        MyAdoptionModel adoptionModel = new MyAdoptionModel();
        adoptionModel.setIdLjubimca(animalModel.getIdLjubimca());

        // Postavljanje imena ljubimca
        if (animalModel.getImeLjubimca() != null && !animalModel.getImeLjubimca().isEmpty()) {
            adoptionModel.setImeLjubimca(animalModel.getImeLjubimca());
        } else {
            Log.w(TAG, "Ime ljubimca nije postavljeno, postavljam 'Nepoznato ime'.");
            adoptionModel.setImeLjubimca("Nepoznato ime");
        }

        // Postavljanje tipa ljubimca
        if (animalModel.getTipLjubimca() != null && !animalModel.getTipLjubimca().isEmpty()) {
            adoptionModel.setTipLjubimca(animalModel.getTipLjubimca());
        } else {
            Log.w(TAG, "Tip ljubimca nije postavljen, postavljam 'Nepoznato tip'.");
            adoptionModel.setTipLjubimca("Nepoznato tip");
        }

        adoptionModel.setOpisLjubimca(animalModel.getOpisLjubimca());

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        // Adjust timezone to Croatian timezone
        sdfDate.setTimeZone(TimeZone.getTimeZone("Europe/Zagreb"));
        sdfTime.setTimeZone(TimeZone.getTimeZone("Europe/Zagreb"));

        // Postavljanje datuma i vremena
        String currentDate = sdfDate.format(Calendar.getInstance().getTime());
        String currentTime = sdfTime.format(Calendar.getInstance().getTime());
        adoptionModel.setDatum(currentDate);
        adoptionModel.setVrijeme(currentTime);
        adoptionModel.setImgUrl(animalModel.getImgUrl());
        adoptionModel.setIdKorisnika(currentUser.getIdKorisnika());
        adoptionModel.setUdomljen(false);
        adoptionModel.setStanjeZivotinje(false);
        adoptionModel.setStatusUdomljavanja(true);

        Log.d(TAG, "Slanje adoption modela na API: " + adoptionModel);

        apiService.addAdoption(adoptionModel).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Udomljavanje uspješno dodano.");
                    Toast.makeText(DetailedActivity.this, "Dodano u listu udomljavanja!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Neuspješno dodavanje udomljavanja: " + response.message());
                    Toast.makeText(DetailedActivity.this, "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Greška prilikom dodavanja udomljavanja: ", t);
                Toast.makeText(DetailedActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
