package com.activity.pis_azil.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.activity.pis_azil.R;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    Button signIn;
    EditText email, lozinka;
    TextView signUp;
    ApiService apiService;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = ApiClient.getClient().create(ApiService.class);

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        signIn = findViewById(R.id.login_btn);
        email = findViewById(R.id.email_login);
        lozinka = findViewById(R.id.password_login);
        signUp = findViewById(R.id.sign_up);

        signUp.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegistrationActivity.class)));

        signIn.setOnClickListener(v -> {
            loginUser();
            progressBar.setVisibility(View.VISIBLE);
        });
    }

    private void loginUser() {
        String userEmail = email.getText().toString().trim();
        String userPassword = lozinka.getText().toString().trim();

        if (TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(userPassword)) {
            Toast.makeText(this, "Sva polja su obavezna", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        Log.d("LoginActivity", "Login attempt with email: " + userEmail);

        apiService.getPasswordByEmail(userEmail).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String dbPassword = response.body().string().trim();  // Pristupite rezultatu lozinke
                        Log.d("LoginActivity", "Database password: " + dbPassword);
                        Log.d("LoginActivity", "Entered password: " + userPassword);
                        if (dbPassword.equals(userPassword)) {
                            Toast.makeText(LoginActivity.this, "Prijava je uspješna!", Toast.LENGTH_SHORT).show();
                            // Save user data to SharedPreferences
                            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("user_email", userEmail);
                            // Save the user ID (assuming you get it in the response or another call)
                            editor.putInt("id_korisnika", 1);
                            editor.apply();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        } else {
                            Toast.makeText(LoginActivity.this, "Pogrešna lozinka", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e("LoginActivity", "Error parsing response body", e);
                        Toast.makeText(LoginActivity.this, "Greška u parsiranju odgovora", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("LoginActivity", "Error response: " + response.errorBody().toString());
                    Toast.makeText(LoginActivity.this, "Korisnik nije pronađen", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
