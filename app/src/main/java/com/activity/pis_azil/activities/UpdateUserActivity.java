package com.activity.pis_azil.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateUserActivity extends AppCompatActivity {
    private EditText etName, etSurname, etUsername, etMail, etPassword;
    private RadioGroup radioGroupAdmin;
    private ImageView ivUserImage;
    private Uri imageUri;
    private UserModel userToUpdate;
    private ApiService apiService;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        etName = findViewById(R.id.editTextName);
        etSurname = findViewById(R.id.editTextSurname);
        etUsername = findViewById(R.id.editTextUsername);
        etMail = findViewById(R.id.editTextMail);
        etPassword = findViewById(R.id.editTextPassword);
        radioGroupAdmin = findViewById(R.id.radioGroupAdmin);
        ivUserImage = findViewById(R.id.imageViewUser);

        Button btnUpdate = findViewById(R.id.buttonUpdateUser);
        btnUpdate.setOnClickListener(v -> updateUser());

        userToUpdate = (UserModel) getIntent().getSerializableExtra("user");
        if (userToUpdate != null) {
            etName.setText(userToUpdate.getIme());
            etSurname.setText(userToUpdate.getPrezime());
            etUsername.setText(userToUpdate.getKorisnickoIme());
            etMail.setText(userToUpdate.getEmail());
            etPassword.setText(userToUpdate.getLozinka());

            if (userToUpdate.isAdmin()) {
                radioGroupAdmin.check(R.id.radioAdminYes);
            } else {
                radioGroupAdmin.check(R.id.radioAdminNo);
            }

            if (userToUpdate.getProfileImg() != null && !userToUpdate.getProfileImg().isEmpty()) {
                Glide.with(this)
                        .load(userToUpdate.getProfileImg())
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .into(ivUserImage);
            } else {
                ivUserImage.setImageResource(R.drawable.ic_baseline_person_24);
            }
        }

        apiService = ApiClient.getClient().create(ApiService.class);

        ivUserImage.setOnClickListener(v -> openImageChooser());
    }

    private void updateUser() {
        String ime_korisnika = etName.getText().toString().trim();
        String prezime_korisnika = etSurname.getText().toString().trim();
        String korisnicko_ime_korisnika = etUsername.getText().toString().trim();
        String mail_korisnika = etMail.getText().toString().trim();
        String lozinka_korisnika = etPassword.getText().toString().trim();

        int selectedAdminId = radioGroupAdmin.getCheckedRadioButtonId();
        boolean isAdmin = selectedAdminId == R.id.radioAdminYes;

        if (ime_korisnika.isEmpty() || mail_korisnika.isEmpty() || lozinka_korisnika.isEmpty()) {
            Toast.makeText(this, "Molimo ispunite sve podatke", Toast.LENGTH_SHORT).show();
            return;
        }

        userToUpdate.setIme(ime_korisnika);
        userToUpdate.setPrezime(prezime_korisnika);
        userToUpdate.setKorisnickoIme(korisnicko_ime_korisnika);
        userToUpdate.setEmail(mail_korisnika);
        userToUpdate.setLozinka(lozinka_korisnika);
        userToUpdate.setAdmin(isAdmin);

        if (imageUri != null) {
            userToUpdate.setProfileImg(imageUri.toString());
            Log.d("UpdateUserActivity", "Updated image URI: " + imageUri.toString());
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("ime", ime_korisnika);
        updates.put("prezime", prezime_korisnika);
        updates.put("korisnickoIme", korisnicko_ime_korisnika);
        updates.put("email", mail_korisnika);
        updates.put("lozinka", lozinka_korisnika);
        updates.put("admin", isAdmin);
        updates.put("profileImg", imageUri != null ? imageUri.toString() : null);

        apiService.updateUser(1, userToUpdate.getIdKorisnika(), updates)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(UpdateUserActivity.this, "Korisnik uspješno ažuriran", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(UpdateUserActivity.this, "Pogreška pri ažuriranju korisnika", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(UpdateUserActivity.this, "Greška pri mrežnoj vezi", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                Log.d("UpdateUserActivity", "Selected image: " + imageUri.toString());
                Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .into(ivUserImage);
            }
        }
    }
}
