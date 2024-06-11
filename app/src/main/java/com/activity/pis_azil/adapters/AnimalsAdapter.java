package com.activity.pis_azil.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.AnimalModel;
import com.bumptech.glide.Glide;

import java.util.List;

public class AnimalsAdapter extends RecyclerView.Adapter<AnimalsAdapter.AnimalViewHolder> {
    private List<AnimalModel> animalsAdapterList;
    private Context context;

    public AnimalsAdapter(List<AnimalModel> animalsAdapterList, Context context) {
        this.animalsAdapterList = animalsAdapterList;
        this.context = context;
    }

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_animal, parent, false);
        return new AnimalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        AnimalModel animal = animalsAdapterList.get(position);
        holder.animalName.setText(animal.getImeLjubimca());
        holder.animalDescription.setText(animal.getOpisLjubimca());

        Log.d("AnimalsAdapter", "Animal name: " + animal.getImeLjubimca());
        Log.d("AnimalsAdapter", "Animal description: " + animal.getOpisLjubimca());

        if (animal.getImgUrl() != null && !animal.getImgUrl().isEmpty()) {
            String imageUrl = animal.getImgUrl();
            if (imageUrl.startsWith("http://192.168.75.1:8000")) {
                imageUrl = imageUrl.replace("http://192.168.75.1:8000", "http://193.198.57.183:7081/Zivotinje");
            }
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.paw) // Placeholder image
                    .error(R.drawable.fruits) // Error image
                    .into(holder.animalImage);
        } else {
            holder.animalImage.setImageResource(R.drawable.milk2); // Default image
        }
    }

    @Override
    public int getItemCount() {
        return animalsAdapterList.size();
    }

    public static class AnimalViewHolder extends RecyclerView.ViewHolder {
        public ImageView animalImage;
        public TextView animalName;
        public TextView animalDescription;

        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            animalImage = itemView.findViewById(R.id.animal_image);
            animalName = itemView.findViewById(R.id.animal_name);
            animalDescription = itemView.findViewById(R.id.animal_description);
        }
    }
}