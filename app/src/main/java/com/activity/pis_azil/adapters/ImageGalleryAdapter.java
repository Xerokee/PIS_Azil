package com.activity.pis_azil.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.ImageViewHolder> {
    private Context context;
    private List<String> imageList;

    public ImageGalleryAdapter(Context context, List<String> imageList) {
        this.context = context;
        this.imageList = imageList != null ? imageList : new ArrayList<>();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageList.get(position);
        Glide.with(context).load(imageUrl).into(holder.imageView);

        // Postavi OnClickListener za brisanje slike
        holder.imageView.setOnLongClickListener(v -> {
            // Prikaži potvrdu pre brisanja
            new AlertDialog.Builder(context)
                    .setTitle("Obriši sliku")
                    .setMessage("Da li ste sigurni da želite obrisati ovu sliku?")
                    .setPositiveButton("Da", (dialog, which) -> {
                        imageList.remove(position); // Uklonite sliku iz liste
                        notifyDataSetChanged(); // Obavijestite adapter da je došlo do promjene podataka
                    })
                    .setNegativeButton("Ne", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return imageList != null ? imageList.size() : 0;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.gallery_image);
        }
    }
}

