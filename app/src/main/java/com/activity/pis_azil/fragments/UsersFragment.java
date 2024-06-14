package com.activity.pis_azil.fragments;

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

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.UsersAdapter;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.models.UserViewModel;

import java.util.List;

public class UsersFragment extends Fragment {

    private UserViewModel userViewModel;
    private UsersAdapter usersAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersAdapter = new UsersAdapter();
        recyclerView.setAdapter(usersAdapter);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getAllUsers().observe(getViewLifecycleOwner(), new Observer<List<UserModel>>() {
            @Override
            public void onChanged(List<UserModel> users) {
                usersAdapter.setUsers(users);
            }
        });

        userViewModel.fetchAllUsers();

        return view;
    }
}
