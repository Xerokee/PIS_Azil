package com.activity.pis_azil.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.models.UserRoleModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateUserActivity extends AppCompatActivity {
    private EditText etName, etSurname, etUsername, etMail, etPassword;
    private RadioGroup radioGroupAdmin;
    private UserModel userToUpdate;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        // Initialize the views
        etName = findViewById(R.id.editTextName);
        etSurname = findViewById(R.id.editTextSurname);
        etUsername = findViewById(R.id.editTextUsername);
        etMail = findViewById(R.id.editTextMail);
        etPassword = findViewById(R.id.editTextPassword);
        radioGroupAdmin = findViewById(R.id.radioGroupAdmin);

        Button btnUpdate = findViewById(R.id.buttonUpdateUser);
        btnUpdate.setOnClickListener(v -> updateUser());

        // Retrieve user from intent
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
        }

        apiService = ApiClient.getClient().create(ApiService.class);
    }

    private void updateUser() {
        String ime_korisnika = etName.getText().toString().trim();
        String prezime_korisnika = etSurname.getText().toString().trim();
        String korisnicko_ime_korisnika = etUsername.getText().toString().trim();
        String mail_korisnika = etMail.getText().toString().trim();
        String lozinka_korisnika = etPassword.getText().toString().trim();

        // Dohvaćanje odabrane admin vrijednosti
        int selectedAdminId = radioGroupAdmin.getCheckedRadioButtonId();
        boolean isAdmin = selectedAdminId == R.id.radioAdminYes;

        if (ime_korisnika.isEmpty() || mail_korisnika.isEmpty() || lozinka_korisnika.isEmpty()) {
            Toast.makeText(this, "Molimo ispunite sve podatke", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ažuriranje podataka korisnika
        userToUpdate.setIme(ime_korisnika);
        userToUpdate.setPrezime(prezime_korisnika);
        userToUpdate.setKorisnickoIme(korisnicko_ime_korisnika);
        userToUpdate.setEmail(mail_korisnika);
        userToUpdate.setLozinka(lozinka_korisnika);

        // Ažuriranje admin statusa
        userToUpdate.setAdmin(isAdmin);

        // Slanje ažuriranih podataka na server
        Map<String, Object> updates = new HashMap<>();
        updates.put("ime", ime_korisnika);
        updates.put("prezime", prezime_korisnika);
        updates.put("korisnickoIme", korisnicko_ime_korisnika);
        updates.put("email", mail_korisnika);
        updates.put("lozinka", lozinka_korisnika);
        updates.put("admin", isAdmin);

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
}
