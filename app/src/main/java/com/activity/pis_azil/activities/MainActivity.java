package com.activity.pis_azil.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.activity.pis_azil.AlarmReceiver;
import android.Manifest;
import com.activity.pis_azil.R;
import com.activity.pis_azil.models.SharedViewModel;
import com.activity.pis_azil.models.UserByEmailResponseModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.ui.profile.ProfileFragment;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
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
    private static final String TAG = "FCM_MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);
                });
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent=getIntent();
        Bundle valueFromFirstActivity = intent.getExtras();
        String userId = intent.getStringExtra("korisnikId");
        SharedViewModel sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        sharedViewModel.setUserId(userId);
        // Log.i("korisnikid",userId);

        AlarmReceiver.setAlarm(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_new_products, R.id.nav_my_orders)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        apiService = ApiClient.getClient().create(ApiService.class);

        View headerView = navigationView.getHeaderView(0);
        TextView headerUserName = headerView.findViewById(R.id.profileUserNam);
        TextView headerEmail = headerView.findViewById(R.id.profileEml);
        CircleImageView headerImg = headerView.findViewById(R.id.profileImg);

        // Dohvati SharedPreferences i provjeri je li korisnik admin
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        boolean isAdmin = prefs.getBoolean("admin", false);
        String userJson = prefs.getString("current_user", null);
        UserModel currentUser = new Gson().fromJson(userJson, UserModel.class);
        if (currentUser != null) {
            updateNavigationHeader(currentUser, headerUserName, headerEmail, headerImg);
        }

        Menu menu = navigationView.getMenu();
        if (!isAdmin) {
            menu.findItem(R.id.nav_admin_settings).setVisible(false);
            menu.findItem(R.id.nav_new_products).setVisible(false);
            menu.findItem(R.id.rejectedAnimalsFragment).setVisible(false);
            menu.findItem(R.id.nav_my_orders).setVisible(false);
            menu.findItem(R.id.nav_admin_dashboard).setVisible(false);
            menu.findItem(R.id.meetingsFragment).setVisible(true);
        } else {
            // Prikazi sve za admina
            menu.findItem(R.id.nav_new_products).setVisible(true);
            menu.findItem(R.id.nav_admin_menu).setVisible(true);
            menu.findItem(R.id.nav_my_orders).setVisible(true);
            menu.findItem(R.id.nav_my_animals).setVisible(false);
            menu.findItem(R.id.meetingsFragment).setVisible(true);
        }

        // Get user data from intent
        UserModel user = (UserModel) getIntent().getSerializableExtra("user_data");
        if (user != null) {
            updateNavigationHeader(user, headerUserName, headerEmail, headerImg);
        } else {
            // If user data is not available in the intent, fetch from SharedPreferences
            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            userEmail = preferences.getString("email", "email@example.com"); // Fetch from saved preferences
            String userName = preferences.getString("korisnickoIme", "");
            String userImg = preferences.getString("profileImg", "");
            UserModel sharedPrefsUser = new UserModel();
            sharedPrefsUser.setKorisnickoIme(userName);
            sharedPrefsUser.setEmail(userEmail);
            sharedPrefsUser.setProfileImg(userImg);
            updateNavigationHeader(sharedPrefsUser, headerUserName, headerEmail, headerImg);
        }

        // Listen for profile updates
        LocalBroadcastManager.getInstance(this).registerReceiver(profileUpdatedReceiver,
                new IntentFilter(ProfileFragment.ACTION_PROFILE_UPDATED));

        // Fetch latest user data
        fetchUserData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Dozvola za notifikacije odobrena.");
            } else {
                Log.d(TAG, "Dozvola za notifikacije odbijena.");
            }
        }
    }

    // Osvježavanje podataka pri svakoj promjeni stanja aktivnosti
    @Override
    protected void onResume() {
        super.onResume();

        // Osvježi podatke o korisniku svaki put kad se aktivnost ponovo pojavi
        fetchUserData();
    }

    private void updateNavigationHeader(UserModel user, TextView headerUserName, TextView headerEmail, CircleImageView headerImg) {
        headerUserName.setText(user.getKorisnickoIme());
        headerEmail.setText(user.getEmail());
        if (user.getProfileImg() != null && !user.getProfileImg().isEmpty()) {
            Glide.with(this).load(user.getProfileImg()).into(headerImg);
        } else {
            headerImg.setImageResource(R.drawable.paw);
        }
    }

    private void saveUserToSharedPreferences(UserModel user) {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String userJson = gson.toJson(user);
        editor.putString("current_user", userJson);
        editor.apply();
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
                                headerView.findViewById(R.id.profileUserNam),
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