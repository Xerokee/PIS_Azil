package com.activity.pis_azil.fragments;

import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.RejectAdoptionModelRead;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.network.DataRefreshListener;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {
    private List<RejectAdoptionModelRead> mValues;
    private Context context;
    private boolean isAdmin;
    private DataRefreshListener dataRefreshListener;
    ApiService apiService = ApiClient.getClient().create(ApiService.class);

    public MyItemRecyclerViewAdapter(List<RejectAdoptionModelRead> items, Context context, DataRefreshListener listener) {
        mValues = items;
        this.context = context;
        this.isAdmin = checkIfUserIsAdmin();
        this.dataRefreshListener = listener;
    }

    // Metoda za ažuriranje podataka
    public void updateData(List<RejectAdoptionModelRead> newItems) {
        mValues = newItems;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_rejected_animals, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        RejectAdoptionModelRead currentItem = mValues.get(holder.getAdapterPosition());
        holder.mIdView.setText(currentItem.getId_korisnika().toString());
        holder.mContentView.setText(currentItem.getIme_ljubimca());

        if (isAdmin) {
            holder.mActionButton.setVisibility(View.VISIBLE);
        } else {
            holder.mActionButton.setVisibility(View.GONE);
        }

        holder.mActionButton.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            unblockAnimalAndReturnToAdoptionList(currentItem);
            Call<Void> call = apiService.deleteOdbijenaZivotinja(currentItem.getId());
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        removeItem(currentPosition);
                        Toast.makeText(v.getContext(), "Životinja uspješno uklonjena", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(v.getContext(), "Došlo je do pogreške", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(v.getContext(), "Greška prilikom brisanja", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void unblockAnimalAndReturnToAdoptionList(RejectAdoptionModelRead rejectedAnimal) {
        if (rejectedAnimal.getId_ljubimca() == null || rejectedAnimal.getId_korisnika() == null) {
            Toast.makeText(context, "Podaci o životinji nisu dostupni.", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateDnevnikModel updateModel = new UpdateDnevnikModel();
        // updateModel.setId_ljubimca(rejectedAnimal.getId_ljubimca());
        updateModel.setId_korisnika(rejectedAnimal.getId_korisnika());
        updateModel.setIme_ljubimca(rejectedAnimal.getIme_ljubimca());

        updateModel.setTip_ljubimca(rejectedAnimal.getTip_ljubimca() != null ? rejectedAnimal.getTip_ljubimca() : "Nepoznat");
        updateModel.setDatum(rejectedAnimal.getDatum() != null ? rejectedAnimal.getDatum() : "N/A");
        updateModel.setVrijeme(rejectedAnimal.getVrijeme() != null ? rejectedAnimal.getVrijeme() : "N/A");
        updateModel.setImgUrl(rejectedAnimal.getImgUrl() != null ? rejectedAnimal.getImgUrl() : "N/A");

        updateModel.setUdomljen(false); // Postavi udomljen na false
        updateModel.setStanje_zivotinje(rejectedAnimal.isStanje_zivotinje());
        updateModel.setStatus_udomljavanja(true);

        apiService.updateAdoption(1, rejectedAnimal.getId_ljubimca(), updateModel).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    removeItem(mValues.indexOf(rejectedAnimal));
                    Toast.makeText(context, "Životinja je vraćena u listu za udomitelje", Toast.LENGTH_SHORT).show();

                    if (dataRefreshListener != null) {
                        dataRefreshListener.refreshData();
                    }
                } else {
                    Log.e("API Error", "Greška pri vraćanju životinje: " + response.message() + ", kod: " + response.code());
                }
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("API Error", "Greška prilikom ažuriranja životinje: ", t);
                Toast.makeText(context, "Greška prilikom ažuriranja životinje", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIdView;
        public final TextView mContentView;
        public final Button mActionButton;

        public ViewHolder(View view) {
            super(view);
            mIdView = view.findViewById(R.id.itemNumber);
            mContentView = view.findViewById(R.id.content);
            mActionButton = view.findViewById(R.id.actionButton);
        }
    }

    private void removeItem(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mValues.size());
    }

    private boolean checkIfUserIsAdmin() {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);
        if (userJson != null) {
            Gson gson = new Gson();
            UserModel currentUser = gson.fromJson(userJson, UserModel.class);
            return currentUser != null && currentUser.isAdmin();
        }
        return false;
    }
}
