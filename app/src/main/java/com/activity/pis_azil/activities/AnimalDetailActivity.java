package com.activity.pis_azil.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.fragments.MyAnimalsFragment;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.RejectAdoptionModelRead;
import com.activity.pis_azil.models.UpdateAnimalModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.bumptech.glide.Glide;
import com.orhanobut.dialogplus.DialogPlus;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimalDetailActivity extends AppCompatActivity {

    String sAnimalId;
    int animalId;
    TextView animalName, animalType, animalAge, animalColor, animalDescription;
    Button animalEdit;
    ImageView animalImage, arrowBack;
    ApiService apiService;
    AnimalModel animal;

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
                    Toast.makeText(AnimalDetailActivity.this, "Greška pri dohvaćanju životinje", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AnimalModel> call, Throwable t) {
                Toast.makeText(AnimalDetailActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

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

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}