package com.activity.pis_azil.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.SendMail;
import com.activity.pis_azil.activities.AdoptedAnimalDetailActivity;
import com.activity.pis_azil.fragments.MyAdoptedFragment;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.models.UserByEmailResponseModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.R;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import javax.mail.MessagingException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAdoptedAnimalsAdapter extends RecyclerView.Adapter<MyAdoptedAnimalsAdapter.ViewHolder> {
    private Context context;
    private List<UpdateDnevnikModel> filteredAdoptedAnimalsList;
    ApiService apiService;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private MyAdoptedFragment fragment;

    public MyAdoptedAnimalsAdapter(Context context, List<UpdateDnevnikModel> filteredAdoptedAnimalsList, ActivityResultLauncher<Intent> activityResultLauncher, MyAdoptedFragment fr) {
        this.context = context;
        this.filteredAdoptedAnimalsList  = filteredAdoptedAnimalsList ;
        this.apiService = ApiClient.getClient().create(ApiService.class);
        this.activityResultLauncher = activityResultLauncher;
        this.fragment = fr;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_adopted_animal2, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UpdateDnevnikModel animal = filteredAdoptedAnimalsList.get(position);
        holder.animalName.setText(" " + animal.getIme_ljubimca());
        holder.animalType.setText(" " + animal.getTip_ljubimca());
        Glide.with(context).load(animal.getImgUrl()).into(holder.animalImage);

        if (animal.isUdomljen()) {
            holder.tvAdoptedStatus.setText(" " + "Udomljen");
            String adopterName = animal.getImeUdomitelja();
            if (adopterName == null || adopterName.isEmpty()) {
                getAdopterNameById(animal.getId_korisnika(), holder.adopterName, holder.adopterSurname, animal);
            } else {
                holder.adopterName.setText(" " + adopterName);
            }
        }

        // Provjeri da li je korisnik admin i sakrij gumb ako nije
        if (!checkIfUserIsAdmin()) {
            holder.returnButton.setVisibility(View.GONE);
        } else {
            holder.returnButton.setVisibility(View.VISIBLE);
            holder.returnButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    returnAnimal(animal.getId(), animal, animal.getId_ljubimca());
                }
            });
        }

        holder.animalFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext().getApplicationContext(), AdoptedAnimalDetailActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("id", String.valueOf(animal.getId()));
                i.putExtra("udomitelj", holder.adopterName.getText());
                activityResultLauncher.launch(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredAdoptedAnimalsList != null ? filteredAdoptedAnimalsList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView animalName, animalType, tvAdoptedStatus, adopterName, adopterSurname;
        ImageView animalImage;
        Button returnButton;
        ConstraintLayout animalFrame;

        public ViewHolder(View itemView) {
            super(itemView);
            animalName = itemView.findViewById(R.id.textViewAnimalName);
            animalType = itemView.findViewById(R.id.textViewAnimalType);
            animalImage = itemView.findViewById(R.id.imageViewAnimal);
            tvAdoptedStatus = itemView.findViewById(R.id.tvAdoptedStatus);
            adopterName = itemView.findViewById(R.id.textViewAdopterName);
            adopterSurname = itemView.findViewById(R.id.textViewAdopterSurname);
            returnButton = itemView.findViewById(R.id.returnButton);
            animalFrame = itemView.findViewById(R.id.animal_frame);
        }
    }

    private boolean checkIfUserIsAdmin() {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);
        if (userJson != null) {
            Gson gson = new Gson();
            UserModel currentUser = gson.fromJson(userJson, UserModel.class);
            return currentUser != null && currentUser.isAdmin();
        }
        return false;
    }

    private void getAdopterNameById(int adopterId, final TextView adopterNameTextView, final TextView adopterSurnameTextView, UpdateDnevnikModel animal) {
        if (adopterId == 0) {
            adopterNameTextView.setText("Nepoznato");
            adopterSurnameTextView.setText("Nepoznato");
            animal.setImeUdomitelja("Nepoznato");
            animal.setPrezimeUdomitelja("Nepoznato");
            notifyDataSetChanged(); // Osvježi adapter nakon ažuriranja modela
            return;
        }

        apiService.getUserById(adopterId).enqueue(new Callback<UserByEmailResponseModel>() {
            @Override
            public void onResponse(Call<UserByEmailResponseModel> call, Response<UserByEmailResponseModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String imeUdomitelja = response.body().getResult().getIme();
                    String prezimeUdomitelja = response.body().getResult().getPrezime();
                    adopterNameTextView.setText(imeUdomitelja);
                    adopterSurnameTextView.setText(prezimeUdomitelja);
                    animal.setImeUdomitelja(imeUdomitelja);
                    notifyDataSetChanged(); // Osvježi adapter nakon ažuriranja modela
                } else {
                    adopterNameTextView.setText("Udomitelj: Nepoznato");
                    adopterSurnameTextView.setText("Udomitelj: Nepoznato");
                    animal.setImeUdomitelja("Nepoznato");
                    animal.setPrezimeUdomitelja("Nepoznato");
                    notifyDataSetChanged(); // Osvježi adapter nakon ažuriranja modela
                }
            }

            @Override
            public void onFailure(Call<UserByEmailResponseModel> call, Throwable t) {
                adopterNameTextView.setText("Udomitelj: Greška u dohvatu podataka");
                adopterSurnameTextView.setText("Udomitelj: Greška u dohvatu podataka");
                animal.setImeUdomitelja("Greška u dohvatu podataka");
                animal.setPrezimeUdomitelja("Greška u dohvatu podataka");
                notifyDataSetChanged(); // Osvježi adapter nakon ažuriranja modela
            }
        });
    }

    private void returnAnimal(int animalId, UpdateDnevnikModel animal, int adoptionId) {
        //animal.setId_korisnika(0);
        //animal.setUdomljen(false);

        apiService.updateAdoptionStatus(animalId, adoptionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    //update list
                    //filteredAdoptedAnimalsList.remove(position);
                    //notifyItemRemoved(position);
                    Toast.makeText(context, "Životinja je vraćena u azil", Toast.LENGTH_SHORT).show();
                    /*String subject = "Životinja je vraćena!";
                    String body = "Poštovani, životinja " + animal.getIme_ljubimca() + " je vraćena!";

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                SendMail.sendEmail("margeta.matija@gmail.com", subject, body);
                            } catch (GeneralSecurityException e) {
                                throw new RuntimeException(e);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (MessagingException e) {
                                throw new RuntimeException(e);
                            }
                            System.out.println("Send email...");
                        }
                    }).start();*/
                    fragment.refreshAdoptedAnimals();
                } else {
                    Toast.makeText(context, "Greška pri vraćanju: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Greška pri vraćanju: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}