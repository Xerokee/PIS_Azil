package com.activity.pis_azil.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserModel;
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
        email = findViewById(R.id.login_email);
        lozinka = findViewById(R.id.login_password);
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

        if (userEmail.isEmpty() || userPassword.isEmpty()) {
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
                        String dbPassword = response.body().string().trim();
                        Log.d("LoginActivity", "Database password: " + dbPassword);
                        Log.d("LoginActivity", "Entered password: " + userPassword);
                        if (dbPassword.equals(userPassword)) {
                            Toast.makeText(LoginActivity.this, "Prijava je uspješna!", Toast.LENGTH_SHORT).show();
                            // Fetch user data by email
                            apiService.getUserByIdEmail(userEmail).enqueue(new Callback<UserModel>() {
                                @Override
                                public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        UserModel user = response.body();
                                        Log.d("LoginActivity", "Dohvaćeni podaci korisnika - ID: " + user.getIdKorisnika() + ", Ime: " + user.getIme() + ", Mail: " + user.getEmail() + ", Slika profila: " + user.getProfileImg());
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.putExtra("user_data", user); // pass user data to MainActivity
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Korisnik nije pronađen", Toast.LENGTH_SHORT).show();
                                        Log.e("LoginActivity", "User not found: " + response.errorBody());
                                    }
                                }

                                @Override
                                public void onFailure(Call<UserModel> call, Throwable t) {
                                    Toast.makeText(LoginActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("LoginActivity", "Error fetching user by email: ", t);
                                }
                            });
                        } else {
                            Toast.makeText(LoginActivity.this, "Pogrešna lozinka", Toast.LENGTH_SHORT).show();
                            Log.e("LoginActivity", "Entered password does not match database password.");
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
                Log.e("LoginActivity", "Error fetching password by email: ", t);
            }
        });
    }
}
