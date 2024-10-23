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
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.activities.AnimalDetailActivity;
import com.activity.pis_azil.fragments.EditAnimalDialogFragment;
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
        holder.animalName.setText(animal.getIme_ljubimca());
        holder.animalType.setText("Tip: " + animal.getTip_ljubimca());

        // Set image
        Glide.with(context).load(animal.getImgUrl()).into(holder.animalImage);

        // Set status text and color
        if (animal.isUdomljen()) {
            holder.animalStatus.setText("Status: Udomljeno");
            holder.itemView.setBackgroundColor(Color.GREEN); // Set green background for adopted animals
        } else if (animal.isStatus_udomljavanja()) {
            holder.animalStatus.setText("Status: Zahtjev u tijeku");
            holder.itemView.setBackgroundColor(Color.parseColor("#FFA500")); // Orange background for requests in process
        } else {
            holder.animalStatus.setText("Status: Odbijen zahtjev");
            holder.itemView.setBackgroundColor(Color.RED); // Red background for rejected requests
        }

    holder.animalFrame.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(v.getContext().getApplicationContext(), AnimalDetailActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("id",String.valueOf(animal.getId()));
            activityResultLauncher.launch(i);
        }
    });
}

    private void openEditAnimalDialog(UpdateDnevnikModel animal) {
        // Implementacija dijaloga za uređivanje podataka o životinji
        // Korisnik može menjati ime, dodavati slike u galeriju i dodavati aktivnosti
        EditAnimalDialogFragment editDialog = EditAnimalDialogFragment.newInstance(animal);
        editDialog.show(((FragmentActivity) context).getSupportFragmentManager(), "EditAnimalDialog");
    }

    @Override
    public int getItemCount() {
        return animalsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView animalName, animalType, animalStatus;
        ImageView animalImage;
        Button actionButton;
        ConstraintLayout animalFrame;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            animalName = itemView.findViewById(R.id.animal_name);
            animalType = itemView.findViewById(R.id.animal_type);
            animalStatus = itemView.findViewById(R.id.animal_status);
            animalImage = itemView.findViewById(R.id.animal_image);
            animalFrame = itemView.findViewById(R.id.animal_frame);
        }
    }
}
