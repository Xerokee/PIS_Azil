package com.activity.pis_azil.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.SendMail;
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

    public MyAdoptedAnimalsAdapter(Context context, List<UpdateDnevnikModel> filteredAdoptedAnimalsList) {
        this.context = context;
        this.filteredAdoptedAnimalsList  = filteredAdoptedAnimalsList ;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_adopted_animal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UpdateDnevnikModel animal = filteredAdoptedAnimalsList.get(position);
        holder.animalName.setText(animal.getIme_ljubimca());
        holder.animalType.setText(animal.getTip_ljubimca());
        Glide.with(context).load(animal.getImgUrl()).into(holder.animalImage);

        if (animal.isUdomljen()) {
            holder.tvAdoptedStatus.setText("Udomljeno");
            String adopterName = animal.getImeUdomitelja();
            if (adopterName == null || adopterName.isEmpty()) {
                getAdopterNameById(animal.getId_korisnika(), holder.adopterName, animal); // Ovaj metod će postaviti ime i u holder i u model
            } else {
                holder.adopterName.setText(adopterName);
            }
        } else {
            holder.tvAdoptedStatus.setText("Dostupno za udomljavanje");
            holder.adopterName.setText("");
        }

        // Provjeri da li je korisnik admin i sakrij gumb ako nije
        if (!checkIfUserIsAdmin()) {
            holder.returnButton.setVisibility(View.GONE);
        } else {
            holder.returnButton.setVisibility(View.VISIBLE);
            holder.returnButton.setOnClickListener(v -> returnAnimal(animal, position));
        }
    }

    @Override
    public int getItemCount() {
        return filteredAdoptedAnimalsList != null ? filteredAdoptedAnimalsList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView animalName, animalType, tvAdoptedStatus, adopterName;
        ImageView animalImage;
        Button returnButton;

        public ViewHolder(View itemView) {
            super(itemView);
            animalName = itemView.findViewById(R.id.textViewAnimalName);
            animalType = itemView.findViewById(R.id.textViewAnimalType);
            animalImage = itemView.findViewById(R.id.imageViewAnimal);
            tvAdoptedStatus = itemView.findViewById(R.id.tvAdoptedStatus);
            adopterName = itemView.findViewById(R.id.textViewAdopterName);
            returnButton = itemView.findViewById(R.id.returnButton);
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

    private void getAdopterNameById(int adopterId, final TextView adopterNameTextView, UpdateDnevnikModel animal) {
        if (adopterId == 0) {
            adopterNameTextView.setText("Nepoznato");
            animal.setImeUdomitelja("Nepoznato");
            notifyDataSetChanged(); // Osvježi adapter nakon ažuriranja modela
            return;
        }

        apiService.getUserById(adopterId).enqueue(new Callback<UserByEmailResponseModel>() {
            @Override
            public void onResponse(Call<UserByEmailResponseModel> call, Response<UserByEmailResponseModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String imeUdomitelja = response.body().getResult().getIme();
                    adopterNameTextView.setText(imeUdomitelja);
                    animal.setImeUdomitelja(imeUdomitelja);
                    notifyDataSetChanged(); // Osvježi adapter nakon ažuriranja modela
                } else {
                    adopterNameTextView.setText("Udomitelj: Nepoznato");
                    animal.setImeUdomitelja("Nepoznato");
                    notifyDataSetChanged(); // Osvježi adapter nakon ažuriranja modela
                }
            }

            @Override
            public void onFailure(Call<UserByEmailResponseModel> call, Throwable t) {
                adopterNameTextView.setText("Udomitelj: Greška u dohvatu podataka");
                animal.setImeUdomitelja("Greška u dohvatu podataka");
                notifyDataSetChanged(); // Osvježi adapter nakon ažuriranja modela
            }
        });
    }

    private void returnAnimal(UpdateDnevnikModel animal, int position) {
        animal.setId_korisnika(0);
        animal.setUdomljen(false);

        apiService.updateAdoption(1, animal.getId_ljubimca(), animal).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    filteredAdoptedAnimalsList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Životinja je vraćena u azil", Toast.LENGTH_SHORT).show();
                    String subject = "Životinja je vraćena!";
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
                    }).start();
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