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
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

import java.io.IOException;

public class NewUsersFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_USER_ID = 1;
    private EditText etName, etSurname, etUsername, etMail, etPassword;
    private ImageView ivUserImage;
    private Uri imageUri;
    private LinearLayout userFormContainer;
    private FloatingActionButton fabAddAnimal;
    private ApiService apiService;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public NewUsersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_new_users, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);

        etName = root.findViewById(R.id.editTextName);
        etSurname = root.findViewById(R.id.editTextSurname);
        etUsername = root.findViewById(R.id.editTextUsername);
        etMail = root.findViewById(R.id.editTextMail);
        etPassword = root.findViewById(R.id.editTextPassword);
        ivUserImage = root.findViewById(R.id.imageViewUser);
        userFormContainer = root.findViewById(R.id.userFormContainer);

        fabAddAnimal = root.findViewById(R.id.fabAddAnimal);
        fabAddAnimal.setOnClickListener(v -> {
            Log.d("NewUsersFragment", "Floating Action Button clicked");
            toggleFormVisibility();
        });

        ivUserImage.setOnClickListener(v -> openImageChooser());

        Button btnSubmitUser = root.findViewById(R.id.buttonSubmitUser);
        btnSubmitUser.setOnClickListener(v -> addNewUser());

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
                        Log.d("NewUsersFragment", selectedImage.toString());
                        ivUserImage.setImageURI(selectedImage);
                        imageUri = selectedImage;
                    }
                }
        );
    }

    private void toggleFormVisibility() {
        if (userFormContainer.getVisibility() == View.GONE) {
            Log.d("NewUsersFragment", "Prikazivanje obrasca");
            userFormContainer.setVisibility(View.VISIBLE);
        } else {
            Log.d("NewAnimalsFragment", "Skrivanje obrasca");
            userFormContainer.setVisibility(View.GONE);
            clearForm();
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void addNewUser() {
        String ime_korisnika = etName.getText().toString().trim();
        String prezime_korisnika = etSurname.getText().toString().trim();
        String korisnicko_ime_korisnika = etUsername.getText().toString().trim();
        String mail_korisnika = etMail.getText().toString().trim();
        String lozinka_korisnika = etPassword.getText().toString().trim();

        Log.d("NewUsersFragment", "Uneseni podaci: " +
                "Ime korisnika: " + ime_korisnika +
                "Prezime korisnika: " + prezime_korisnika +
                "Korisničko ime korisnika: " + korisnicko_ime_korisnika +
                ", Mail korisnka: " + mail_korisnika +
                ", Lozinka korisnika: " + lozinka_korisnika);

        if (ime_korisnika.isEmpty() || mail_korisnika.isEmpty() || lozinka_korisnika.isEmpty() || imageUri == null) {
            Toast.makeText(getContext(), "Molimo ispunite sve podatke", Toast.LENGTH_SHORT).show();
            return;
        }

        String imgUrl = imageUri.toString();
        Log.d("NewUsersFragment", "Image URL: " + imgUrl);

        UserModel newUser = new UserModel();
        Log.d("NewUsersFragment", "Podaci koje šaljemo: " + new Gson().toJson(newUser));

        SharedPreferences preferences = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = preferences.getInt("id_korisnika", -1);

        apiService.addUser(userId, newUser).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("NewUsersFragment", "Korisnik uspješno dodan");
                    Toast.makeText(getContext(), "Korisnik uspješno dodan", Toast.LENGTH_SHORT).show();
                    toggleFormVisibility();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("NewUsersFragment", "Greška pri dodavanju korisnika: " + response.message() + " - " + errorBody);
                        Toast.makeText(getContext(), "Došlo je do greške: " + response.message(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("NewUsersFragment", "Greška pri dodavanju korisnika: " + t.getMessage());
                Toast.makeText(getContext(), "Došlo je do greške: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfUserIsAdmin() {
        SharedPreferences preferences = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = preferences.getInt("id_korisnika", -1);

        if (userId == -1) {
            Log.e("NewUsersFragment", "User ID not found");
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
                Log.e("NewUsersFragment", "Error getting user admin status", t);
                fabAddAnimal.setVisibility(View.GONE);
            }
        });
    }

    private void clearForm() {
        etName.setText("");
        etSurname.setText("");
        etName.setText("");
        etMail.setText("");
        etPassword.setText("");
        ivUserImage.setImageResource(R.drawable.ic_baseline_person_24);
    }
}
