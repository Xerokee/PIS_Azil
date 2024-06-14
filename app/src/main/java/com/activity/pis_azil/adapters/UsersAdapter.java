package com.activity.pis_azil.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserModel;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<UserModel> users = new ArrayList<>();

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = users.get(position);
        holder.idTextView.setText(String.format("ID: %d", user.getIdKorisnika()));
        holder.nameTextView.setText(String.format("Ime: %s", user.getIme()));
        holder.emailTextView.setText(String.format("Email: %s", user.getEmail()));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<UserModel> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView idTextView;
        TextView nameTextView;
        TextView emailTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            idTextView = itemView.findViewById(R.id.user_id);
            nameTextView = itemView.findViewById(R.id.user_name);
            emailTextView = itemView.findViewById(R.id.user_email);
        }
    }
}
