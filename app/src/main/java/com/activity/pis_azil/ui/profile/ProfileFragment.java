package com.activity.pis_azil.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.activity.pis_azil.R;
import com.activity.pis_azil.activities.HomeActivity;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.models.UserRoleModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    CircleImageView profileImage;
    EditText ime, email, lozinka;
    Button update, logout;
    ApiService apiService;
    SharedPreferences preferences;

    public static final String ACTION_PROFILE_UPDATED = "com.activity.vuv_azil_navigation.PROFILE_UPDATED";
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int IMAGE_PICK_GALLERY_CODE = 101;
    private static final int IMAGE_PICK_CAMERA_CODE = 102;
    private Uri imageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);
        preferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        profileImage = root.findViewById(R.id.profileImage);
        ime = root.findViewById(R.id.profileName);
        email = root.findViewById(R.id.profileEmail);
        lozinka = root.findViewById(R.id.profilePassword);
        update = root.findViewById(R.id.update);
        logout = root.findViewById(R.id.logout);

        logout.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red));
        update.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.green_700));

        profileImage.setOnClickListener(v -> showImagePickDialog());
        logout.setOnClickListener(v -> logoutUser());
        update.setOnClickListener(v -> updateUserProfile());
        loadUserProfile();

        return root;
    }

    private void logoutUser() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(requireContext(), HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showImagePickDialog() {
        String[] options = {"Kamere", "Galerije"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Odaberite sliku iz");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    pickFromCamera();
                }
            } else {
                pickFromGallery();
            }
        });
        builder.create().show();
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean result1 = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result && result1;
    }

    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                imageUri = data != null ? data.getData() : null;
                if (imageUri != null) {
                    profileImage.setImageURI(imageUri);
                }
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                profileImage.setImageURI(imageUri);
            }
        }
    }

    private void loadUserProfile() {
        int userId = preferences.getInt("id_korisnika", -1);

        if (userId == -1) {
            Toast.makeText(getContext(), "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getUserById(userId).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserModel userModel = response.body();
                    ime.setText(userModel.getIme());
                    email.setText(userModel.getEmail());
                    lozinka.setText(userModel.getLozinka());
                    if (userModel.getProfileImg() != null) {
                        Glide.with(getContext()).load(userModel.getProfileImg()).into(profileImage);
                    }
                    // Fetch and display user role
                    fetchUserRole(userId);
                    // Save current admin status
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("admin", userModel.isAdmin());
                    editor.apply();
                } else {
                    Toast.makeText(getContext(), "Error fetching user data: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserRole(int userId) {
        apiService.getUserRoleById(userId).enqueue(new Callback<UserRoleModel>() {
            @Override
            public void onResponse(Call<UserRoleModel> call, Response<UserRoleModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserRoleModel userRole = response.body();
                    // Display user role
                    if (userRole != null && "Admin".equals(userRole.getNazivUloge())) {
                        Toast.makeText(getContext(), "User is an Admin", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserRoleModel> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to fetch user role: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserProfile() {
        String Ime = ime.getText().toString().trim();
        String Mail = email.getText().toString().trim();
        String Lozinka = lozinka.getText().toString().trim();
        boolean admin = preferences.getBoolean("admin", false);

        if (Ime.isEmpty() || Mail.isEmpty() || Lozinka.isEmpty()) {
            Toast.makeText(getContext(), "Sva polja su obavezna", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = preferences.getInt("id_korisnika", -1);

        if (userId == -1) {
            Toast.makeText(getContext(), "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("ime", Ime);
        userUpdates.put("email", Mail);
        userUpdates.put("lozinka", Lozinka);

        // Ensure admin status is not changed for the user with ID 1
        if (userId != 1) {
            userUpdates.put("admin", admin);
        }

        apiService.updateUser(userId, userId, userUpdates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Profil ažuriran", Toast.LENGTH_SHORT).show();
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(ACTION_PROFILE_UPDATED));
                    // Update local preferences
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("ime", Ime);
                    editor.putString("email", Mail);
                    editor.putString("lozinka", Lozinka);
                    editor.apply();
                    // Update UI
                    loadUserProfile();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("ProfileFragment", "Update failed: " + errorBody + " " + response.code());
                        Toast.makeText(getContext(), "Greška: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ProfileFragment", "Update failed", t);
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromCamera();
                } else {
                    Toast.makeText(getContext(), "Potrebne su dozvole za korištenje kamere", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
