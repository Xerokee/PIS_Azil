package com.activity.pis_azil.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserModel;

public class NewUserActivity extends AppCompatActivity {

    private EditText etUserName, etMail, etPassword;
    private ImageView ivUserImage;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        etUserName = findViewById(R.id.editTextUsername);
        etMail = findViewById(R.id.editTextMail);
        etPassword = findViewById(R.id.editTextPassword);
        ivUserImage = findViewById(R.id.imageViewUser);

        Button btnSubmitUser = findViewById(R.id.buttonSubmitUser);
        btnSubmitUser.setOnClickListener(v -> addNewUser());

        ivUserImage.setOnClickListener(v -> openImageChooser());
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void addNewUser() {
        String ime_korisnika = etUserName.getText().toString().trim();
        String mail_korisnika = etMail.getText().toString().trim();
        String lozinka_korisnika = etPassword.getText().toString().trim();

        if (ime_korisnika.isEmpty() || mail_korisnika.isEmpty() || lozinka_korisnika.isEmpty()) {
            Toast.makeText(this, "Molimo ispunite sve podatke", Toast.LENGTH_SHORT).show();
            return;
        }

        UserModel newUser = new UserModel();
        newUser.setIme(ime_korisnika);
        newUser.setEmail(mail_korisnika);
        newUser.setLozinka(lozinka_korisnika);

        // Poziv API-ja za dodavanje korisnika

        // Nakon uspješnog dodavanja korisnika, završite aktivnost
        Toast.makeText(this, "Korisnik uspješno dodan", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            ivUserImage.setImageURI(data.getData());
        }
    }
}
