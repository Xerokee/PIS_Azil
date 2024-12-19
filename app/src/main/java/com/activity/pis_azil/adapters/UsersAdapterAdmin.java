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

public class UsersAdapterAdmin extends RecyclerView.Adapter<UsersAdapterAdmin.UserViewHolder> {

    private List<UserModel> users = new ArrayList<>();

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_admin, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = users.get(position);
        holder.idTextView.setText(String.format("ID: %d", user.getIdKorisnika()));
        holder.nameTextView.setText(String.format("Ime: %s", user.getIme()));
        holder.emailTextView.setText(String.format("Email: %s", user.getEmail()));
        holder.passwordTextView.setText(String.format("Lozinka: %s", user.getLozinka()));

        holder.editUser.setOnClickListener(v -> {
            // Otvaranje fragmenta za ažuriranje korisnika
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(user);
            }
        });

        holder.deleteUser.setOnClickListener(v -> {
            // Otvaranje dijaloga za potvrdu brisanja
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(user);
            }
        });
    }

    // Dodajte interfejse za edit i delete
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnEditClickListener {
        void onEditClick(UserModel user);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(UserModel user);
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
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
        public View editUser;
        public View deleteUser;
        TextView idTextView;
        TextView nameTextView;
        TextView emailTextView;
        TextView passwordTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            idTextView = itemView.findViewById(R.id.user_id);
            nameTextView = itemView.findViewById(R.id.user_name);
            emailTextView = itemView.findViewById(R.id.user_email);
            passwordTextView = itemView.findViewById(R.id.user_password);
            editUser = itemView.findViewById(R.id.edit_user);
            deleteUser = itemView.findViewById(R.id.delete_user);
        }
    }
}
