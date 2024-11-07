package com.activity.pis_azil.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.SlikeAdapter;
import com.activity.pis_azil.fragments.MyAnimalsFragment;
import com.activity.pis_azil.models.Aktivnost;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.RejectAdoptionModelRead;
import com.activity.pis_azil.models.SlikaModel;
import com.activity.pis_azil.models.UpdateAnimalModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.network.HttpRequestResponse;
import com.activity.pis_azil.network.HttpRequestResponseList;
import com.bumptech.glide.Glide;
import com.orhanobut.dialogplus.DialogPlus;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimalDetailActivity extends AppCompatActivity {

    String sAnimalId;
    int animalId;
    TextView animalName, animalType, animalAge, animalColor, animalDescription, tvNemaAktivnosti;
    Button animalEdit;
    ImageView animalImage, arrowBack;
    ApiService apiService;
    AnimalModel animal;
    private static final int SELECT_IMAGE_CODE = 1;
    List<Aktivnost> listaAktivnosti = new ArrayList<>();
    LinearLayout linearLayoutAktivnosti;
    ImageButton addAktivnost, addSlika;
    List<SlikaModel> listaSlika = new ArrayList<>();
    RecyclerView rvSlike;
    SlikeAdapter slikeAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_detail);

        final Bundle oExtras= getIntent().getExtras();
        sAnimalId= oExtras.getString("id");
        animalId= Integer.parseInt(sAnimalId);

        animalName=findViewById(R.id.animalName);
        animalType=findViewById(R.id.animalType);
        animalAge=findViewById(R.id.animalAge);
        animalColor=findViewById(R.id.animalColor);
        animalDescription=findViewById(R.id.animalDescription);
        animalEdit=findViewById(R.id.animalEdit);
        animalImage=findViewById(R.id.animalImage);
        arrowBack = findViewById(R.id.arrowBack);
        tvNemaAktivnosti= findViewById(R.id.tvNemaAktivnosti);
        linearLayoutAktivnosti = findViewById(R.id.linearLayoutAktivnosti);
        addAktivnost = findViewById(R.id.addAktivnost);
        addSlika = findViewById(R.id.addSlika);
        rvSlike= findViewById(R.id.rvSlike);

        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAnimalById(animalId).enqueue(new Callback<AnimalModel>() {
            @Override
            public void onResponse(Call<AnimalModel> call, Response<AnimalModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    animal = response.body();
                    animalName.setText(animal.getImeLjubimca());
                    animalType.setText(animal.getTipLjubimca());
                    animalAge.setText(String.valueOf(animal.getDob()));
                    animalColor.setText(animal.getBoja());
                    animalDescription.setText(animal.getOpisLjubimca());
                    Glide.with(AnimalDetailActivity.this).load(animal.getImgUrl()).into(animalImage);
                } else {
                    // Toast.makeText(AnimalDetailActivity.this, "Greška pri dohvaćanju životinje", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AnimalModel> call, Throwable t) {
                Toast.makeText(AnimalDetailActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        refreshAnimalDetails();
        refreshPopisAktivnosti();
        initializeRecyclerView();
        getSlike();

        animalEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogPlus dialogPlus = DialogPlus.newDialog(v.getContext())
                        .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.update_animal))
                        .setExpanded(true,2000)
                        .setGravity(Gravity.CENTER)
                        .setCancelable(true)
                        .create();

                View view = dialogPlus.getHolderView();
                EditText editName= view.findViewById(R.id.editAnimalName);
                EditText editAge= view.findViewById(R.id.editAnimalAge);
                EditText editDescription= view.findViewById(R.id.editAnimalDescription);
                Button saveAnimalEdit = view.findViewById(R.id.saveAnimalEdit);

                editName.setText(animal.getImeLjubimca());
                editAge.setText(String.valueOf(animal.getDob()));
                editDescription.setText(animal.getOpisLjubimca());

                dialogPlus.show();

                saveAnimalEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Objects.equals(editName.getText().toString(),"") || Objects.equals(editAge.getText().toString(),"") || Objects.equals(editDescription.getText().toString(),"")){
                            Toast.makeText(v.getContext(), "Nisu popunjena sve polja.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            UpdateAnimalModel updateAnimal = new UpdateAnimalModel(editName.getText().toString(), editDescription.getText().toString(), Integer.parseInt(editAge.getText().toString()));
                            apiService.updateAnimalDetail(animal.getIdLjubimca(), updateAnimal).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(AnimalDetailActivity.this, "Životinja uspješno ažurirana.", Toast.LENGTH_SHORT).show();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialogPlus.dismiss();
                                                animalName.setText(editName.getText().toString());
                                                animalAge.setText(editAge.getText().toString());
                                                animalDescription.setText(editDescription.getText().toString());
                                            }
                                        }, 3000);
                                    } else {
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(AnimalDetailActivity.this, "Greška u API pozivu.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });

        addAktivnost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogPlus dialogPlus = DialogPlus.newDialog(v.getContext())
                        .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.add_activity))
                        .setExpanded(true,1000)
                        .setGravity(Gravity.CENTER)
                        .setCancelable(true)
                        .create();

                View view = dialogPlus.getHolderView();
                TextView inputDateActivity = view.findViewById(R.id.inputDateActivity);
                Button addActivity = view.findViewById(R.id.addActivity);
                Button closeActivity = view.findViewById(R.id.closeActivity);
                EditText inputDescriptionActivity = view.findViewById(R.id.inputDescriptionActivity);

                dialogPlus.show();

                closeActivity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inputDescriptionActivity.setText("");
                        dialogPlus.dismiss();
                    }
                });

                addActivity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Objects.equals(inputDateActivity.getText().toString(), "") || Objects.equals(inputDescriptionActivity.getText().toString(), "")){
                            Toast.makeText(AnimalDetailActivity.this, "Moraju biti popunjeni svi podaci.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Aktivnost novaAktivnost = new Aktivnost(0, animalId, inputDateActivity.getText().toString(), inputDescriptionActivity.getText().toString());
                            apiService.addAktivnost(novaAktivnost).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(AnimalDetailActivity.this, "Aktivnost uspješno dodana.", Toast.LENGTH_SHORT).show();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialogPlus.dismiss();
                                                inputDescriptionActivity.setText("");
                                                refreshPopisAktivnosti();
                                            }
                                        }, 3000);
                                    } else {
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(AnimalDetailActivity.this, "Greška u API pozivu.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

                inputDateActivity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                AnimalDetailActivity.this,
                                (view1, selectedYear, selectedMonth, selectedDay) -> {
                                    String selectedDate =  String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear);
                                    inputDateActivity.setText(selectedDate);
                                },
                                year, month, day);
                        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                        datePickerDialog.show();
                    }
                });
            }
        });

        addSlika.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, SELECT_IMAGE_CODE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ponovno učitavanje podataka kada se aktivnost pojavi na ekranu
        refreshAnimalDetails();
        refreshPopisAktivnosti();
        getSlike();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void refreshAnimalDetails() {
        apiService.getAnimalById(animalId).enqueue(new Callback<AnimalModel>() {
            @Override
            public void onResponse(Call<AnimalModel> call, Response<AnimalModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    animal = response.body();
                    animalName.setText(animal.getImeLjubimca());
                    animalType.setText(animal.getTipLjubimca());
                    animalAge.setText(String.valueOf(animal.getDob()));
                    animalColor.setText(animal.getBoja());
                    animalDescription.setText(animal.getOpisLjubimca());
                    Glide.with(AnimalDetailActivity.this).load(animal.getImgUrl()).into(animalImage);
                } else {
                    // Toast.makeText(AnimalDetailActivity.this, "Greška pri dohvaćanju podataka o životinji.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AnimalModel> call, Throwable t) {
                Toast.makeText(AnimalDetailActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void refreshPopisAktivnosti(){
        apiService.getAktivnostiById(animalId).enqueue(new Callback<HttpRequestResponseList<Aktivnost>>() {
            @Override
            public void onResponse(Call<HttpRequestResponseList<Aktivnost>> call, Response<HttpRequestResponseList<Aktivnost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaAktivnosti = response.body().getResult();
                    if (listaAktivnosti.size() == 0){
                        tvNemaAktivnosti.setVisibility(View.VISIBLE);
                    }
                    else{
                        linearLayoutAktivnosti.removeAllViews();
                        for (Aktivnost a : listaAktivnosti){
                            TextView tv = new TextView(getApplicationContext());
                            String text = a.getDatum() + " " + a.getOpis();
                            tv.setText(text);
                            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
                            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            tv.setTextSize(17);
                            tv.setTextColor(Color.parseColor("#FFFFFF"));
                            linearLayoutAktivnosti.addView(tv);
                        }
                    }
                } else {
                    // Toast.makeText(AnimalDetailActivity.this, "Greška pri dohvaćanju aktivnosti", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HttpRequestResponseList<Aktivnost>> call, Throwable t) {
                Toast.makeText(AnimalDetailActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null){
                addImage(selectedImageUri);
            }
        }
    }

    private void addImage(Uri imageUri){
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imagePath = cursor.getString(columnIndex);
            cursor.close();

            File file = new File(imagePath);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            apiService.addSlika(animalId, body).enqueue (new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AnimalDetailActivity.this, "Uspješno dodana slika.", Toast.LENGTH_SHORT).show();
                        getSlike();
                    } else {
                        // Toast.makeText(AnimalDetailActivity.this, "Greška pri dodavanju slike..", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(AnimalDetailActivity.this, "Greška u API pozivu.", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void initializeRecyclerView() {
        slikeAdapter = new SlikeAdapter(this, listaSlika);
        rvSlike.setAdapter(slikeAdapter);
        rvSlike.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void getSlike(){
        apiService.getSlikeById(animalId).enqueue(new Callback<HttpRequestResponseList<SlikaModel>>() {
            @Override
            public void onResponse(Call<HttpRequestResponseList<SlikaModel>> call, Response<HttpRequestResponseList<SlikaModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaSlika.clear();
                    listaSlika = response.body().getResult();
                    slikeAdapter.notifyDataSetChanged();
                    if (listaSlika.size() == 0){
                        //Log.i("getSlike","Nema dodanih slika");
                    }
                    else{
                        slikeAdapter = new SlikeAdapter(AnimalDetailActivity.this, listaSlika);
                        rvSlike.setAdapter(slikeAdapter);
                        rvSlike.setLayoutManager(new LinearLayoutManager(AnimalDetailActivity.this, LinearLayoutManager.HORIZONTAL, false));

                        //Log.i("getSlike",String.valueOf(listaSlika));
                        //Log.i("getSlika",String.valueOf(listaSlika.get(0).slika_data));
                        //ImageView ivSlika = findViewById(R.id.ivSlika);

                        //byte[] decodedString = Base64.decode(listaSlika.get(0).slika_data, Base64.DEFAULT);
                        //Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        //ivSlika.setImageBitmap(decodedBitmap);
                    }
                } else {
                    // Toast.makeText(AnimalDetailActivity.this, "Greška pri dohvaćanju slika", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HttpRequestResponseList<SlikaModel>> call, Throwable t) {
                Toast.makeText(AnimalDetailActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}