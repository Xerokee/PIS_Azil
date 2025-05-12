package com.activity.pis_azil.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserByEmailResponseModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import java.io.IOException;
import java.util.concurrent.Executor;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    Button signIn, biometricLogin;
    EditText email, lozinka;
    TextView signUp;
    ApiService apiService;
    ProgressBar progressBar;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        apiService = ApiClient.getClient().create(ApiService.class);

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        signIn = findViewById(R.id.login_btn);
        email = findViewById(R.id.login_email);
        lozinka = findViewById(R.id.login_password);
        signUp = findViewById(R.id.sign_up);
        biometricLogin = findViewById(R.id.biometric_login_btn);

        signUp.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegistrationActivity.class)));

        signIn.setOnClickListener(v -> {
            loginUser();
            progressBar.setVisibility(View.VISIBLE);
        });

        biometricLogin.setOnClickListener(v -> authenticateWithBiometrics());

        checkBiometricSupport();
    }

    private void checkBiometricSupport() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                int userId = preferences.getInt("biometric_user_id", -1);
                if (userId != -1) {
                    biometricLogin.setVisibility(View.VISIBLE);
                } else {
                    biometricLogin.setVisibility(View.GONE);
                }
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "Ovaj uređaj ne podržava biometrijsku autentifikaciju", Toast.LENGTH_SHORT).show();
                biometricLogin.setVisibility(View.GONE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Biometrijski senzor trenutno nije dostupan", Toast.LENGTH_SHORT).show();
                biometricLogin.setVisibility(View.GONE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "Nema registriranih biometrijskih podataka na uređaju", Toast.LENGTH_SHORT).show();
                biometricLogin.setVisibility(View.GONE);
                break;
            default:
                biometricLogin.setVisibility(View.GONE);
                break;
        }
    }

    private void authenticateWithBiometrics() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Biometrijska autentifikacija uspješna!", Toast.LENGTH_SHORT).show();
                    autoLogin();
                });
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Autentifikacija nije uspjela", Toast.LENGTH_SHORT).show());
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometrijska prijava")
                .setSubtitle("Prijavite se pomoću otiska prsta")
                .setNegativeButtonText("Odustani")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void autoLogin() {
        int userId = preferences.getInt("biometric_user_id", 1);
        Log.d("LoginActivity", "Biometric User ID: " + userId);

        if (userId != -1) {
            apiService.getUserById(userId).enqueue(new Callback<UserByEmailResponseModel>() {
                @Override
                public void onResponse(Call<UserByEmailResponseModel> call, Response<UserByEmailResponseModel> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserByEmailResponseModel userByEmailResponseModel = response.body();
                        UserModel user = userByEmailResponseModel.getResult();

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("id_korisnika", user.getIdKorisnika());
                        editor.putString("ime", user.getIme());
                        editor.putString("email", user.getEmail());
                        editor.putBoolean("admin", user.isAdmin());
                        editor.putString("profileImg", user.getProfileImg());
                        editor.putInt("biometric_user_id", user.getIdKorisnika());
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Prijava korisnika " + user.getIme() + " " +  user.getPrezime(), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("korisnikId", String.valueOf(user.getIdKorisnika()));
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Korisnik nije pronađen", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<UserByEmailResponseModel> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Greška prilikom prijave", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Nema spremljenog korisnika za prijavu", Toast.LENGTH_SHORT).show();
        }
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
                            apiService.getUserByIdEmail(userEmail).enqueue(new Callback<UserByEmailResponseModel>() {
                                @Override
                                public void onResponse(Call<UserByEmailResponseModel> call, Response<UserByEmailResponseModel> response) {
                                    progressBar.setVisibility(View.GONE);
                                    if (response.isSuccessful() && response.body() != null) {
                                        UserByEmailResponseModel userByEmailResponseModel = response.body();
                                        UserModel user = userByEmailResponseModel.getResult();
                                        if (user != null) {
                                            Log.d("LoginActivity", "Dohvaćeni podaci korisnika - ID: " + user.getIdKorisnika() + ", Ime: " + user.getIme() + ", Mail: " + user.getEmail() + ", Slika profila: " + user.getProfileImg());

                                            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putInt("id_korisnika", user.getIdKorisnika());
                                            editor.putString("ime", user.getIme());
                                            editor.putString("email", user.getEmail());
                                            editor.putString("lozinka", user.getLozinka());
                                            editor.putBoolean("admin", user.isAdmin());
                                            editor.putString("profileImg", user.getProfileImg());

                                            editor.putInt("biometric_user_id", user.getIdKorisnika());

                                            editor.apply();

                                            checkBiometricSupport();

                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.putExtra("user_data", user); // pass user data to MainActivity
                                            intent.putExtra("korisnikId", String.valueOf(user.getIdKorisnika()));
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Korisnik nije pronađen", Toast.LENGTH_SHORT).show();
                                            Log.e("LoginActivity", "User not found in result");
                                        }
                                    } else {
                                        Log.e("LoginActivity", "Error response: " + response.errorBody().toString());
                                        Toast.makeText(LoginActivity.this, "Korisnik nije pronađen", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<UserByEmailResponseModel> call, Throwable t) {
                                    progressBar.setVisibility(View.GONE);
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