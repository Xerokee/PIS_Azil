package com.activity.pis_azil.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.activities.AnimalDetailActivity;
import com.activity.pis_azil.models.SlikaModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SlikeAdapter  extends RecyclerView.Adapter<SlikeAdapter.ViewHolder> {

    List<SlikaModel> listaSlika;
    Activity activity;

    public SlikeAdapter(Activity activity, List<SlikaModel> listaSlika){
        this.activity=activity;
        this.listaSlika=listaSlika;
    }


    @NonNull
    @Override
    public SlikeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.slika, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlikeAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        byte[] decodedString = Base64.decode(listaSlika.get(position).getSlika_data(), Base64.DEFAULT);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        holder.ivSlika.setImageBitmap(decodedBitmap);

        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.ivSlika.getContext());
                builder.setMessage("Da li ste sigurni da želite obrisati sliku?");

                builder.setPositiveButton("Obriši", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ApiService apiService = ApiClient.getClient().create(ApiService.class);
                        apiService.deleteSlika(listaSlika.get(position).getId()).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(v.getContext(), "Slika uspješno obrisana.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(v.getContext(), "Slika nije uspješno obrisana.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(v.getContext(), "Greška u API pozivu.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                activity.finish();
                                activity.startActivity(activity.getIntent());
                            }
                        }, 3000);

                    }
                });
                builder.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return listaSlika.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView ivSlika;
        ImageView ivDelete;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            ivSlika=itemView.findViewById(R.id.ivSlika);
            ivDelete=itemView.findViewById(R.id.ivDelete);
        }
    }
}
