package com.activity.pis_azil.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.SendMail;
import com.activity.pis_azil.activities.MainActivity;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.activity.pis_azil.models.UserByEmailResponseModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.bumptech.glide.Glide;

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

public class OsnovniPodaciFragment extends Fragment {

    LinearLayout detailCard;
    ImageView animalImage;
    TextView animalName, animalType, animalAge, animalColor, animalDescription;
    Button buttonUdomi;
    AnimalModel detailedAnimal;
    UserModel currentUser;
    ApiService apiService;
    IsBlockedAnimalModel animalModel;
    private boolean isRequestInProgress = false;

    public OsnovniPodaciFragment() {
        // Required empty public constructor
    }

    public OsnovniPodaciFragment(AnimalModel da, UserModel cu, IsBlockedAnimalModel am) {
        detailedAnimal=da;
        currentUser=cu;
        animalModel=am;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_osnovni_podaci, container, false);

        detailCard=view.findViewById(R.id.detailCard);
        animalImage = view.findViewById(R.id.animalImage);
        animalName  = view.findViewById(R.id.animalName);
        animalType = view.findViewById(R.id.animalType);
        animalAge = view.findViewById(R.id.animalAge);
        animalColor = view.findViewById(R.id.animalColor);
        animalDescription = view.findViewById(R.id.animalDescription);
        buttonUdomi = view.findViewById(R.id.buttonUdomi);

        apiService = ApiClient.getClient().create(ApiService.class);

        animalName.setText(detailedAnimal.getImeLjubimca());
        animalType.setText(detailedAnimal.getTipLjubimca());
        animalAge.setText(String.valueOf(detailedAnimal.getDob()));
        animalColor.setText(detailedAnimal.getBoja());
        animalDescription.setText(detailedAnimal.getOpisLjubimca());
        Glide.with(getContext())
                .load(detailedAnimal.getImgUrl())
                .placeholder(R.drawable.paw)
                .error(R.drawable.milk2)
                .into(animalImage);

        // Prikaži gumb "Udomi" za sve korisnike, ali s različitim funkcionalnostima
        if (currentUser != null && currentUser.isAdmin()) {
            // Admin bira udomitelja
            buttonUdomi.setOnClickListener(v -> {
                if (!isRequestInProgress) {
                    isRequestInProgress = true;
                    buttonUdomi.setEnabled(false);  // privremeno onemogućimo gumb
                    showAdoptionDialogWithAllUsers();
                }
            });
        } else {
            // Obični korisnici mogu samo poslati zahtjev za udomljavanje
            buttonUdomi.setOnClickListener(v -> {
                if (!isRequestInProgress) {
                    isRequestInProgress = true;
                    buttonUdomi.setEnabled(false);  // privremeno onemogućimo gumb
                    requestAdoptionForUser(currentUser);
                }
            });
        }
        return view;
    }

    // Metoda za admina - prikaz dijaloga s popisom korisnika
    private void showAdoptionDialogWithAllUsers() {
        apiService.getAllUsers().enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> userNames = new ArrayList<>();
                    List<Integer> userIds = new ArrayList<>();

                    // Prikazujemo samo korisnike koji nisu admini
                    for (UserModel user : response.body()) {
                        if (!user.isAdmin()) {
                            userNames.add(user.getIme());
                            userIds.add(user.getIdKorisnika());
                        }
                    }

                    if (!userNames.isEmpty()) {
                        CharSequence[] usersArray = userNames.toArray(new CharSequence[0]);

                        new AlertDialog.Builder(getContext())
                                .setTitle("Odaberite udomitelja")
                                .setItems(usersArray, (dialog, which) -> {
                                    int selectedUserId = userIds.get(which);
                                    String selectedUserName = userNames.get(which);
                                    adoptAnimalForUser(selectedUserId, selectedUserName);
                                })
                                .show();
                    } else {
                        Toast.makeText(getContext(), "Nema dostupnih korisnika za udomljavanje.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Greška u dohvaćanju korisnika.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Metoda za korisnike - zahtjev za udomljavanje životinje za sebe (dodaje se u listu za odobrenje)
    private void requestAdoptionForUser(UserModel currentUser) {
        if (currentUser != null) {
            Log.i("korisnik", "Korisnik šalje zahtjev za udomljavanje: " + currentUser.getIme());
            adoptAnimal(currentUser.getIdKorisnika(), true); // Korisnik šalje zahtjev koji čeka odobrenje
        }
    }

    // Definicija interfejsa unutar DetailedActivity
    public interface OnEmailFetchedListener {
        void onEmailFetched(String email);
    }

    // Metoda za admina - udomljavanje životinje za odabranog korisnika
    private void adoptAnimalForUser(int userId, String userName) {
        //Log.d(TAG, "Udomljavanje životinje za korisnika: " + userName);

        // Pozovemo adoptAnimal i nakon toga pošaljemo email
        adoptAnimal(userId, false); // Admin odmah odobrava udomljavanje

        // Nakon odabira udomitelja, dohvatite email korisnika i pošaljite email
        /*getEmailById(userId, email -> {
            if (email != null && !email.isEmpty()) {
                String subject = "Životinja je uspješno udomljena!";
                String body = "Poštovani, " + userName + " je uspješno udomio/udomila životinju " + animalModel.getImeLjubimca() + ".";

                // Pokretanje nove niti za slanje emaila kako ne bi blokiralo glavni thread
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
                //Log.e(TAG, "Korisnik nema email adresu ili email nije dostupan.");
            }
        });*/
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

    // Zajednička metoda za udomljavanje ili zahtjev za udomljavanje
    private void adoptAnimal(int userId, boolean requiresApproval) {
        /*
        View view = getView();
        if (view == null) {
            return;
        }

        Button btnAdopt = view.findViewById(R.id.buttonUdomi);

        btnAdopt.setEnabled(false);
        */

        if (animalModel == null) {
            //Log.e(TAG, "AnimalModel je null, prekidam proces.");
            return;
        }

        MyAdoptionModel adoptionModel = new MyAdoptionModel();
        adoptionModel.setId(animalModel.getIdLjubimca());
        adoptionModel.setIdLjubimca(animalModel.getIdLjubimca());
        Log.i("id ljubimca", String.valueOf(animalModel.getIdLjubimca()));

        // Postavljanje imena ljubimca
        if (animalModel.getImeLjubimca() != null && !animalModel.getImeLjubimca().isEmpty()) {
            adoptionModel.setImeLjubimca(animalModel.getImeLjubimca());
        } else {
            //Log.w(TAG, "Ime ljubimca nije postavljeno, postavljam 'Nepoznato ime'.");
            adoptionModel.setImeLjubimca("Nepoznato ime");
        }

        // Postavljanje tipa ljubimca
        if (animalModel.getTipLjubimca() != null && !animalModel.getTipLjubimca().isEmpty()) {
            adoptionModel.setTipLjubimca(animalModel.getTipLjubimca());
        } else {
            //Log.w(TAG, "Tip ljubimca nije postavljen, postavljam 'Nepoznato tip'.");
            adoptionModel.setTipLjubimca("Nepoznato tip");
        }

        adoptionModel.setOpisLjubimca(animalModel.getOpisLjubimca());

        // Postavljanje trenutnog datuma i vremena
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
        adoptionModel.setIdKorisnika(userId);  // Postavljanje korisnika

        if (requiresApproval) {
            // Ako zahtjeva odobrenje, označava se kao neudomljena životinja
            adoptionModel.setUdomljen(false);
            adoptionModel.setStatusUdomljavanja(true); // Čeka odobrenje
            adoptionModel.setZahtjevUdomljavanja(true);
            adoptionModel.setZahtjevUdomljavanja(true);
            //Log.d(TAG, "Print");
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("zahtjev_udomljen", adoptionModel.isStatusUdomljavanja());

            apiService.adoptAnimal((adoptionModel.getIdLjubimca())).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        //Log.d(TAG, "Životinja uspješno ažurirana.");
                        apiService.addAdoption(adoptionModel).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    //Log.d(TAG, "Proces udomljavanja uspješno završen.");
                                    if (requiresApproval) {
                                        Toast.makeText(getContext(), "Zahtjev za udomljavanje poslan.", Toast.LENGTH_SHORT).show();
                                        resetRequestState();
                                    } else {
                                        Toast.makeText(getContext(), "Životinja je uspješno udomljena!", Toast.LENGTH_SHORT).show();
                                    }

                                    // Postavi rezultat kao uspješan i vrati ID udomljene životinje
                                    //Intent resultIntent = new Intent();
                                    //resultIntent.putExtra("udomljena_zivotinja_id", animalModel.getIdLjubimca());
                                    //setResult(RESULT_OK, resultIntent);
                                    //finish();
                                    Log.i("uspjesno", "uspjesno");
                                    Intent i = new Intent(getContext(), MainActivity.class);
                                    startActivity(i);
                                } else {
                                    //Log.e(TAG, "Greška u procesu udomljavanja: " + response.message());
                                    Toast.makeText(getContext(), "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                //Log.e(TAG, "Greška prilikom udomljavanja: ", t);
                                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                resetRequestState();
                            }
                        });
                    } else {
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    //Log.e(TAG, "API poziv nije uspio: ", t);
                    Toast.makeText(getContext(), "Greška u API pozivu.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Ako admin odobrava odmah, označava se kao udomljena
            adoptionModel.setUdomljen(true);
            adoptionModel.setStatusUdomljavanja(false);
            apiService.adoptAnimalByAdmin(animalModel.getIdLjubimca()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        //Log.e(TAG, "Uspješno udomljavanje životinje kod admina. " + response.message());
                        apiService.addAdoption(adoptionModel).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    //Log.d(TAG, "Proces udomljavanja uspješno završen.");
                                    if (requiresApproval) {
                                        Toast.makeText(getContext(), "Zahtjev za udomljavanje poslan.", Toast.LENGTH_SHORT).show();
                                        resetRequestState();
                                    } else {
                                        Toast.makeText(getContext(), "Životinja je uspješno udomljena!", Toast.LENGTH_SHORT).show();
                                    }

                                    // Postavi rezultat kao uspješan i vrati ID udomljene životinje
                                    //Intent resultIntent = new Intent();
                                    //resultIntent.putExtra("udomljena_zivotinja_id", animalModel.getIdLjubimca());
                                    //setResult(RESULT_OK, resultIntent);
                                    //finish();
                                    Log.i("uspjesno", "uspjesno");
                                    Intent i = new Intent(getContext(), MainActivity.class);
                                    startActivity(i);

                                } else {
                                    //Log.e(TAG, "Greška u procesu udomljavanja: " + response.message());
                                    Toast.makeText(getContext(), "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                //Log.e(TAG, "Greška prilikom udomljavanja: ", t);
                                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                resetRequestState();
                            }
                        });

                    } else {
                        //Log.e(TAG, "Greška u procesu udomljavanja: " + response.message());
                        Toast.makeText(getContext(), "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    //Log.e(TAG, "Greška prilikom udomljavanja: ", t);
                    Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        //Log.d(TAG, "Slanje modela udomljavanja na API: " + adoptionModel);

        /*apiService.addAdoption(adoptionModel).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    //Log.d(TAG, "Proces udomljavanja uspješno završen.");
                    if (requiresApproval) {
                        Toast.makeText(getContext(), "Zahtjev za udomljavanje poslan.", Toast.LENGTH_SHORT).show();
                        resetRequestState();
                    } else {
                        Toast.makeText(getContext(), "Životinja je uspješno udomljena!", Toast.LENGTH_SHORT).show();
                    }

                    // Postavi rezultat kao uspješan i vrati ID udomljene životinje
                    //Intent resultIntent = new Intent();
                    //resultIntent.putExtra("udomljena_zivotinja_id", animalModel.getIdLjubimca());
                    //setResult(RESULT_OK, resultIntent);
                    //finish();
                    Log.i("uspjesno", "uspjesno");
                    Intent i = new Intent(getContext(), MainActivity.class);
                    startActivity(i);

                } else {
                    //Log.e(TAG, "Greška u procesu udomljavanja: " + response.message());
                    Toast.makeText(getContext(), "Greška: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                //Log.e(TAG, "Greška prilikom udomljavanja: ", t);
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                resetRequestState();
            }
        });*/
    }

    private void resetRequestState() {
        isRequestInProgress = false;
        buttonUdomi.setEnabled(false);
        // buttonUdomi.setBackgroundColor(Color.GRAY);
    }
}