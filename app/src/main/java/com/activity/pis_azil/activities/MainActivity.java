package com.activity.pis_azil.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.models.UserViewModel;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.ui.profile.ProfileFragment;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private UserViewModel userViewModel;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        View headerView = navigationView.getHeaderView(0);
        TextView headerName = headerView.findViewById(R.id.profileNam);
        TextView headerEmail = headerView.findViewById(R.id.profileEml);
        CircleImageView headerImg = headerView.findViewById(R.id.profileImg);

        // Get user data from intent
        UserModel user = (UserModel) getIntent().getSerializableExtra("user_data");
        if (user != null) {
            updateNavigationHeader(user, headerName, headerEmail, headerImg);
        } else {
            // If user data is not available in the intent, fetch from ViewModel
            userEmail = "email@example.com"; // Set default or fetch from saved preferences
            userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
            userViewModel.getUser().observe(this, new Observer<UserModel>() {
                @Override
                public void onChanged(UserModel user) {
                    updateNavigationHeader(user, headerName, headerEmail, headerImg);
                }
            });
            userViewModel.fetchUserData(userEmail);
        }
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

