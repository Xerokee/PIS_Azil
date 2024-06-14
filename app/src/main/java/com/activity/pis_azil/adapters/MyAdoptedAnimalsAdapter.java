package com.activity.pis_azil.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.R;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.UserModel;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAdoptedAnimalsAdapter extends RecyclerView.Adapter<MyAdoptedAnimalsAdapter.ViewHolder> {
    private Context context;
    private List<AnimalModel> adoptedAnimalsList;
    ApiService apiService;

    public MyAdoptedAnimalsAdapter(Context context, List<AnimalModel> adoptedAnimalsList) {
        this.context = context;
        this.adoptedAnimalsList = adoptedAnimalsList;
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
        AnimalModel animal = adoptedAnimalsList.get(position);
        holder.animalName.setText(animal.getImeLjubimca());
        holder.animalType.setText(animal.getTipLjubimca());
        Glide.with(context).load(animal.getImgUrl()).into(holder.animalImage);

        if (animal.isUdomljen()) {
            holder.tvAdoptedStatus.setText("Udomljeno");
            getAdopterNameById(animal.getIdUdomitelja(), holder.adopterName);
        } else {
            holder.tvAdoptedStatus.setText("Dostupno za udomljavanje");
            holder.adopterName.setText("");
        }

        holder.returnButton.setOnClickListener(v -> {
            int animalId = animal.getIdLjubimca();
            checkIfUserIsAdmin(animalId, position, () -> returnAnimal(animalId, position));
        });
    }

    @Override
    public int getItemCount() {
        return adoptedAnimalsList.size();
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

    private void checkIfUserIsAdmin(int animalId, int position, Runnable onAdminAction) {
        apiService.getUserById(1).enqueue(new Callback<UserModel>() { // Pretpostavimo da je admin provjeren pomoću ID 1
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isAdmin()) {
                    onAdminAction.run();
                } else {
                    showAdminOnlyDialog();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Toast.makeText(context, "Greška pri provjeri statusa admina", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAdminOnlyDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Administratorska Akcija")
                .setMessage("Samo admini mogu vraćati životinje.")
                .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private void getAdopterNameById(int adopterId, final TextView adopterNameTextView) {
        if (adopterId == 0) {
            adopterNameTextView.setText("Nepoznato");
            return;
        }

        apiService.getUserById(adopterId).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adopterNameTextView.setText(response.body().getIme());
                } else {
                    adopterNameTextView.setText("Udomitelj: Nepoznato");
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                adopterNameTextView.setText("Udomitelj: Greška u dohvatu podataka");
            }
        });
    }

    private void returnAnimal(int animalId, int position) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("udomljen", false);
        updateData.put("id_udomitelja", 0);

        apiService.updateAnimal(String.valueOf(animalId), updateData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    adoptedAnimalsList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Životinja je vraćena u azil", Toast.LENGTH_SHORT).show();
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

    public void updateList(List<AnimalModel> newList) {
        adoptedAnimalsList.clear();
        adoptedAnimalsList.addAll(newList);
        notifyDataSetChanged();
    }
}
