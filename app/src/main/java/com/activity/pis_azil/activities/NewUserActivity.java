package com.activity.pis_azil.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewUserActivity extends AppCompatActivity {

    private EditText etName, etSurname, etUsername, etMail, etPassword;
    private ImageView ivUserImage;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        etName = findViewById(R.id.editTextName);
        etSurname = findViewById(R.id.editTextSurname);
        etUsername = findViewById(R.id.editTextUsername);
        etMail = findViewById(R.id.editTextMail);
        etPassword = findViewById(R.id.editTextPassword);
        ivUserImage = findViewById(R.id.imageViewUser);

        Button btnSubmitUser = findViewById(R.id.buttonSubmitUser);
        btnSubmitUser.setOnClickListener(v -> addNewUser());

        ivUserImage.setOnClickListener(v -> openImageChooser());

        apiService = ApiClient.getClient().create(ApiService.class);
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void addNewUser() {
        String ime_korisnika = etName.getText().toString().trim();
        String prezime_korisnika = etSurname.getText().toString().trim();
        String korisnicko_ime_korisnika = etUsername.getText().toString().trim();
        String mail_korisnika = etMail.getText().toString().trim();
        String lozinka_korisnika = etPassword.getText().toString().trim();

        if (ime_korisnika.isEmpty() || mail_korisnika.isEmpty() || lozinka_korisnika.isEmpty()) {
            Toast.makeText(this, "Molimo ispunite sve podatke", Toast.LENGTH_SHORT).show();
            return;
        }

        String profileImgUri = selectedImageUri != null ? selectedImageUri.toString() : null;

        UserModel newUser = new UserModel();
        newUser.setIme(ime_korisnika);
        newUser.setPrezime(prezime_korisnika);
        newUser.setKorisnickoIme(korisnicko_ime_korisnika);
        newUser.setEmail(mail_korisnika);
        newUser.setLozinka(lozinka_korisnika);
        newUser.setProfileImg(profileImgUri);

        apiService.addUser(1, newUser)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(NewUserActivity.this, "Korisnik uspješno dodan", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(NewUserActivity.this, "Pogreška pri dodavanju korisnika", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(NewUserActivity.this, "Greška u mreži", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ivUserImage.setImageURI(selectedImageUri);
        }
    }
}
