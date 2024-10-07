package com.activity.pis_azil.adapters;

import android.content.Context;
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

public class RequestListAdapter extends RecyclerView.Adapter<RequestListAdapter.ViewHolder> {

    private Context context;
    private List<AnimalModel> requestList;

    public RequestListAdapter(Context context, List<AnimalModel> requestList) {
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnimalModel animal = requestList.get(position);

        holder.animalName.setText(animal.getImeLjubimca());
        holder.animalType.setText("Tip: " + animal.getTipLjubimca());
        holder.animalStatus.setText("Zahtjev na čekanju");

        // Use Glide to load the animal image
        if (animal.getImgUrl() != null && !animal.getImgUrl().isEmpty()) {
            Glide.with(context).load(animal.getImgUrl()).into(holder.animalImage);
        } else {
            holder.animalImage.setImageResource(R.drawable.profile); // Default image if none available
        }
    }

    @Override
    public int getItemCount() {
        return requestList.size();
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
