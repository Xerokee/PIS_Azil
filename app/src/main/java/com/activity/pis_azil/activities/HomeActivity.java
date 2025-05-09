package com.activity.pis_azil.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.activity.pis_azil.R;

public class HomeActivity extends AppCompatActivity {

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        /*
        boolean isLoggedIn = preferences.getBoolean("is_logged_in", true);
        int userId = preferences.getInt("id_korisnika", -1);


        if (isLoggedIn && userId != -1) {
            progressBar.setVisibility(View.VISIBLE);
            Intent i = new Intent(HomeActivity.this, MainActivity.class);
            i.putExtra("korisnikId", String.valueOf(userId));
            startActivity(i);
            Toast.makeText(this, "Molimo vas pričekajte, već ste prijavljeni!", Toast.LENGTH_SHORT).show();
            finish();
        }
        */
    }

    public void login(View view) {
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
    }

    public void registration(View view) {
        startActivity(new Intent(HomeActivity.this, RegistrationActivity.class));
    }
}