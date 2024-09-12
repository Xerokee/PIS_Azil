package com.activity.pis_azil.fragments;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.RejectAdoptionModelRead;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private List<RejectAdoptionModelRead> mValues;
    private Context context;
    private boolean isAdmin;  // Čuva informaciju o tome je li korisnik admin
    ApiService apiService = ApiClient.getClient().create(ApiService.class);

    public MyItemRecyclerViewAdapter(List<RejectAdoptionModelRead> items, Context context) {
        mValues = items;
        this.context = context;
        this.isAdmin = checkIfUserIsAdmin(); // Provjeri je li korisnik admin prilikom kreiranja adaptera
    }

    // Metoda za ažuriranje podataka
    public void updateData(List<RejectAdoptionModelRead> newItems) {
        mValues = newItems;
        notifyDataSetChanged(); // Obavijesti adapter da se podaci promijenili
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_rejected_animals, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        RejectAdoptionModelRead currentItem = mValues.get(position);
        holder.mIdView.setText(currentItem.getId_korisnika().toString());
        holder.mContentView.setText(currentItem.getIme_ljubimca());

        // Postavi vidljivost gumba na temelju toga je li korisnik admin
        if (isAdmin) {
            holder.mActionButton.setVisibility(View.VISIBLE); // Pokaži gumb ako je admin
        } else {
            holder.mActionButton.setVisibility(View.GONE); // Sakrij gumb ako nije admin
        }

        holder.mActionButton.setOnClickListener(v -> {

            Call<Void> call = apiService.deleteOdbijenaZivotinja(currentItem.getId());
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        // Makni životinju iz liste
                        removeItem(position);
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

    // Metoda za uklanjanje stavke iz liste i obavještavanje adaptera
    private void removeItem(int position) {
        mValues.remove(position); // Ukloni životinju iz liste
        notifyItemRemoved(position); // Obavijesti adapter da je stavka uklonjena
        notifyItemRangeChanged(position, mValues.size()); // Ažuriraj ostatak liste
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
