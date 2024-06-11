package com.activity.pis_azil.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.models.ViewAllModel;
import com.activity.pis_azil.network.ApiService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewAnimalsFragment extends Fragment {

    private EditText etName, etDescription, etRating, etImgUrl, etType;
    private LinearLayout animalFormContainer;
    private FloatingActionButton fabAddAnimal;
    ApiService apiService;

    public NewAnimalsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_new_animals, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);

        etName = root.findViewById(R.id.editTextName);
        etDescription = root.findViewById(R.id.editTextDescription);
        etRating = root.findViewById(R.id.editTextRating);
        etImgUrl = root.findViewById(R.id.editTextImgUrl);
        etType = root.findViewById(R.id.editTextType);
        animalFormContainer = root.findViewById(R.id.animalFormContainer);

        fabAddAnimal = root.findViewById(R.id.fabAddAnimal);
        fabAddAnimal.setOnClickListener(v -> toggleFormVisibility());

        Button btnSubmitAnimal = root.findViewById(R.id.buttonSubmitAnimal);
        btnSubmitAnimal.setOnClickListener(v -> addNewAnimal());

        checkIfUserIsAdmin();

        return root;
    }

    private void toggleFormVisibility() {
        if (animalFormContainer.getVisibility() == View.GONE) {
            Log.d("NewAnimalsFragment", "Prikazivanje obrasca");
            animalFormContainer.setVisibility(View.VISIBLE);
        } else {
            Log.d("NewAnimalsFragment", "Skrivanje obrasca");
            animalFormContainer.setVisibility(View.GONE);
            clearForm();
        }
    }

    private void addNewAnimal() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String rating = etRating.getText().toString().trim();
        String img_url = etImgUrl.getText().toString().trim();
        String type = etType.getText().toString().trim();

        Log.d("NewAnimalsFragment", "Uneseni podaci: " +
                "Ime: " + name +
                ", Opis: " + description +
                ", Ocjena: " + rating +
                ", URL slike: " + img_url +
                ", Tip: " + type);

        if (name.isEmpty() || description.isEmpty() || rating.isEmpty() || type.isEmpty()) {
            Toast.makeText(getContext(), "Molimo ispunite sve podatke", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Float.parseFloat(rating);
        } catch (NumberFormatException e) {
            Log.e("NewAnimalsFragment", "Greška pri provjeri ocjene: " + e.getMessage());
            Toast.makeText(getContext(), "Ocjena mora biti broj", Toast.LENGTH_SHORT).show();
            return;
        }

        ViewAllModel newAnimal = new ViewAllModel(name, description, rating, img_url, type);
        apiService.addAnimal(newAnimal).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("NewAnimalsFragment", "Životinja uspješno dodana");
                    Toast.makeText(getContext(), "Životinja uspješno dodana", Toast.LENGTH_SHORT).show();
                    toggleFormVisibility();
                } else {
                    Log.e("NewAnimalsFragment", "Greška pri dodavanju životinje: " + response.message());
                    Toast.makeText(getContext(), "Došlo je do greške: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("NewAnimalsFragment", "Greška pri dodavanju životinje: " + t.getMessage());
                Toast.makeText(getContext(), "Došlo je do greške: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfUserIsAdmin() {
        apiService.getUserById(123, 123).enqueue(new Callback<UserModel>() { // Pretpostavimo da je admin provjeren pomoću ID 1
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isAdmin()) {
                    fabAddAnimal.setVisibility(View.VISIBLE);
                } else {
                    fabAddAnimal.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Log.e("NewAnimalsFragment", "Error getting user admin status", t);
                fabAddAnimal.setVisibility(View.GONE);
            }
        });
    }

    private void clearForm() {
        etName.setText("");
        etDescription.setText("");
        etRating.setText("");
        etImgUrl.setText("");
        etType.setText("");
    }
}
