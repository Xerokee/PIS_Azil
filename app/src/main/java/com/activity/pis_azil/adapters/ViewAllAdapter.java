package com.activity.pis_azil.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.ApiClient;
import com.activity.pis_azil.ApiService;
import com.activity.pis_azil.R;
import com.activity.pis_azil.activities.DetailedActivity;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.models.ViewAllModel;
import com.bumptech.glide.Glide;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewAllAdapter extends RecyclerView.Adapter<ViewAllAdapter.ViewHolder> {

    Context context;
    List<ViewAllModel> list;
    ApiService apiService;

    public ViewAllAdapter(Context context, List<ViewAllModel> list) {
        this.context = context;
        this.list = list;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    @NonNull
    @Override
    public ViewAllAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_all_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewAllAdapter.ViewHolder holder, int position) {
        ViewAllModel model = list.get(position);
        Glide.with(context).load(model.getImg_url()).into(holder.imageView);
        holder.name.setText(model.getName());
        holder.description.setText(model.getDescription());
        holder.rating.setText(model.getRating());

        if (model.isAdopted()) {
            holder.adopterName.setVisibility(View.VISIBLE);
            if (model.getAdopterName() == null || model.getAdopterName().isEmpty()) {
                fetchAdopterName(model.getAdopterId(), holder.adopterName);
            } else {
                holder.adopterName.setText("Udomitelj: " + model.getAdopterName());
            }
        } else {
            holder.adopterName.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailedActivity.class);
            intent.putExtra("detail", (CharSequence) model); // Ensure ViewAllModel implements Serializable or Parcelable
            context.startActivity(intent);
        });
    }

    private void fetchAdopterName(String adopterId, final TextView adopterNameTextView) {
        if (adopterId == null || adopterId.trim().isEmpty()) {
            adopterNameTextView.setText("Udomitelj: Nepoznato");
            return;
        }

        apiService.getUserById(Integer.parseInt(adopterId)).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adopterNameTextView.setText("Udomitelj: " + response.body().getIme());
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

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView name, description, rating, adopterName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.view_img);
            name = itemView.findViewById(R.id.view_name);
            description = itemView.findViewById(R.id.view_description);
            rating = itemView.findViewById(R.id.view_rating);
            adopterName = itemView.findViewById(R.id.adopter_name);
        }
    }
}
