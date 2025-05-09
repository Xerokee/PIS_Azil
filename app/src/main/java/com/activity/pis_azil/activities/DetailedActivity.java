package com.activity.pis_azil.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.viewpager2.widget.ViewPager2;

import com.activity.pis_azil.SendMail;
import com.activity.pis_azil.adapters.ImagePagerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.adapters.MyAdoptionAdapter;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.GalleryImageModel;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.activity.pis_azil.models.UserByEmailResponseModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.R;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.mail.MessagingException;

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
    private boolean isRequestInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        viewPager = findViewById(R.id.viewPager);

        apiService = ApiClient.getClient().create(ApiService.class);

        animalModel = (IsBlockedAnimalModel) getIntent().getSerializableExtra("animal");

        //Log.d(TAG, "Proslijeđeni model životinje: " + animalModel);

        detailedImg = findViewById(R.id.detailed_img);
        name = findViewById(R.id.detailed_name);
        description = findViewById(R.id.detailed_dec);

        addToCart = findViewById(R.id.add_to_cart);

        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);
        UserModel currentUser;

        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, UserModel.class);
        } else {
            currentUser = null;
        }

        if (currentUser != null && currentUser.isAdmin()) {
            // Admin bira udomitelja
            addToCart.setOnClickListener(v -> {
                if (!isRequestInProgress) {
                    isRequestInProgress = true;
                    addToCart.setEnabled(false);
                    showAdoptionDialogWithAllUsers();
                }
            });
        } else {
            addToCart.setOnClickListener(v -> {
                if (!isRequestInProgress) {
                    isRequestInProgress = true;
                    addToCart.setEnabled(false);
                    requestAdoptionForUser(currentUser);
                }
            });
        }

        if (animalModel != null) {
            Log.d(TAG, "ID ljubimca: " + animalModel.getIdLjubimca());
            fetchAnimalDetails(animalModel.getIdLjubimca());
        } else {
            Log.e(TAG, "AnimalModel je null, završavam aktivnost.");
            Toast.makeText(this, "Greška u dohvaćanju podataka o ljubimcu", Toast.LENGTH_SHORT).show();
            finish();
        }
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

        Log.d(TAG, "Učitavam sliku iz URL-a: " + detailedAnimal.getImgUrl());
        Glide.with(getApplicationContext())
                .load(detailedAnimal.getImgUrl())
                .placeholder(R.drawable.paw)
                .error(R.drawable.milk2)
                .into(detailedImg);

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

    private void showAdoptionDialogWithAllUsers() {
        apiService.getAllUsers().enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> userNames = new ArrayList<>();
                    List<Integer> userIds = new ArrayList<>();

                    for (UserModel user : response.body()) {
                        if (!user.isAdmin()) {
                            userNames.add(user.getIme());
                            userIds.add(user.getIdKorisnika());
                        }
                    }

                    if (!userNames.isEmpty()) {
                        CharSequence[] usersArray = userNames.toArray(new CharSequence[0]);

                        new AlertDialog.Builder(DetailedActivity.this)
                                .setTitle("Odaberite udomitelja")
                                .setItems(usersArray, (dialog, which) -> {
                                    int selectedUserId = userIds.get(which);
                                    String selectedUserName = userNames.get(which);
                                    adoptAnimalForUser(selectedUserId, selectedUserName);
                                })
                                .show();
                    } else {
                        Toast.makeText(DetailedActivity.this, "Nema dostupnih korisnika za udomljavanje.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DetailedActivity.this, "Greška u dohvaćanju korisnika.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Toast.makeText(DetailedActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface OnEmailFetchedListener {
        void onEmailFetched(String email);
    }


    private void adoptAnimalForUser(int userId, String userName) {
        Log.d(TAG, "Udomljavanje životinje za korisnika: " + userName);

        adoptAnimal(userId, false);

        getEmailById(userId, email -> {
            if (email != null && !email.isEmpty()) {
                String subject = "Životinja je uspješno udomljena!";
                String body = "Poštovani, " + userName + " je uspješno udomio/udomila životinju " + animalModel.getImeLjubimca() + ".";

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SendMail.sendEmail("margeta.matija@gmail.com", subject, body);
                        } catch (GeneralSecurityException | IOException | MessagingException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println("send mail...");
                    }
                }).start();
            } else {
                Log.e(TAG, "Korisnik nema email adresu ili email nije dostupan.");
            }
        });
    }

    private void getEmailById(int userId, OnEmailFetchedListener listener) {
        apiService.getUserById(userId).enqueue(new Callback<UserByEmailResponseModel>() {
            @Override
            public void onResponse(Call<UserByEmailResponseModel> call, Response<UserByEmailResponseModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserModel user = response.body().getResult();
                    if (user != null && user.getEmail() != null) {
                        listener.onEmailFetched(user.getEmail());
                    } else {
                        listener.onEmailFetched(null);
                    }
                } else {
                    listener.onEmailFetched(null);
                }
            }

            @Override
            public void onFailure(Call<UserByEmailResponseModel> call, Throwable t) {
                listener.onEmailFetched(null);
            }
        });
    }

    private void requestAdoptionForUser(UserModel currentUser) {
        if (currentUser != null) {
            Log.d(TAG, "Korisnik šalje zahtjev za udomljavanje: " + currentUser.getIme());
            adoptAnimal(currentUser.getIdKorisnika(), true);
        }
    }

    private void adoptAnimal(int userId, boolean requiresApproval) {
        if (animalModel == null) {
            Log.e(TAG, "AnimalModel je null, prekidam proces.");
            return;
        }

        MyAdoptionModel adoptionModel = new MyAdoptionModel();
        adoptionModel.setId(animalModel.getIdLjubimca());
        adoptionModel.setIdLjubimca(animalModel.getIdLjubimca());
        Log.i("id ljubimca", String.valueOf(animalModel.getIdLjubimca()));

        if (animalModel.getImeLjubimca() != null && !animalModel.getImeLjubimca().isEmpty()) {
            adoptionModel.setImeLjubimca(animalModel.getImeLjubimca());
        } else {
            Log.w(TAG, "Ime ljubimca nije postavljeno, postavljam 'Nepoznato ime'.");
            adoptionModel.setImeLjubimca("Nepoznato ime");
        }

        if (animalModel.getTipLjubimca() != null && !animalModel.getTipLjubimca().isEmpty()) {
            adoptionModel.setTipLjubimca(animalModel.getTipLjubimca());
        } else {
            Log.w(TAG, "Tip ljubimca nije postavljen, postavljam 'Nepoznato tip'.");
            adoptionModel.setTipLjubimca("Nepoznato tip");
        }

        adoptionModel.setOpisLjubimca(animalModel.getOpisLjubimca());

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        sdfDate.setTimeZone(TimeZone.getTimeZone("Europe/Zagreb"));
        sdfTime.setTimeZone(TimeZone.getTimeZone("Europe/Zagreb"));

        String currentDate = sdfDate.format(Calendar.getInstance().getTime());
        String currentTime = sdfTime.format(Calendar.getInstance().getTime());

        adoptionModel.setDatum(currentDate);
        adoptionModel.setVrijeme(currentTime);
        adoptionModel.setImgUrl(animalModel.getImgUrl());
        adoptionModel.setIdKorisnika(userId);

        if (requiresApproval) {
            adoptionModel.setUdomljen(false);
            adoptionModel.setStatusUdomljavanja(true);
            adoptionModel.setZahtjevUdomljavanja(true);
            adoptionModel.setZahtjevUdomljavanja(true);
            Log.d(TAG, "Print");
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("zahtjev_udomljen", adoptionModel.isStatusUdomljavanja());

            apiService.adoptAnimal((adoptionModel.getIdLjubimca())).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Životinja uspješno ažurirana.");
                    } else {
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "API poziv nije uspio: ", t);
                    Toast.makeText(DetailedActivity.this, "Greška u API pozivu.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            adoptionModel.setUdomljen(true);
            adoptionModel.setStatusUdomljavanja(false);
            apiService.adoptAnimalByAdmin(animalModel.getIdLjubimca()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.e(TAG, "Uspješno udomljavanje životinje kod admina. " + response.message());

                    } else {
                        Log.e(TAG, "Greška u procesu udomljavanja: " + response.message());
                        Toast.makeText(DetailedActivity.this, "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Greška prilikom udomljavanja: ", t);
                    Toast.makeText(DetailedActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }

        Log.d(TAG, "Slanje modela udomljavanja na API: " + adoptionModel);

        apiService.addAdoption(adoptionModel).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Proces udomljavanja uspješno završen.");
                    if (requiresApproval) {
                        Toast.makeText(DetailedActivity.this, "Zahtjev za udomljavanje poslan.", Toast.LENGTH_SHORT).show();
                        resetRequestState();
                    } else {
                        Toast.makeText(DetailedActivity.this, "Životinja je uspješno udomljena!", Toast.LENGTH_SHORT).show();
                    }

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("udomljena_zivotinja_id", animalModel.getIdLjubimca());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Log.e(TAG, "Greška u procesu udomljavanja: " + response.message());
                    Toast.makeText(DetailedActivity.this, "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Greška prilikom udomljavanja: ", t);
                Toast.makeText(DetailedActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                resetRequestState();
            }
        });
    }

    private void resetRequestState() {
        isRequestInProgress = false;
        addToCart.setEnabled(true);
    }
}
