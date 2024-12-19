package com.activity.pis_azil.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.activity.pis_azil.R;
import com.activity.pis_azil.activities.NewUserActivity;
import com.activity.pis_azil.activities.UpdateUserActivity;
import com.activity.pis_azil.adapters.UsersAdapterAdmin;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.models.UserViewModelAdmin;

import java.util.List;

public class UsersFragmentAdmin extends Fragment {

    private UserViewModelAdmin userViewModelAdmin;
    private UsersAdapterAdmin usersAdapterAdmin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_admin, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView2);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersAdapterAdmin = new UsersAdapterAdmin();
        recyclerView.setAdapter(usersAdapterAdmin);

        userViewModelAdmin = new ViewModelProvider(this).get(UserViewModelAdmin.class);
        userViewModelAdmin.getAllUsers().observe(getViewLifecycleOwner(), usersAdapterAdmin::setUsers);

        // Klik na ikonu za ažuriranje
        usersAdapterAdmin.setOnEditClickListener(user -> {
            // Otvori UpdateUserActivity s podacima korisnika
            Intent intent = new Intent(getActivity(), UpdateUserActivity.class);
            intent.putExtra("user", user); // Prosljeđivanje korisnika koji se ažurira
            startActivity(intent);
        });

        // Klik na ikonu za brisanje
        usersAdapterAdmin.setOnDeleteClickListener(user -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Brisanje korisnika")
                    .setMessage("Jeste li sigurni da želite obrisati korisnika?")
                    .setPositiveButton("Da", (dialog, which) -> userViewModelAdmin.deleteUser(user))
                    .setNegativeButton("Ne", null)
                    .show();
        });

        // Set the OnClickListener for the Add User button
        Button btnAddUser = view.findViewById(R.id.btnAddUser);
        btnAddUser.setOnClickListener(v -> {
            // Pokreni aktivnost za dodavanje novog korisnika
            Intent intent = new Intent(getActivity(), NewUserActivity.class);
            startActivity(intent);
        });

        userViewModelAdmin.fetchAllUsers();
        return view;
    }
}
