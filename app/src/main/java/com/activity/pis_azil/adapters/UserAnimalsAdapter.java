package com.activity.pis_azil.adapters;

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

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.AnimalModel;
import com.bumptech.glide.Glide;

import java.util.List;

public class UserAnimalsAdapter extends RecyclerView.Adapter<UserAnimalsAdapter.ViewHolder> {

    private Context context;
    private List<AnimalModel> animalList;

    public UserAnimalsAdapter(Context context, List<AnimalModel> animalList) {
        this.context = context;
        this.animalList = animalList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.animal_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnimalModel animal = animalList.get(position);

        holder.nameTextView.setText(animal.getImeLjubimca());
        holder.typeTextView.setText("Tip: " + animal.getTipLjubimca());
        holder.ageTextView.setText("Dob: " + animal.getDob() + " godina");
        holder.colorTextView.setText("Boja: " + animal.getBoja());

        // Postavljanje slike pomoću Glide-a
        if (animal.getImgUrl() != null && !animal.getImgUrl().isEmpty()) {
            Glide.with(context).load(animal.getImgUrl()).into(holder.animalImageView);
        } else {
            holder.animalImageView.setImageResource(R.drawable.profile); // Zadana slika
        }

        // Postavljanje akcije za gumb "Udomi"
        holder.adoptButton.setOnClickListener(v -> {
            Toast.makeText(context, "Odabrali ste: " + animal.getImeLjubimca(), Toast.LENGTH_SHORT).show();
            // Ovdje možete dodati logiku za udomljavanje
        });
    }

    @Override
    public int getItemCount() {
        return animalList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, typeTextView, ageTextView, colorTextView;
        ImageView animalImageView;
        Button adoptButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.animal_name);
            typeTextView = itemView.findViewById(R.id.animal_type);
            ageTextView = itemView.findViewById(R.id.animal_age);
            colorTextView = itemView.findViewById(R.id.animal_color);
            animalImageView = itemView.findViewById(R.id.animal_image);
            adoptButton = itemView.findViewById(R.id.adopt_button);
        }
    }
}
