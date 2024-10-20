package com.activity.pis_azil.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.ImageGalleryAdapter;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.google.gson.Gson;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditAnimalDialogFragment extends DialogFragment {
    private UpdateDnevnikModel animal;
    private EditText editName;
    private Button addImageButton, addActivityButton, saveButton;
    private RecyclerView imageGalleryRecyclerView;
    private List<String> imageGallery;
    private ImageGalleryAdapter imageGalleryAdapter;

    public static EditAnimalDialogFragment newInstance(UpdateDnevnikModel animal) {
        EditAnimalDialogFragment fragment = new EditAnimalDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("animal", animal);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_animal, container, false);

        animal = (UpdateDnevnikModel) getArguments().getSerializable("animal");
        editName = view.findViewById(R.id.edit_animal_name);
        addImageButton = view.findViewById(R.id.add_image_button);
        addActivityButton = view.findViewById(R.id.add_activity_button);
        saveButton = view.findViewById(R.id.save_button);
        imageGalleryRecyclerView = view.findViewById(R.id.image_gallery_recyclerview);

        imageGallery = animal.getGalleryImgUrls();
        if (imageGallery == null) {
            imageGallery = new ArrayList<>();
        }

        imageGalleryAdapter = new ImageGalleryAdapter(getContext(), imageGallery);
        imageGalleryRecyclerView.setAdapter(imageGalleryAdapter);
        imageGalleryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Postavi trenutno ime ljubimca
        editName.setText(animal.getIme_ljubimca());

        addImageButton.setOnClickListener(v -> {
            // Dodaj sliku u galeriju
            addImageToGallery();
        });

        addActivityButton.setOnClickListener(v -> {
            // Dodaj aktivnost za životinju
            addActivity();
        });

        saveButton.setOnClickListener(v -> {
            // Sačuvaj promene i zatvori dijalog
            saveChanges();
            dismiss();
        });

        return view;
    }

    private static final int PICK_IMAGE_REQUEST = 1;

    private void addImageToGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Odaberite sliku"), PICK_IMAGE_REQUEST);
    }

    private void addActivity() {
        // Prikaži dijalog za unos nove aktivnosti
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Dodaj novu aktivnost");

        final EditText input = new EditText(getContext());
        input.setHint("Unesite opis aktivnosti");
        builder.setView(input);

        builder.setPositiveButton("Dodaj", (dialog, which) -> {
            String newActivity = input.getText().toString().trim();
            if (!newActivity.isEmpty()) {
                // Dodaj novu aktivnost u model životinje
                animal.addActivity(newActivity);
                Toast.makeText(getContext(), "Aktivnost dodana", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Otkaži", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveChanges() {
        // Ažuriraj ime životinje
        animal.setIme_ljubimca(editName.getText().toString().trim());

        animal.setGalleryImgUrls(imageGallery);

        if (animal.getId_ljubimca() <= 0) {
            if (isAdded() && getActivity() != null) {
                Toast.makeText(getActivity(), "Greška: Nevažeći ID životinje", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        UpdateDnevnikModel animalForUpdate = new UpdateDnevnikModel();
        animalForUpdate.setId_ljubimca(animal.getId_ljubimca());
        animalForUpdate.setId_korisnika(animal.getId_korisnika());
        animalForUpdate.setIme_ljubimca(animal.getIme_ljubimca());
        animalForUpdate.setTip_ljubimca(animal.getTip_ljubimca());
        animalForUpdate.setUdomljen(animal.isUdomljen());
        animalForUpdate.setDatum(animal.getDatum());
        animalForUpdate.setVrijeme(animal.getVrijeme());
        animalForUpdate.setImgUrl(animal.getImgUrl());
        animalForUpdate.setStanje_zivotinje(animal.isStanje_zivotinje());
        animalForUpdate.setStatus_udomljavanja(animal.isStatus_udomljavanja());
        animalForUpdate.setZahtjev_udomljen(animal.isZahtjev_udomljen());
        animalForUpdate.setGalleryImgUrls(animal.getGalleryImgUrls());

        // API poziv za ažuriranje podataka o životinji putem DnevnikUdomljavanja
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        int requestAnimalId = animal.getId_ljubimca();
        int animalId = animal.getId_ljubimca(); // Ili koristite drugi ispravan ID ako treba

        Log.d("API_REQUEST", "RequestAnimalId: " + requestAnimalId);
        Log.d("API_REQUEST", "AnimalId: " + animalId);
        Log.d("API_REQUEST", "Animal: " + new Gson().toJson(animal)); // Ovo zahteva `Gson` zavisnost da pretvori objekat u JSON.

        // Dodavanje headera i Path parametara kako je predviđeno
        apiService.updateAdoption(requestAnimalId, animalId, animalForUpdate).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (isAdded() && getActivity() != null) {
                        Toast.makeText(getActivity(), "Podaci o životinji su uspešno ažurirani", Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                } else {
                    if (isAdded() && getActivity() != null) {
                        Toast.makeText(getActivity(), "Greška pri ažuriranju podataka: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded() && getActivity() != null) {
                    Toast.makeText(getActivity(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}