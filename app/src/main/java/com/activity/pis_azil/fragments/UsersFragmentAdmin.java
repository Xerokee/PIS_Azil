package com.activity.pis_azil.fragments;

import static android.app.Activity.RESULT_OK;

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
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.activities.NewUserActivity;
import com.activity.pis_azil.activities.UpdateUserActivity;
import com.activity.pis_azil.adapters.UsersAdapterAdmin;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.models.UserViewModelAdmin;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersFragmentAdmin extends Fragment {

    private UserViewModelAdmin userViewModelAdmin;
    private UsersAdapterAdmin usersAdapterAdmin;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_admin, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView2);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersAdapterAdmin = new UsersAdapterAdmin();
        recyclerView.setAdapter(usersAdapterAdmin);

        apiService = ApiClient.getClient().create(ApiService.class);

        userViewModelAdmin = new ViewModelProvider(this).get(UserViewModelAdmin.class);
        userViewModelAdmin.getAllUsers().observe(getViewLifecycleOwner(), usersAdapterAdmin::setUsers);

        usersAdapterAdmin.setOnEditClickListener(user -> {
            Intent intent = new Intent(getActivity(), UpdateUserActivity.class);
            intent.putExtra("user", user);
            startActivityForResult(intent, 100);
        });

        usersAdapterAdmin.setOnDeleteClickListener(user -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Brisanje Korisnika")
                    .setMessage("Da li ste sigurni da želite izbrisati korisnika?")
                    .setPositiveButton("Da", (dialog, which) -> deleteUser(user))
                    .setNegativeButton("Ne", null)
                    .show();
        });

        Button btnAddUser = view.findViewById(R.id.btnAddUser);
        btnAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NewUserActivity.class);
            startActivityForResult(intent, 200);
        });

        userViewModelAdmin.fetchAllUsers();
        return view;
    }

    private void deleteUser(UserModel user) {
        apiService.deleteUser(1, user.getIdKorisnika())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Korisnik uspješno izbrisan", Toast.LENGTH_SHORT).show();
                            userViewModelAdmin.fetchAllUsers();
                        } else {
                            Toast.makeText(getContext(), "Pogreška pri brisanju korisnika", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Greška u mreži", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Check which activity returned the result
            if (requestCode == 100 || requestCode == 200) {
                userViewModelAdmin.fetchAllUsers();
            }
        }
    }
}
