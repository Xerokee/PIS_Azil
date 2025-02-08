package com.activity.pis_azil.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import com.activity.pis_azil.R;
import com.activity.pis_azil.activities.AnimalDetail2Activity;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.activity.pis_azil.models.MyAdoptionModel;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAnimalsAdapter extends RecyclerView.Adapter<MyAnimalsAdapter.ViewHolder> {
    private Context context;
    private List<UpdateDnevnikModel> animalsList;
    private List<IsBlockedAnimalModel> animalsList2;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    ApiService apiService;
    OnFetchAnimalsCallback callback;

    public interface OnFetchAnimalsCallback{
        void fetchMyAnimals();
    }

    public MyAnimalsAdapter(Context context, List<UpdateDnevnikModel> animalsList, List<IsBlockedAnimalModel> animalsList2, ActivityResultLauncher<Intent> activityResultLauncher, OnFetchAnimalsCallback callback) {
        this.context = context;
        this.animalsList = animalsList;
        this.animalsList2 = animalsList2;
        this.activityResultLauncher = activityResultLauncher;
        this.apiService = ApiClient.getClient().create(ApiService.class);
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_animal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UpdateDnevnikModel animal = animalsList.get(position);
        IsBlockedAnimalModel animal2 = new IsBlockedAnimalModel();
        // Nađi status blokiranja na osnovu ID-a
        boolean isBlocked = false;
        for (IsBlockedAnimalModel blockedAnimal : animalsList2) {
            if (blockedAnimal.getIdLjubimca() == animal.getId_ljubimca() && blockedAnimal.isBlocked()) {
                isBlocked = true;
                break;
            }
        }

        holder.animalName.setText(" " + animal.getIme_ljubimca());
        holder.animalType.setText("  Tip: " + animal.getTip_ljubimca());

        // Set image
        Glide.with(context).load(animal.getImgUrl()).into(holder.animalImage);

        // Set status text and color
        if (animal.isUdomljen()) {
            holder.animalStatus.setText("  Status: Udomljen");
            holder.itemView.setBackgroundColor(Color.GREEN);
            holder.btnReturn.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.GONE);
        } else if (animal.isStatus_udomljavanja()) {
            holder.animalStatus.setText("  Status: Rezerviran");
            holder.itemView.setBackgroundColor(Color.parseColor("#FFA500"));
            holder.animalFrame.setOnClickListener(null);
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnReturn.setVisibility(View.GONE);
        } else if (animal2.isBlocked()) {
            // Obavijest o odbijanju
            Toast.makeText(context, "Administrator je odbio zahtjev za " + animal.getIme_ljubimca(), Toast.LENGTH_LONG).show();
            holder.btnReturn.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);
        }

        holder.btnReturn.setOnClickListener(v -> returnAnimal(animal.getId(), animal, animal.getId_ljubimca()));
        holder.btnCancel.setOnClickListener(v -> onCancelButtonClicked(animal.getId(), animal, animal.getId_ljubimca()));

        holder.animalFrame.setOnClickListener(v -> {
            if (holder.animalFrame.isClickable() && !animal.isStatus_udomljavanja()) {
                Intent i = new Intent(v.getContext().getApplicationContext(), AnimalDetail2Activity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("id", String.valueOf(animal.getId())); // ID životinje
                //i.putExtra("animal", animal); // Cijeli UpdateDnevnikModel objekt
                activityResultLauncher.launch(i);
            } else {
                // Show a message indicating why it can't be clicked
                Toast.makeText(context, "Životinja je rezervirana - nedostupno", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        // Vraća maksimalnu veličinu između dve liste
        return Math.max(animalsList.size(), animalsList2.size());
    }

    private void returnAnimal(int animalId, UpdateDnevnikModel animal, int adoptionId) {
        apiService.updateAdoptionStatus(animalId, adoptionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    notifyDataSetChanged();
                    Toast.makeText(context, "Životinja je vraćena u azil!", Toast.LENGTH_SHORT).show();

                    if (callback != null) {
                        callback.fetchMyAnimals();
                    }
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

    private void onCancelButtonClicked(int animalId, UpdateDnevnikModel animal, int adoptionId) {
        apiService.updateAdoptionStatus(animalId, adoptionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    notifyDataSetChanged();
                    Toast.makeText(context, "Životinja više nije rezervirana!", Toast.LENGTH_SHORT).show();

                    if (callback != null){
                        callback.fetchMyAnimals();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(context, "Greška: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView animalName, animalType, animalStatus;
        ImageView animalImage;
        Button btnReturn, btnCancel;
        ConstraintLayout animalFrame;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            animalName = itemView.findViewById(R.id.animal_name);
            animalType = itemView.findViewById(R.id.animal_type);
            animalStatus = itemView.findViewById(R.id.animal_status);
            animalImage = itemView.findViewById(R.id.animal_image);
            animalFrame = itemView.findViewById(R.id.animal_frame);
            btnReturn = itemView.findViewById(R.id.btn_return);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
        }
    }
}
