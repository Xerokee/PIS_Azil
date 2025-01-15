package com.activity.pis_azil.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserByEmailResponseModel;
import com.activity.pis_azil.models.ViewAllModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

import java.io.IOException;

public class NewAnimalsFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_ANIMAL_ID = 1; // Ovdje definiramo RequestAnimalId

    private EditText etName, etDescription, etType, etDob, etColor;
    private ImageView ivAnimalImage;
    private Uri imageUri;
    private LinearLayout animalFormContainer;
    private FloatingActionButton fabAddAnimal;
    private ApiService apiService;
    private ActivityResultLauncher<Intent> galleryLauncher;

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
        etDob = root.findViewById(R.id.editTextDob);
        etColor = root.findViewById(R.id.editTextColor);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        Log.d("NewAnimalsFragment", selectedImage.toString());
                        ivAnimalImage.setImageURI(selectedImage);
                        imageUri = selectedImage;
                    }
                }
        );
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
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void addNewAnimal() {
        String ime_ljubimca = etName.getText().toString().trim();
        String opis_ljubimca = etDescription.getText().toString().trim();
        String tip_ljubimca = etType.getText().toString().trim();
        String dob_ljubimca_str = etDob.getText().toString().trim();
        String boja_ljubimca = etColor.getText().toString().trim();

        Log.d("NewAnimalsFragment", "Uneseni podaci: " +
                "Ime: " + ime_ljubimca +
                ", Opis: " + opis_ljubimca +
                ", Tip: " + tip_ljubimca +
                ", Dob: " + dob_ljubimca_str +
                ", Boja: " + boja_ljubimca);

        if (ime_ljubimca.isEmpty() || opis_ljubimca.isEmpty() || tip_ljubimca.isEmpty() || dob_ljubimca_str.isEmpty() || imageUri == null) {
            Toast.makeText(getContext(), "Molimo ispunite sve podatke", Toast.LENGTH_SHORT).show();
            return;
        }

        int dob_ljubimca;
        try {
            dob_ljubimca = Integer.parseInt(dob_ljubimca_str);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Molimo unesite validan broj za dob", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pretvaranje URI slike u URL (u stvarnom scenariju biste trebali uploadati sliku na server i dobiti URL)
        String imgUrl = imageUri.toString();
        Log.d("NewAnimalsFragment", "Image URL: " + imgUrl);

        ViewAllModel newAnimal = new ViewAllModel(ime_ljubimca, opis_ljubimca, imgUrl, tip_ljubimca, dob_ljubimca, boja_ljubimca);
        Log.d("NewAnimalsFragment", "Podaci koje šaljemo: " + new Gson().toJson(newAnimal));

        SharedPreferences preferences = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = preferences.getInt("id_korisnika", -1);

        apiService.addAnimal(userId, newAnimal).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("NewAnimalsFragment", "Životinja uspješno dodana");
                    Toast.makeText(getContext(), "Životinja uspješno dodana", Toast.LENGTH_SHORT).show();
                    toggleFormVisibility();

                    // Navigacija prema HomeFragment
                    NavHostFragment.findNavController(NewAnimalsFragment.this)
                            .navigate(R.id.nav_home);
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("NewAnimalsFragment", "Greška pri dodavanju životinje: " + response.message() + " - " + errorBody);
                        Toast.makeText(getContext(), "Došlo je do greške: " + response.message(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
        etColor.setText("");
        ivAnimalImage.setImageResource(R.drawable.paw);
    }
}
