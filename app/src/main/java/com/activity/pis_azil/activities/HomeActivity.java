package com.activity.pis_azil.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.activity.pis_azil.MainActivity;
import com.activity.pis_azil.R;

public class HomeActivity extends AppCompatActivity {

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        // Pretpostavljamo da korisnik može biti prijavljen putem neke druge metode
        if (true /* Zamijeni ovdje s provjerom za prijavu korisnika */) {
            progressBar.setVisibility(View.VISIBLE);
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            Toast.makeText(this, "Molimo vas pričekajte, već ste prijavljeni!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void login(View view) {
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
    }

    public void registration(View view) {
        startActivity(new Intent(HomeActivity.this, RegistrationActivity.class));
    }
}
