package com.activity.pis_azil.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.SifrBojaLjubimca;
import com.activity.pis_azil.models.UserByEmailResponseModel;
import com.activity.pis_azil.models.ViewAllModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.activity.pis_azil.models.SifrTipLjubimca;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewAnimalsFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_ANIMAL_ID = 1; // Ovdje definiramo RequestAnimalId

    private EditText etName, etDescription, etDob, etColor;
    private List<SifrTipLjubimca> animalTypesList = new ArrayList<>(); // Lista za tipove ljubimaca
    private List<SifrBojaLjubimca> animalColorsList = new ArrayList<>(); // Lista za tipove ljubimaca
    private ArrayAdapter<String> spinnerAdapter; // Adapter za spinner
    private ImageView ivAnimalImage;
    private Uri imageUri;
    private LinearLayout animalFormContainer;
    private FloatingActionButton fabAddAnimal;
    private ApiService apiService;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public NewAnimalsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_new_animals, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);

        etName = root.findViewById(R.id.editTextName);
        etDescription = root.findViewById(R.id.editTextDescription);
        etDob = root.findViewById(R.id.editTextDob);
        ivAnimalImage = root.findViewById(R.id.imageViewAnimal);
        animalFormContainer = root.findViewById(R.id.animalFormContainer);

        // Pronalazak spinnera za tip životinje
        Spinner spinnerAnimalType = root.findViewById(R.id.spinnerAnimalType);
        // Pronalazak spinnera za boju životinje
        Spinner spinnerAnimalColor = root.findViewById(R.id.spinnerAnimalColor);

        spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAnimalType.setAdapter(spinnerAdapter);

        // Postavljanje adaptera za tipove životinja
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.animal_types3,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAnimalType.setAdapter(adapter);

        // Postavljanje adaptera za boje
        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.animal_colors2,
                android.R.layout.simple_spinner_item
        );
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAnimalColor.setAdapter(colorAdapter);

        // Pozivanje metode za učitavanje tipova iz API-ja
        fetchAnimalTypes();
        fetchAnimalColors();

        // Dobivanje odabranog tipa
        spinnerAnimalType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Odabir psa ili mačke sa int vrednostima
                int selectedType = position; // 0: Tip životinje, 1: Pas, 2: Mačka
                if (selectedType == 1) {
                    Log.d("SpinnerSelection", "Odabrali ste psa (int: 1)");
                } else if (selectedType == 2) {
                    Log.d("SpinnerSelection", "Odabrali ste mačku (int: 2)");
                } else {
                    Log.w("SpinnerSelection", "Odabrali ste nevažeći tip (int: " + selectedType + ")");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.w("SpinnerSelection", "Nije odabran tip ljubimca.");
            }
        });

        // Dobivanje odabrane boje
        spinnerAnimalColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Odabir psa ili mačke sa int vrednostima
                int selectedColor = position; // 0: Boja životinje, 1: Crna, 2: Bijela, 3. Smeđa
                if (selectedColor == 1) {
                    Log.d("SpinnerSelection", "Odabrali ste crnu boju (int: 1)");
                } else if (selectedColor == 2) {
                    Log.d("SpinnerSelection", "Odabrali ste bijelu boju (int: 2)");
                } else if (selectedColor == 3) {
                    Log.d("SpinnerSelection", "Odabrali ste smeđu boju (int: 3)");
                } else {
                    Log.w("SpinnerSelection", "Odabrali ste nevažeći tip (int: " + selectedColor + ")");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.w("SpinnerSelection", "Nije odabrana boja.");
            }
        });

        fabAddAnimal = root.findViewById(R.id.fabAddAnimal);
        fabAddAnimal.setOnClickListener(v -> {
            Log.d("NewAnimalsFragment", "Floating Action Button clicked");
            toggleFormVisibility();
        });

        ivAnimalImage.setOnClickListener(v -> openImageChooser());

        Button btnSubmitAnimal = root.findViewById(R.id.buttonSubmitAnimal);
        btnSubmitAnimal.setOnClickListener(v -> addNewAnimal());

        checkIfUserIsAdmin();

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        Log.d("NewAnimalsFragment", selectedImage.toString());
                        ivAnimalImage.setImageURI(selectedImage);
                        imageUri = selectedImage;
                    }
                }
        );
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

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void fetchAnimalTypes() {
        apiService.getAnimalTypes().enqueue(new Callback<List<SifrTipLjubimca>>() {
            @Override
            public void onResponse(Call<List<SifrTipLjubimca>> call, Response<List<SifrTipLjubimca>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    animalTypesList.clear();
                    animalTypesList.addAll(response.body());

                    // Dodavanje naziva tipova u spinner adapter
                    List<String> typeNames = new ArrayList<>();
                    for (SifrTipLjubimca type : animalTypesList) {
                        typeNames.add(type.getNaziv());
                    }
                    spinnerAdapter.clear();
                    spinnerAdapter.addAll(typeNames);
                    spinnerAdapter.notifyDataSetChanged();
                } else {
                    Log.e("NewAnimalsFragment", "Greška pri dohvaćanju tipova: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<SifrTipLjubimca>> call, Throwable t) {
                Log.e("NewAnimalsFragment", "Greška pri dohvaćanju tipova", t);
            }
        });
    }

    private void fetchAnimalColors() {
        apiService.getAnimalColors().enqueue(new Callback<List<SifrBojaLjubimca>>() {
            @Override
            public void onResponse(Call<List<SifrBojaLjubimca>> call, Response<List<SifrBojaLjubimca>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    animalColorsList.clear();
                    animalColorsList.addAll(response.body());

                    // Dodavanje naziva boja u spinner adapter
                    List<String> colorNames = new ArrayList<>();
                    for (SifrBojaLjubimca color : animalColorsList) {
                        colorNames.add(color.getNaziv());
                    }
                    spinnerAdapter.clear();
                    spinnerAdapter.addAll(colorNames);
                    spinnerAdapter.notifyDataSetChanged();
                } else {
                    Log.e("NewAnimalsFragment", "Greška pri dohvaćanju tipova: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<SifrBojaLjubimca>> call, Throwable t) {
                Log.e("NewAnimalsFragment", "Greška pri dohvaćanju tipova", t);
            }
        });
    }

    private void addNewAnimal() {
        String ime_ljubimca = etName.getText().toString().trim();
        String opis_ljubimca = etDescription.getText().toString().trim();
        String dob_ljubimca_str = etDob.getText().toString().trim();

        // Dohvati odabrani tip životinje iz spinnera
        Spinner spinnerAnimalType = getView().findViewById(R.id.spinnerAnimalType);
        String selectedAnimalType = spinnerAnimalType.getSelectedItem().toString();
        // Dohvati odabranu boju iz spinnera
        Spinner spinnerAnimalColor = getView().findViewById(R.id.spinnerAnimalColor);
        String selectedAnimalColor = spinnerAnimalColor.getSelectedItem().toString();

        // Mapiranje naziva tipova na numeričke vrijednosti
        int tip_ljubimca = 3;
        if (selectedAnimalType.equals("Pas")) {
            tip_ljubimca = 1;
        } else if (selectedAnimalType.equals("Mačka")) {
            tip_ljubimca = 2;
        } else {
            tip_ljubimca = 3; // Ako nije odabran valjani tip
        }

        Log.d("MappedValue", "Mapped tip_ljubimca: " + tip_ljubimca);
        Log.d("NewAnimalsFragment", "Tip životinje (int): " + tip_ljubimca);

        if (ime_ljubimca.isEmpty() || opis_ljubimca.isEmpty() || dob_ljubimca_str.isEmpty() || imageUri == null) {
            Toast.makeText(getContext(), "Molimo ispunite sve podatke", Toast.LENGTH_SHORT).show();
            return;
        }

        // Provjera da li je validan tip
        if (selectedAnimalType.equals("Tip životinje")) {
            Toast.makeText(getContext(), "Molimo odaberite tip životinje", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mapiranje naziva tipova na numeričke vrijednosti
        int boja_ljubimca = 4;
        if (selectedAnimalColor.equals("Crna")) {
            boja_ljubimca = 1;
        } else if (selectedAnimalColor.equals("Bijela")) {
            boja_ljubimca = 2;
        } else if (selectedAnimalColor.equals("Smeđa")) {
            boja_ljubimca = 3;
        } else {
            boja_ljubimca = 4; // Ako nije odabran valjani tip
        }

        Log.d("MappedValue", "Mapped boja_ljubimca: " + boja_ljubimca);
        Log.d("NewAnimalsFragment", "Boja životinje (int): " + boja_ljubimca);

        // Provjeri je li boja odabrana
        if (selectedAnimalColor.equals("Boja životinje")) {
            Toast.makeText(getContext(), "Molimo odaberite boju životinje", Toast.LENGTH_SHORT).show();
            return;
        }

        int dob_ljubimca;
        try {
            dob_ljubimca = Integer.parseInt(dob_ljubimca_str);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Molimo unesite validan broj za dob", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pretvaranje URI slike u URL
        String imgUrl = imageUri.toString();
        Log.d("NewAnimalsFragment", "Image URL: " + imgUrl);

        ViewAllModel newAnimal = new ViewAllModel(ime_ljubimca, opis_ljubimca, imgUrl, tip_ljubimca, dob_ljubimca, boja_ljubimca);
        Log.d("NewAnimalsFragment", "Podaci koje šaljemo: " + new Gson().toJson(newAnimal));

        SharedPreferences preferences = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = preferences.getInt("id_korisnika", -1);

        apiService.addAnimal(userId, newAnimal).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("NewAnimalsFragment", "Životinja uspješno dodana");
                    Toast.makeText(getContext(), "Životinja uspješno dodana", Toast.LENGTH_SHORT).show();
                    toggleFormVisibility();

                    // Navigacija prema HomeFragment
                    NavHostFragment.findNavController(NewAnimalsFragment.this)
                            .navigate(R.id.nav_home);
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("NewAnimalsFragment", "Greška pri dodavanju životinje: " + response.message() + " - " + errorBody);
                        Toast.makeText(getContext(), "Došlo je do greške: " + response.message(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e("NewAnimalsFragment", "Greška pri čitanju errorBody: " + e.getMessage());
                        e.printStackTrace();
                    }
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
        SharedPreferences preferences = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = preferences.getInt("id_korisnika", -1);

        if (userId == -1) {
            Log.e("NewAnimalsFragment", "User ID not found");
            fabAddAnimal.setVisibility(View.GONE);
            return;
        }

        apiService.getUserById(userId).enqueue(new Callback<UserByEmailResponseModel>() {
            @Override
            public void onResponse(Call<UserByEmailResponseModel> call, Response<UserByEmailResponseModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult().isAdmin()) {
                    fabAddAnimal.setVisibility(View.VISIBLE);
                } else {
                    fabAddAnimal.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<UserByEmailResponseModel> call, Throwable t) {
                Log.e("NewAnimalsFragment", "Error getting user admin status", t);
                fabAddAnimal.setVisibility(View.GONE);
            }
        });
    }

    private void clearForm() {
        etName.setText("");
        etDescription.setText("");
        ivAnimalImage.setImageResource(R.drawable.paw);
    }
}
