package com.activity.pis_azil.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.activities.Detailed2Activity;
import com.activity.pis_azil.activities.DetailedActivity;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.bumptech.glide.Glide;

import java.util.List;

public class AnimalsAdapter extends RecyclerView.Adapter<AnimalsAdapter.AnimalViewHolder> {
    private List<IsBlockedAnimalModel> animalsAdapterList;
    private Context context;

    public AnimalsAdapter(List<IsBlockedAnimalModel> animalsAdapterList, Context context) {
        this.animalsAdapterList = animalsAdapterList;
        this.context = context;
    }

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_animal2, parent, false);
        return new AnimalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        Log.i("zivotinje", String.valueOf(animalsAdapterList));
        IsBlockedAnimalModel animal = animalsAdapterList.get(position);
        holder.animalName.setText(" " + animal.getImeLjubimca());
        holder.animalType.setText("  Tip: " + animal.getTipLjubimca());
        holder.animalStatus.setText("  Status: " + "Raspoloživ");

        if (animal.getImgUrl() != null && !animal.getImgUrl().isEmpty()) {
            Glide.with(context)
                    .load(animal.getImgUrl())
                    .placeholder(R.drawable.paw)
                    .error(R.drawable.paw)
                    .into(holder.animalImage);
        } else {
            holder.animalImage.setImageResource(R.drawable.paw);
        }

        if (animalsAdapterList.get(position).isBlocked() == true) {
            holder.animalFrame.setBackgroundResource(R.color.gray);
            holder.animalStatus.setText("  Status: Odbijen");
            //holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.gray));
            holder.itemView.setOnClickListener(null);
        }
        else {
            //holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            holder.animalFrame.setBackgroundResource(R.color.white);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, Detailed2Activity.class);
                intent.putExtra("animal", animal);
                context.startActivity(intent);
            });
        }

        /*if (!animal.isBlocked()) {
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailedActivity.class);
                intent.putExtra("animal", animal);
                context.startActivity(intent);
            });
        }*/
    }

    @Override
    public int getItemCount() {
        return animalsAdapterList.size();
    }

    public static class AnimalViewHolder extends RecyclerView.ViewHolder {
        public ImageView animalImage;
        public TextView animalName;
        public TextView animalType;
        public TextView animalStatus;
        public ConstraintLayout animalFrame;

        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            animalImage = itemView.findViewById(R.id.animal_image);
            animalName = itemView.findViewById(R.id.animal_name);
            animalType = itemView.findViewById(R.id.animal_type);
            animalStatus = itemView.findViewById(R.id.animal_status);
            animalFrame = itemView.findViewById(R.id.animal_frame);
        }
    }
}