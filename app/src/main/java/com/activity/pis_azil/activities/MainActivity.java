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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.activity.pis_azil.AlarmReceiver;
import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserByEmailResponseModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.ui.profile.ProfileFragment;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ApiService apiService;
    private String userEmail;
    private UserModel currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AlarmReceiver.setAlarm(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_new_products, R.id.nav_my_orders, R.id.nav_my_carts)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        apiService = ApiClient.getClient().create(ApiService.class);

        View headerView = navigationView.getHeaderView(0);
        TextView headerName = headerView.findViewById(R.id.profileNam);
        TextView headerEmail = headerView.findViewById(R.id.profileEml);
        CircleImageView headerImg = headerView.findViewById(R.id.profileImg);

        // Get user data from intent
        UserModel user = (UserModel) getIntent().getSerializableExtra("user_data");
        if (user != null) {
            updateNavigationHeader(user, headerName, headerEmail, headerImg);
        } else {
            // If user data is not available in the intent, fetch from SharedPreferences
            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            userEmail = preferences.getString("email", "email@example.com"); // Fetch from saved preferences
            String userName = preferences.getString("ime", "");
            String userImg = preferences.getString("profileImg", "");
            UserModel sharedPrefsUser = new UserModel();
            sharedPrefsUser.setIme(userName);
            sharedPrefsUser.setEmail(userEmail);
            sharedPrefsUser.setProfileImg(userImg);
            updateNavigationHeader(sharedPrefsUser, headerName, headerEmail, headerImg);
        }

        // Listen for profile updates
        LocalBroadcastManager.getInstance(this).registerReceiver(profileUpdatedReceiver,
                new IntentFilter(ProfileFragment.ACTION_PROFILE_UPDATED));

        // Fetch latest user data
        fetchUserData();
    }

    private void updateNavigationHeader(UserModel user, TextView headerName, TextView headerEmail, CircleImageView headerImg) {
        headerName.setText(user.getIme());
        headerEmail.setText(user.getEmail());
        if (user.getProfileImg() != null && !user.getProfileImg().isEmpty()) {
            Glide.with(this).load(user.getProfileImg()).into(headerImg);
        } else {
            headerImg.setImageResource(R.drawable.fruits);
        }
    }

    private final BroadcastReceiver profileUpdatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            fetchUserData();
        }
    };

    private void fetchUserData() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userEmail = preferences.getString("email", "email@example.com");

        apiService.getUserByIdEmail(userEmail).enqueue(new Callback<UserByEmailResponseModel>() {
            @Override
            public void onResponse(Call<UserByEmailResponseModel> call, Response<UserByEmailResponseModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserModel user = response.body().getResult();
                    if (user != null) {
                        // Postavi admin status na temelju uloge
                        if (user.getUserRole() != null) {
                            user.setAdmin(user.getUserRole().isAdmin());
                        }

                        currentUser = user;
                        // Spremi korisnika u SharedPreferences
                        saveUserToSharedPreferences(user);

                        // Update navigation header
                        View headerView = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
                        updateNavigationHeader(user,
                                headerView.findViewById(R.id.profileNam),
                                headerView.findViewById(R.id.profileEml),
                                headerView.findViewById(R.id.profileImg));
                    }
                }
            }

            @Override
            public void onFailure(Call<UserByEmailResponseModel> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to fetch user data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToSharedPreferences(UserModel user) {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String userJson = gson.toJson(user);
        editor.putString("current_user", userJson);
        editor.apply();
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

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(profileUpdatedReceiver);
        super.onDestroy();
    }
}
