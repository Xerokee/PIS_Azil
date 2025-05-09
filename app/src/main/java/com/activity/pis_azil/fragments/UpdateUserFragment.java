package com.activity.pis_azil.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiService;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class UpdateUserFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_USER_ID = 1;
    private static final String ARG_USER = "user";
    private EditText etName, etSurname, etUsername, etMail, etPassword;
    private ImageView ivUserImage;
    private Uri imageUri;
    private UserModel user;
    private LinearLayout userFormContainer;
    private FloatingActionButton fabAddAnimal;
    private ApiService apiService;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public static UpdateUserFragment newInstance(UserModel user) {
        UpdateUserFragment fragment = new UpdateUserFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        if (imageUri != null) {
                            Log.d("UpdateUserFragment", "Selected image: " + imageUri.toString());
                            if (ivUserImage != null) {
                                Glide.with(this)
                                        .load(imageUri)
                                        .placeholder(R.drawable.ic_baseline_person_24)
                                        .into(ivUserImage);
                            }
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_update_user, container, false);

        user = (UserModel) getArguments().getSerializable(ARG_USER);

        etName = view.findViewById(R.id.editTextName);
        etSurname = view.findViewById(R.id.editTextSurname);
        etUsername = view.findViewById(R.id.editTextUsername);
        etMail = view.findViewById(R.id.editTextMail);
        etPassword = view.findViewById(R.id.editTextPassword);
        ivUserImage = view.findViewById(R.id.imageViewUser);

        etName.setText(user.getIme());
        etSurname.setText(user.getPrezime());
        etUsername.setText(user.getKorisnickoIme());
        etMail.setText(user.getEmail());
        etPassword.setText(user.getLozinka());

        if (user.getProfileImg() != null && !user.getProfileImg().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfileImg())
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .into(ivUserImage);
        } else {
            ivUserImage.setImageResource(R.drawable.ic_baseline_person_24);
        }

        ivUserImage.setOnClickListener(v -> openImageChooser());

        Button btnUpdate = view.findViewById(R.id.buttonUpdateUser);
        btnUpdate.setOnClickListener(v -> {
            user.setIme(etName.getText().toString());
            user.setPrezime(etSurname.getText().toString());
            user.setKorisnickoIme(etUsername.getText().toString());
            user.setEmail(etMail.getText().toString());
            user.setLozinka(etPassword.getText().toString());

            if (imageUri != null) {
                user.setProfileImg(imageUri.toString());
                Log.d("UpdateUserFragment", "Updated image URI: " + imageUri.toString());
            }

            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }
}
