package com.activity.pis_azil.fragments;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.activity.pis_azil.R;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.activity.pis_azil.models.UpdateAnimalModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.bumptech.glide.Glide;
import com.orhanobut.dialogplus.DialogPlus;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimalDetailFragment extends Fragment {

    private int animalId;
    private TextView animalName, animalType, animalAge, animalColor, animalDescription;
    private ImageView animalImage;
    private Button animalEdit;
    private ApiService apiService;
    private AnimalModel animal;
    private UserModel currentUser;
    private IsBlockedAnimalModel animalModel;

    public AnimalDetailFragment() {
        // Required empty public constructor
    }

    // Novi konstruktor koji prima animalId
    public AnimalDetailFragment(int animalId, AnimalModel animalModel, UserModel userModel, IsBlockedAnimalModel blockedAnimalModel) {
        this.animalId = animalId;
        this.animal = animalModel;
        this.currentUser = userModel;
        this.animalModel = blockedAnimalModel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getClient().create(ApiService.class);

        // Ako fragment nije dobio animalId kroz konstruktor, provjeri da li postoji u argumentima
        if (getArguments() != null) {
            animalId = getArguments().getInt("animalId", -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalji_zivotinje, container, false);

        animalName = view.findViewById(R.id.animalName);
        animalType = view.findViewById(R.id.animalType);
        animalAge = view.findViewById(R.id.animalAge);
        animalColor = view.findViewById(R.id.animalColor);
        animalDescription = view.findViewById(R.id.animalDescription);
        animalImage = view.findViewById(R.id.animalImage);
        animalEdit = view.findViewById(R.id.animalEdit);

        // Provjera ako animalId postoji i dohvat podataka
        if (animalId != -1) {
            loadAnimalDetails();
        } else {
            Toast.makeText(getContext(), "Neispravan ID životinje", Toast.LENGTH_SHORT).show();
        }

        animalEdit.setOnClickListener(v -> openEditDialog());

        return view;
    }

    private void loadAnimalDetails() {
        apiService.getAnimalById(animalId).enqueue(new Callback<AnimalModel>() {
            @Override
            public void onResponse(Call<AnimalModel> call, Response<AnimalModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    animal = response.body();
                    updateUI();
                } else {
                }
            }

            @Override
            public void onFailure(Call<AnimalModel> call, Throwable t) {
                Toast.makeText(getContext(), "Greška u dohvaćanju podataka.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (animal != null) {
            animalName.setText(animal.getImeLjubimca());
            animalType.setText(animal.getTipLjubimca());
            animalAge.setText(String.valueOf(animal.getDob()));
            animalColor.setText(animal.getBoja());
            animalDescription.setText(animal.getOpisLjubimca());
            Glide.with(this).load(animal.getImgUrl()).into(animalImage);
        }
    }

    private void openEditDialog() {
        final DialogPlus dialogPlus = DialogPlus.newDialog(getContext())
                .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.update_animal))
                .setExpanded(true, 2000)
                .setGravity(Gravity.CENTER)
                .setCancelable(true)
                .create();

        View dialogView = dialogPlus.getHolderView();
        EditText editName = dialogView.findViewById(R.id.editAnimalName);
        EditText editAge = dialogView.findViewById(R.id.editAnimalAge);
        EditText editDescription = dialogView.findViewById(R.id.editAnimalDescription);
        Button saveAnimalEdit = dialogView.findViewById(R.id.saveAnimalEdit);

        editName.setText(animal.getImeLjubimca());
        editAge.setText(String.valueOf(animal.getDob()));
        editDescription.setText(animal.getOpisLjubimca());

        dialogPlus.show();

        saveAnimalEdit.setOnClickListener(v -> {
            if (Objects.equals(editName.getText().toString(), "") || Objects.equals(editAge.getText().toString(), "") || Objects.equals(editDescription.getText().toString(), "")) {
                Toast.makeText(getContext(), "Nisu popunjena sva polja.", Toast.LENGTH_SHORT).show();
            } else {
                UpdateAnimalModel updateAnimal = new UpdateAnimalModel(editName.getText().toString(), editDescription.getText().toString(), Integer.parseInt(editAge.getText().toString()));
                apiService.updateAnimalDetail(animal.getIdLjubimca(), updateAnimal).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Životinja uspješno ažurirana.", Toast.LENGTH_SHORT).show();
                            loadAnimalDetails();
                            dialogPlus.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Greška u API pozivu.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (animalId != -1) {
            loadAnimalDetails();  // Refresh data when fragment is resumed
        }
    }
}
