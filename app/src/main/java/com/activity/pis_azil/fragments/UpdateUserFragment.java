package com.activity.pis_azil.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserModel;

public class UpdateUserFragment extends Fragment {
    private static final String ARG_USER = "user";

    public static UpdateUserFragment newInstance(UserModel user) {
        UpdateUserFragment fragment = new UpdateUserFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_update_user, container, false);
        UserModel user = (UserModel) getArguments().getSerializable(ARG_USER);

        EditText etUserName = view.findViewById(R.id.editTextUsername);
        EditText etMail = view.findViewById(R.id.editTextMail);
        EditText etPassword = view.findViewById(R.id.editTextPassword);

        etUserName.setText(user.getIme());
        etMail.setText(user.getEmail());
        etPassword.setText(user.getLozinka());

        Button btnUpdate = view.findViewById(R.id.buttonUpdateUser);
        btnUpdate.setOnClickListener(v -> {
            user.setIme(etUserName.getText().toString());
            user.setEmail(etMail.getText().toString());
            user.setLozinka(etPassword.getText().toString());

            // Poziv API-ja za ažuriranje korisnika
            // userViewModel.updateUser(user);
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }
}
