package com.activity.pis_azil.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;

public class RegistrationActivity extends AppCompatActivity {

    Button signUp;
    EditText ime, email, lozinka;
    ImageView regProfileImg;
    TextView signIn;
    ApiService apiService;
    ProgressBar progressBar;
    private Uri imageUri;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private static final int CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        apiService = ApiClient.getClient().create(ApiService.class);

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        signUp = findViewById(R.id.reg_btn);
        ime = findViewById(R.id.reg_name);
        email = findViewById(R.id.reg_email);
        lozinka = findViewById(R.id.reg_password);
        regProfileImg = findViewById(R.id.regProfileImg);
        signIn = findViewById(R.id.sign_in);

        signIn.setOnClickListener(v -> startActivity(new Intent(RegistrationActivity.this, LoginActivity.class)));

        regProfileImg.setOnClickListener(v -> showImagePickDialog());

        signUp.setOnClickListener(v -> {
            createUser();
            progressBar.setVisibility(View.VISIBLE);
        });

        // Inicijalizacija ActivityResultLauncher-a
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            try (Cursor cursor = getContentResolver().query(selectedImage, null, null, null, null)) {
                                if (cursor != null && cursor.moveToFirst()) {
                                    int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                                    if (columnIndex != -1) {
                                        long imageId = cursor.getLong(columnIndex);
                                        imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(imageId));
                                        regProfileImg.setImageURI(imageUri);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Došlo je do greške pri odabiru slike", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        regProfileImg.setImageURI(imageUri);
                    }
                }
        );

        // Inicijalizacija ActivityResultLauncher-a za traženje dozvola
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Dozvola za čitanje pohrane nije dodijeljena", Toast.LENGTH_SHORT).show();
                    }
                }
        );
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
                checkGalleryPermission();
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
        cameraLauncher.launch(cameraIntent);
    }

    private void checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            pickFromGallery();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void createUser() {
        String userName = ime.getText().toString();
        String userEmail = email.getText().toString();
        String userPassword = lozinka.getText().toString();

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
            String realPath = getRealPathFromURI(imageUri);
            if (realPath != null) {
                userModel.setProfileImg(realPath);
            } else {
                userModel.setProfileImg(imageUri.toString());
            }
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

    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromCamera();
            } else {
                Toast.makeText(this, "Dozvola za kameru nije dodijeljena", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
