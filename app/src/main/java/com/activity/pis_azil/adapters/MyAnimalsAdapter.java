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
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.bumptech.glide.Glide;

import java.util.List;

public class MyAnimalsAdapter extends RecyclerView.Adapter<MyAnimalsAdapter.ViewHolder> {
    private Context context;
    private List<UpdateDnevnikModel> animalsList;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    public MyAnimalsAdapter(Context context, List<UpdateDnevnikModel> animalsList, ActivityResultLauncher<Intent> activityResultLauncher) {
        this.context = context;
        this.animalsList = animalsList;
        this.activityResultLauncher = activityResultLauncher;
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
        } else {
            holder.btnReturn.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);
        }

        holder.btnReturn.setOnClickListener(v -> onReturnButtonClicked(animal, position));
        holder.btnCancel.setOnClickListener(v -> onCancelButtonClicked(animal, position));

        holder.animalFrame.setOnClickListener(v -> {
            if (holder.animalFrame.isClickable() && !animal.isStatus_udomljavanja()) {
                Intent i = new Intent(v.getContext().getApplicationContext(), AnimalDetail2Activity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("id", String.valueOf(animal.getId())); // ID životinje
                i.putExtra("animal", animal); // Cijeli UpdateDnevnikModel objekt
                activityResultLauncher.launch(i);
            } else {
                // Show a message indicating why it can't be clicked
                Toast.makeText(context, "Životinja je rezervirana - nedostupno", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return animalsList.size();
    }

    private void onReturnButtonClicked(UpdateDnevnikModel animal, int position) {
        animal.setUdomljen(false); // Set Udomljen to false
        animalsList.remove(position); // Remove the animal from the list
        notifyItemRemoved(position); // Notify the adapter that the item is removed

        String status = animal.isUdomljen() ? "nije vraćena" : "vraćena";
        Toast.makeText(context, "Životinja je : " + status, Toast.LENGTH_SHORT).show();
    }

    private void onCancelButtonClicked(UpdateDnevnikModel animal, int position) {
        animal.setZahtjev_udomljen(false); // Set Status Udomljavanja to false
        animalsList.remove(position); // Remove the animal from the list
        notifyItemRemoved(position); // Notify the adapter that the item is removed

        String status = animal.isZahtjev_udomljen() ? "rezervina" : "nije rezervirana";
        Toast.makeText(context, "Životinja više " + status, Toast.LENGTH_SHORT).show();
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
