package com.activity.pis_azil.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.fragments.MyAnimalsFragment;
import com.activity.pis_azil.models.Aktivnost;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.RejectAdoptionModelRead;
import com.activity.pis_azil.models.UpdateAnimalModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.network.HttpRequestResponse;
import com.activity.pis_azil.network.HttpRequestResponseList;
import com.bumptech.glide.Glide;
import com.orhanobut.dialogplus.DialogPlus;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimalDetailActivity extends AppCompatActivity {

    String sAnimalId;
    int animalId;
    TextView animalName, animalType, animalAge, animalColor, animalDescription, tvNemaAktivnosti;
    Button animalEdit;
    ImageView animalImage, arrowBack;
    ApiService apiService;
    AnimalModel animal;
    private static final int SELECT_IMAGE_CODE = 1;
    List<Aktivnost> listaAktivnosti = new ArrayList<>();
    LinearLayout linearLayoutAktivnosti;
    ImageButton addAktivnost;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_detail);

        final Bundle oExtras= getIntent().getExtras();
        sAnimalId= oExtras.getString("id");
        animalId= Integer.parseInt(sAnimalId);

        animalName=findViewById(R.id.animalName);
        animalType=findViewById(R.id.animalType);
        animalAge=findViewById(R.id.animalAge);
        animalColor=findViewById(R.id.animalColor);
        animalDescription=findViewById(R.id.animalDescription);
        animalEdit=findViewById(R.id.animalEdit);
        animalImage=findViewById(R.id.animalImage);
        arrowBack = findViewById(R.id.arrowBack);
        tvNemaAktivnosti= findViewById(R.id.tvNemaAktivnosti);
        linearLayoutAktivnosti = findViewById(R.id.linearLayoutAktivnosti);
        addAktivnost = findViewById(R.id.addAktivnost);

        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAnimalById(animalId).enqueue(new Callback<AnimalModel>() {
            @Override
            public void onResponse(Call<AnimalModel> call, Response<AnimalModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    animal = response.body();
                    animalName.setText(animal.getImeLjubimca());
                    animalType.setText(animal.getTipLjubimca());
                    animalAge.setText(String.valueOf(animal.getDob()));
                    animalColor.setText(animal.getBoja());
                    animalDescription.setText(animal.getOpisLjubimca());
                    Glide.with(AnimalDetailActivity.this).load(animal.getImgUrl()).into(animalImage);
                } else {
                    // Toast.makeText(AnimalDetailActivity.this, "Greška pri dohvaćanju životinje", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AnimalModel> call, Throwable t) {
                Toast.makeText(AnimalDetailActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        refreshPopisAktivnosti();

        animalEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogPlus dialogPlus = DialogPlus.newDialog(v.getContext())
                        .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.update_animal))
                        .setExpanded(true,1700)
                        .setGravity(Gravity.CENTER)
                        .setCancelable(true)
                        .create();

                View view = dialogPlus.getHolderView();
                EditText editName= view.findViewById(R.id.editAnimalName);
                EditText editAge= view.findViewById(R.id.editAnimalAge);
                EditText editDescription= view.findViewById(R.id.editAnimalDescription);
                Button saveAnimalEdit = view.findViewById(R.id.saveAnimalEdit);

                editName.setText(animal.getImeLjubimca());
                editAge.setText(String.valueOf(animal.getDob()));
                editDescription.setText(animal.getOpisLjubimca());

                dialogPlus.show();

                saveAnimalEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Objects.equals(editName.getText().toString(),"") || Objects.equals(editAge.getText().toString(),"") || Objects.equals(editDescription.getText().toString(),"")){
                            Toast.makeText(v.getContext(), "Nisu popunjena sve polja.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            UpdateAnimalModel updateAnimal = new UpdateAnimalModel(editName.getText().toString(), editDescription.getText().toString(), Integer.parseInt(editAge.getText().toString()));
                            apiService.updateAnimalDetail(animal.getIdLjubimca(), updateAnimal).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(AnimalDetailActivity.this, "Životinja uspješno ažurirana.", Toast.LENGTH_SHORT).show();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialogPlus.dismiss();
                                                animalName.setText(editName.getText().toString());
                                                animalAge.setText(editAge.getText().toString());
                                                animalDescription.setText(editDescription.getText().toString());
                                            }
                                        }, 3000);
                                    } else {
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(AnimalDetailActivity.this, "Greška u API pozivu.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });

        addAktivnost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogPlus dialogPlus = DialogPlus.newDialog(v.getContext())
                        .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.add_activity))
                        .setExpanded(true,1000)
                        .setGravity(Gravity.CENTER)
                        .setCancelable(true)
                        .create();

                View view = dialogPlus.getHolderView();
                TextView inputDateActivity = view.findViewById(R.id.inputDateActivity);
                Button addActivity = view.findViewById(R.id.addActivity);
                Button closeActivity = view.findViewById(R.id.closeActivity);
                EditText inputDescriptionActivity = view.findViewById(R.id.inputDescriptionActivity);

                dialogPlus.show();

                closeActivity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inputDescriptionActivity.setText("");
                        dialogPlus.dismiss();
                    }
                });

                addActivity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Objects.equals(inputDateActivity.getText().toString(), "") || Objects.equals(inputDescriptionActivity.getText().toString(), "")){
                            Toast.makeText(AnimalDetailActivity.this, "Moraju biti popunjeni svi podaci.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Aktivnost novaAktivnost = new Aktivnost(0, animalId, inputDateActivity.getText().toString(), inputDescriptionActivity.getText().toString());
                            apiService.addAktivnost(novaAktivnost).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(AnimalDetailActivity.this, "Aktivnost uspješno dodana.", Toast.LENGTH_SHORT).show();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialogPlus.dismiss();
                                                inputDescriptionActivity.setText("");
                                                refreshPopisAktivnosti();
                                            }
                                        }, 3000);
                                    } else {
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(AnimalDetailActivity.this, "Greška u API pozivu.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

                inputDateActivity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);


                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                AnimalDetailActivity.this,
                                (view1, selectedYear, selectedMonth, selectedDay) -> {
                                    String selectedDate =  String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear);
                                    inputDateActivity.setText(selectedDate);
                                },
                                year, month, day);
                        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                        datePickerDialog.show();
                    }
                });
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void refreshPopisAktivnosti(){
        apiService.getAktivnostiById(animalId).enqueue(new Callback<HttpRequestResponseList<Aktivnost>>() {
            @Override
            public void onResponse(Call<HttpRequestResponseList<Aktivnost>> call, Response<HttpRequestResponseList<Aktivnost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaAktivnosti = response.body().getResult();
                    if (listaAktivnosti.size() == 0){
                        tvNemaAktivnosti.setVisibility(View.VISIBLE);
                    }
                    else{
                        linearLayoutAktivnosti.removeAllViews();
                        for (Aktivnost a : listaAktivnosti){
                            TextView tv = new TextView(getApplicationContext());
                            String text = a.getDatum() + " " + a.getOpis();
                            tv.setText(text);
                            tv.setTypeface(tv.getTypeface(), Typeface.BOLD_ITALIC);
                            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            tv.setTextSize(18);
                            linearLayoutAktivnosti.addView(tv);
                        }
                    }
                } else {
                    // Toast.makeText(AnimalDetailActivity.this, "Greška pri dohvaćanju aktivnosti", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HttpRequestResponseList<Aktivnost>> call, Throwable t) {
                Toast.makeText(AnimalDetailActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshAnimalDetails() {
        apiService.getAnimalById(animalId).enqueue(new Callback<AnimalModel>() {
            @Override
            public void onResponse(Call<AnimalModel> call, Response<AnimalModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    animal = response.body();
                    animalName.setText(animal.getImeLjubimca());
                    animalType.setText(animal.getTipLjubimca());
                    animalAge.setText(String.valueOf(animal.getDob()));
                    animalColor.setText(animal.getBoja());
                    animalDescription.setText(animal.getOpisLjubimca());
                    Glide.with(AnimalDetailActivity.this).load(animal.getImgUrl()).into(animalImage);
                } else {
                    // Toast.makeText(AnimalDetailActivity.this, "Greška pri dohvaćanju životinje", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AnimalModel> call, Throwable t) {
                Toast.makeText(AnimalDetailActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Osvježi podatke kada se korisnik vrati u ovu aktivnost
        refreshAnimalDetails();  // Osvježava podatke o životinji
        refreshPopisAktivnosti();  // Osvježava popis aktivnosti
    }
}