package com.activity.pis_azil.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserModel;
import com.google.gson.Gson;

import org.jetbrains.annotations.Nullable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;

public class RegistrationActivity extends AppCompatActivity {

    Button signUp;
    EditText name, email, password;
    ImageView profileImg;
    TextView signIn;
    ApiService apiService;
    ProgressBar progressBar;
    private Uri imageUri;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int IMAGE_PICK_GALLERY_CODE = 101;
    private static final int IMAGE_PICK_CAMERA_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        apiService = ApiClient.getClient().create(ApiService.class);

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        signUp = findViewById(R.id.reg_btn);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email_reg);
        password = findViewById(R.id.password_reg);
        profileImg = findViewById(R.id.profile_img);
        signIn = findViewById(R.id.sign_in);

        signIn.setOnClickListener(v -> startActivity(new Intent(RegistrationActivity.this, LoginActivity.class)));

        profileImg.setOnClickListener(v -> showImagePickDialog());

        signUp.setOnClickListener(v -> {
            createUser();
            progressBar.setVisibility(View.VISIBLE);
        });
    }

    private void showImagePickDialog() {
        String[] options = {"Kamere", "Galerije"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Odaberite sliku iz");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    pickFromCamera();
                }
            } else {
                pickFromGallery();
            }
        });
        builder.create().show();
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result && result1;
    }

    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // Pozivamo super.onActivityResult

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                imageUri = data != null ? data.getData() : null;
                if (imageUri != null) {
                    profileImg.setImageURI(imageUri);
                }
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                profileImg.setImageURI(imageUri);
            }
        }
    }

    private void createUser() {
        String userName = name.getText().toString();
        String userEmail = email.getText().toString();
        String userPassword = password.getText().toString();

        // Provjera praznih polja
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(userPassword)) {
            Toast.makeText(this, "Sva polja su obavezna", Toast.LENGTH_SHORT).show();
            return;
        }

        UserModel userModel = new UserModel();
        userModel.setIme(userName);
        userModel.setEmail(userEmail);
        userModel.setLozinka(userPassword);
        userModel.setAdmin(false);
        if (imageUri != null) {
            userModel.setProfileImg(imageUri.toString());
        }

        Log.d("RegistrationActivity", "Slanje podataka: " + new Gson().toJson(userModel));

        int requestUserId = 1; // Ovdje stavite odgovarajući ID korisnika koji pravi zahtjev

        apiService.addUser(requestUserId, userModel).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(RegistrationActivity.this, "Registracija je uspješna!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                    finish();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(RegistrationActivity.this, "Registracija neuspješna: " + response.code() + " " + errorBody, Toast.LENGTH_SHORT).show();
                    Log.e("RegistrationActivity", "Greška pri registraciji: " + response.code() + " " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                String errorMessage = t.getMessage();
                Toast.makeText(RegistrationActivity.this, "Greška: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e("RegistrationActivity", "Registracija neuspješna: " + errorMessage, t);
            }
        });
    }
}
