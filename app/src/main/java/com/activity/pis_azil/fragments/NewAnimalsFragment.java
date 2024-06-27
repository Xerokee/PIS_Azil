package com.activity.pis_azil.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserByEmailResponseModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.models.ViewAllModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class NewAnimalsFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etName, etDescription, etType;
    private ImageView ivAnimalImage;
    private Uri imageUri;
    private LinearLayout animalFormContainer;
    private FloatingActionButton fabAddAnimal;
    private ApiService apiService;

    public NewAnimalsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_new_animals, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);

        etName = root.findViewById(R.id.editTextName);
        etDescription = root.findViewById(R.id.editTextDescription);
        etType = root.findViewById(R.id.editTextType);
        ivAnimalImage = root.findViewById(R.id.imageViewAnimal);
        animalFormContainer = root.findViewById(R.id.animalFormContainer);

        fabAddAnimal = root.findViewById(R.id.fabAddAnimal);
        fabAddAnimal.setOnClickListener(v -> {
            Log.d("NewAnimalsFragment", "Floating Action Button clicked");
            toggleFormVisibility();
        });

        ivAnimalImage.setOnClickListener(v -> openImageChooser());

        Button btnSubmitAnimal = root.findViewById(R.id.buttonSubmitAnimal);
        btnSubmitAnimal.setOnClickListener(v -> addNewAnimal());

        checkIfUserIsAdmin();

        return root;
    }

    private void toggleFormVisibility() {
        if (animalFormContainer.getVisibility() == View.GONE) {
            Log.d("NewAnimalsFragment", "Prikazivanje obrasca");
            animalFormContainer.setVisibility(View.VISIBLE);
        } else {
            Log.d("NewAnimalsFragment", "Skrivanje obrasca");
            animalFormContainer.setVisibility(View.GONE);
            clearForm();
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Odaberite sliku"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(ivAnimalImage);
        }
    }

    private void addNewAnimal() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String type = etType.getText().toString().trim();

        Log.d("NewAnimalsFragment", "Uneseni podaci: " +
                "Ime: " + name +
                ", Opis: " + description +
                ", Tip: " + type);

        if (name.isEmpty() || description.isEmpty() || type.isEmpty() || imageUri == null) {
            Toast.makeText(getContext(), "Molimo ispunite sve podatke", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pretvaranje URI slike u URL (u stvarnom scenariju biste trebali uploadati sliku na server i dobiti URL)
        String imgUrl = imageUri.toString();

        ViewAllModel newAnimal = new ViewAllModel(name, description, "N/A", imgUrl, type);
        apiService.addAnimal(newAnimal).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("NewAnimalsFragment", "Životinja uspješno dodana");
                    Toast.makeText(getContext(), "Životinja uspješno dodana", Toast.LENGTH_SHORT).show();
                    toggleFormVisibility();
                } else {
                    Log.e("NewAnimalsFragment", "Greška pri dodavanju životinje: " + response.message());
                    Toast.makeText(getContext(), "Došlo je do greške: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("NewAnimalsFragment", "Greška pri dodavanju životinje: " + t.getMessage());
                Toast.makeText(getContext(), "Došlo je do greške: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfUserIsAdmin() {
        SharedPreferences preferences = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = preferences.getInt("id_korisnika", -1);

        if (userId == -1) {
            Log.e("NewAnimalsFragment", "User ID not found");
            fabAddAnimal.setVisibility(View.GONE);
            return;
        }

        apiService.getUserById(userId).enqueue(new Callback<UserByEmailResponseModel>() {
            @Override
            public void onResponse(Call<UserByEmailResponseModel> call, Response<UserByEmailResponseModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult().isAdmin()) {
                    fabAddAnimal.setVisibility(View.VISIBLE);
                } else {
                    fabAddAnimal.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<UserByEmailResponseModel> call, Throwable t) {
                Log.e("NewAnimalsFragment", "Error getting user admin status", t);
                fabAddAnimal.setVisibility(View.GONE);
            }
        });
    }


    private void clearForm() {
        etName.setText("");
        etDescription.setText("");
        etType.setText("");
        ivAnimalImage.setImageResource(R.drawable.fruits);
    }
}
