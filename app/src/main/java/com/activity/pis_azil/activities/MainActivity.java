package com.activity.pis_azil.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.ui.profile.ProfileFragment;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(profileUpdateReceiver, new IntentFilter(ProfileFragment.ACTION_PROFILE_UPDATED));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_category, R.id.nav_profile, R.id.nav_new_products, R.id.nav_my_orders, R.id.nav_my_carts)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View headerView = navigationView.getHeaderView(0);
        TextView headerName = headerView.findViewById(R.id.nav_header_name);
        TextView headerEmail = headerView.findViewById(R.id.nav_header_email);
        CircleImageView headerImg = headerView.findViewById(R.id.nav_header_img);

        apiService = ApiClient.getClient().create(ApiService.class);
        updateNavigationHeader(headerName, headerEmail, headerImg);
    }

    private void updateNavigationHeader(TextView headerName, TextView headerEmail, CircleImageView headerImg) {
        SharedPreferences preferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = preferences.getInt("id_korisnika", -1); // Default to -1 if not found
        Log.i("MainActivity", "Fetching user data for ID: " + userId);

        if (userId == -1) {
            Log.e("MainActivity", "User ID not found");
            return;
        }

        apiService.getUserById(userId).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(@NonNull Call<UserModel> call, @NonNull Response<UserModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserModel user = response.body();
                    Log.i("MainActivity", "Fetched user: " + user.getIme() + ", " + user.getEmail());

                    headerName.setVisibility(View.VISIBLE);
                    headerEmail.setVisibility(View.VISIBLE);

                    headerName.setText(user.getIme());
                    headerEmail.setText(user.getEmail());

                    if (user.getProfileImg() != null && !user.getProfileImg().isEmpty()) {
                        Glide.with(MainActivity.this).load(user.getProfileImg()).into(headerImg);
                    } else {
                        headerImg.setImageResource(R.drawable.fruits); // Placeholder image
                    }
                    Log.i("MainActivity", "User data fetched successfully");
                } else {
                    Log.e("MainActivity", "Error fetching user data: " + response.message() + " " + response.code());
                    try {
                        Log.e("MainActivity", "Error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModel> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Failed to fetch user data", t);
            }
        });
    }

    private final BroadcastReceiver profileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ProfileFragment.ACTION_PROFILE_UPDATED.equals(intent.getAction())) {
                NavigationView navigationView = findViewById(R.id.nav_view);
                View headerView = navigationView.getHeaderView(0);
                TextView headerName = headerView.findViewById(R.id.nav_header_name);
                TextView headerEmail = headerView.findViewById(R.id.nav_header_email);
                CircleImageView headerImg = headerView.findViewById(R.id.nav_header_img);
                updateNavigationHeader(headerName, headerEmail, headerImg);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(profileUpdateReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}

