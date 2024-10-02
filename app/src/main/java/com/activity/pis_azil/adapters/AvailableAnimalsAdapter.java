package com.activity.pis_azil.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.AnimalModel;
import com.bumptech.glide.Glide;

import java.util.List;

public class AvailableAnimalsAdapter extends RecyclerView.Adapter<AvailableAnimalsAdapter.ViewHolder> {

    private Context context;
    private List<AnimalModel> animalsList;

    public AvailableAnimalsAdapter(Context context, List<AnimalModel> animalsList) {
        this.context = context;
        this.animalsList = animalsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_available_animal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnimalModel animal = animalsList.get(position);

        holder.animalName.setText(animal.getImeLjubimca());
        holder.animalType.setText("Tip: " + animal.getTipLjubimca());

        // Set image using Glide
        Glide.with(context).load(animal.getImgUrl()).into(holder.animalImage);

        // Set status text and background color
        if (animal.isZahtjevUdomljavanja()) {
            holder.animalStatus.setText("Status: Udomljeno");
            holder.itemView.setBackgroundColor(Color.GRAY); // Grey for adopted animals
        } else {
            holder.animalStatus.setText("Status: Dostupno");
            holder.itemView.setBackgroundColor(Color.WHITE); // White for available animals
        }

        // Action button logic
        holder.actionButton.setOnClickListener(v -> {
            // Handle the adoption action or details
        });
    }

    @Override
    public int getItemCount() {
        return animalsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView animalName, animalType, animalStatus;
        ImageView animalImage;
        Button actionButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            animalName = itemView.findViewById(R.id.animal_name);
            animalType = itemView.findViewById(R.id.animal_type);
            animalStatus = itemView.findViewById(R.id.animal_status);
            animalImage = itemView.findViewById(R.id.animal_image);
            actionButton = itemView.findViewById(R.id.action_button);
        }
    }
}
