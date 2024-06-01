package com.activity.pis_azil.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.ApiClient;
import com.activity.pis_azil.ApiService;
import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity {

    Button signUp;
    EditText name, email, password;
    TextView signIn;
    ApiService apiService;
    ProgressBar progressBar;

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
        signIn = findViewById(R.id.sign_in);

        signIn.setOnClickListener(v -> startActivity(new Intent(RegistrationActivity.this, LoginActivity.class)));

        signUp.setOnClickListener(v -> {
            createUser();
            progressBar.setVisibility(View.VISIBLE);
        });
    }

    private void createUser() {
        String userName = name.getText().toString();
        String userEmail = email.getText().toString();
        String userPassword = password.getText().toString();

        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(this, "Ime je prazno!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(this, "Mail je prazan!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(userPassword)) {
            Toast.makeText(this, "Lozinka je prazna!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userPassword.length() < 6) {
            Toast.makeText(this, "Dužina lozinke mora biti veća od 6 slova", Toast.LENGTH_SHORT).show();
            return;
        }

        UserModel userModel = new UserModel();
        userModel.setIme(userName);
        userModel.setEmail(userEmail);
        userModel.setLozinka(userPassword);
        userModel.setAdmin(false);

        apiService.addUser(userModel).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(RegistrationActivity.this, "Registracija je uspješna!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegistrationActivity.this, "Registracija neuspješna: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("RegistrationActivity", "Greška pri registraciji: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RegistrationActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("RegistrationActivity", "Registracija neuspješna", t);
            }
        });
    }
}
