package com.activity.pis_azil.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserModel;

public class UpdateUserActivity extends AppCompatActivity {

    private EditText etUserName, etMail, etPassword;
    private UserModel userToUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        etUserName = findViewById(R.id.editTextUsername);
        etMail = findViewById(R.id.editTextMail);
        etPassword = findViewById(R.id.editTextPassword);

        Button btnUpdate = findViewById(R.id.buttonUpdateUser);
        btnUpdate.setOnClickListener(v -> updateUser());

        // Get the User passed from the previous Activity
        userToUpdate = (UserModel) getIntent().getSerializableExtra("user");
        if (userToUpdate != null) {
            etUserName.setText(userToUpdate.getIme());
            etMail.setText(userToUpdate.getEmail());
            etPassword.setText(userToUpdate.getLozinka());
        }
    }

    private void updateUser() {
        String ime_korisnika = etUserName.getText().toString().trim();
        String mail_korisnika = etMail.getText().toString().trim();
        String lozinka_korisnika = etPassword.getText().toString().trim();

        if (ime_korisnika.isEmpty() || mail_korisnika.isEmpty() || lozinka_korisnika.isEmpty()) {
            Toast.makeText(this, "Molimo ispunite sve podatke", Toast.LENGTH_SHORT).show();
            return;
        }

        userToUpdate.setIme(ime_korisnika);
        userToUpdate.setEmail(mail_korisnika);
        userToUpdate.setLozinka(lozinka_korisnika);

        // Poziv API-ja za ažuriranje korisnika

        Toast.makeText(this, "Korisnik uspješno ažuriran", Toast.LENGTH_SHORT).show();
        finish();
    }
}
