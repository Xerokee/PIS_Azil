package com.activity.pis_azil.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserModel;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapterAdmin extends RecyclerView.Adapter<UsersAdapterAdmin.UserViewHolder> {

    private List<UserModel> users = new ArrayList<>();

    @NonNull
    @Override
    public UsersAdapterAdmin.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_admin2, parent, false);
        return new UsersAdapterAdmin.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = users.get(position);
        if (user.getProfileImg() != null && !user.getProfileImg().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getProfileImg()) // URL slike
                    .placeholder(R.drawable.menu_person) // Placeholder ako slika nije dostupna
                    .error(R.drawable.menu_person) // Ako se slika ne može učitati
                    .into(holder.ivUserImage);
        } else {
            holder.ivUserImage.setImageResource(R.drawable.menu_person);
        }

        holder.idTextView.setText(String.format("ID: %d", user.getIdKorisnika()));
        holder.nameTextView.setText(String.format("Ime: %s", user.getIme()));
        holder.surnameTextView.setText(String.format("Prezime: %s", user.getPrezime()));
        holder.nicknameTextView.setText(String.format("Korisničko ime: %s", user.getKorisnickoIme()));
        holder.emailTextView.setText(String.format("Email: %s", user.getEmail()));
        holder.passwordTextView.setText(String.format("Lozinka: %s", user.getLozinka()));

        if (user.isAdmin()) {
            holder.adminTextView.setText("Admin: Da");
        } else {
            holder.adminTextView.setText("Admin: Ne");
        }

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
        TextView surnameTextView;
        TextView nicknameTextView;
        TextView emailTextView;
        TextView passwordTextView;
        TextView adminTextView;
        private ImageView ivUserImage;
        ConstraintLayout userFrame;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserImage = itemView.findViewById(R.id.user_image);
            idTextView = itemView.findViewById(R.id.user_id);
            nameTextView = itemView.findViewById(R.id.user_name);
            surnameTextView = itemView.findViewById(R.id.user_surname);
            nicknameTextView = itemView.findViewById(R.id.user_nickname);
            emailTextView = itemView.findViewById(R.id.user_email);
            passwordTextView = itemView.findViewById(R.id.user_password);
            adminTextView = itemView.findViewById(R.id.user_admin);
            editUser = itemView.findViewById(R.id.edit_user);
            deleteUser = itemView.findViewById(R.id.delete_user);
            userFrame = itemView.findViewById(R.id.user_frame);
        }
    }
}
