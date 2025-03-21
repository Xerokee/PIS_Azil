package com.activity.pis_azil.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.Aktivnost;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.activity.pis_azil.models.MessageModel;
import com.activity.pis_azil.models.NotificationBodyModel;
import com.activity.pis_azil.models.NotificationModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiClientToken;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.network.ApiServiceToken;
import com.activity.pis_azil.network.HttpRequestResponseList;
import com.orhanobut.dialogplus.DialogPlus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimalActivityFragment extends Fragment {
    LinearLayout linearLayoutAktivnosti;
    TextView tvNemaAktivnosti;
    List<Aktivnost> listaAktivnosti = new ArrayList<>();
    ImageButton addAktivnost;
    AnimalModel detailedActivity;
    ApiService apiService;
    ApiServiceToken apiServiceToken;
    IsBlockedAnimalModel animalModel;
    int animalId;
    String token;

    public AnimalActivityFragment() {
        // Required empty public constructor
    }

    public AnimalActivityFragment(AnimalModel da, int aid) {
        detailedActivity = da;
        animalId = aid;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popis_aktivnosti, container, false);

        // Initialize UI elements
        linearLayoutAktivnosti = view.findViewById(R.id.linearLayoutAktivnosti);
        tvNemaAktivnosti = view.findViewById(R.id.tvNemaAktivnosti);
        addAktivnost = view.findViewById(R.id.addAktivnost);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        apiService = ApiClient.getClient().create(ApiService.class);
        apiServiceToken = ApiClientToken.getClient().create(ApiServiceToken.class);

        loadActivityLog();
        refreshPopisAktivnosti();

        addAktivnost.setOnClickListener(v -> openAddActivityDialog());

        return view;
    }

    private void loadActivityLog() {
        apiService.getAktivnostiById(animalId).enqueue(new Callback<HttpRequestResponseList<Aktivnost>>() {
            @Override
            public void onResponse(Call<HttpRequestResponseList<Aktivnost>> call, Response<HttpRequestResponseList<Aktivnost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Aktivnost> aktivnosti = response.body().getResult();
                    if (aktivnosti.isEmpty()) {
                        tvNemaAktivnosti.setVisibility(View.VISIBLE);
                    } else {
                        linearLayoutAktivnosti.removeAllViews();
                        for (Aktivnost a : aktivnosti) {
                            TextView tv = new TextView(getContext());
                            tv.setText(a.getDatum() + " - " + a.getAktivnost() + "\n" + a.getOpis());
                            linearLayoutAktivnosti.addView(tv);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<HttpRequestResponseList<Aktivnost>> call, Throwable t) {
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openAddActivityDialog() {
        final DialogPlus dialogPlus = DialogPlus.newDialog(getContext())
                .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.add_activity))
                .setExpanded(false, 2000)
                .setGravity(Gravity.CENTER)
                .setCancelable(true)
                .setPadding(30, 30, 30, 30)
                .create();

        View dialogView = dialogPlus.getHolderView();
        TextView inputDateActivity = dialogView.findViewById(R.id.inputDateActivity);
        Button addActivity = dialogView.findViewById(R.id.addActivity);
        Button closeActivity = dialogView.findViewById(R.id.closeActivity);
        EditText inputDescriptionActivity = dialogView.findViewById(R.id.inputDescriptionActivity);
        Spinner activitySpinner = dialogView.findViewById(R.id.activitySpinner);
        TextView selectedActivityTextView = dialogView.findViewById(R.id.selectedActivityTextView);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.activity_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activitySpinner.setAdapter(adapter);

        activitySpinner.setSelection(0);

        activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Dohvati odabranu aktivnost
                String selectedActivity = parentView.getItemAtPosition(position).toString();

                // Postavi odabranu aktivnost kao tekst na TextView
                selectedActivityTextView.setText("Aktivnost: " + selectedActivity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Ako ništa nije odabrano
            }
        });

        dialogPlus.show();

        closeActivity.setOnClickListener(v -> {
            inputDescriptionActivity.setText("");
            dialogPlus.dismiss();
        });

        addActivity.setOnClickListener(v -> {
            // Check if all fields are filled
            if (Objects.equals(inputDateActivity.getText().toString(), "") || Objects.equals(inputDescriptionActivity.getText().toString(), "")) {
                Toast.makeText(getContext(), "Moraju biti popunjeni svi podaci.", Toast.LENGTH_SHORT).show();
            } else {
                // Get selected activity from the spinner
                String selectedActivity = activitySpinner.getSelectedItem().toString();

                // Create a new Aktivnost object with the selected data
                Aktivnost novaAktivnost = new Aktivnost(
                        0, // Id (set to 0 for now, as it will be created in the database)
                        animalId, // The id of the animal
                        inputDateActivity.getText().toString(), // The selected date
                        selectedActivity, // The selected activity
                        inputDescriptionActivity.getText().toString() // The activity description
                );

                // Call the API to add the new activity
                apiService.addAktivnost(novaAktivnost).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // Slanje broadcasta s informacijama o novoj aktivnosti
                            Intent intent = new Intent("com.activity.pis_azil.NOVA_AKTIVNOST");
                            intent.putExtra("animalId", animalId);
                            intent.putExtra("aktivnost", selectedActivity);
                            intent.putExtra("datum", inputDateActivity.getText().toString());
                            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

                            Toast.makeText(getContext(), "Aktivnost uspješno dodana.", Toast.LENGTH_SHORT).show();
                            new Handler().postDelayed(() -> {
                                dialogPlus.dismiss();
                                inputDescriptionActivity.setText("");
                                refreshPopisAktivnosti();
                            }, 3000);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Greška u API pozivu.", Toast.LENGTH_SHORT).show();
                    }
                });

                apiService.getToken("matija.margeta@vuv.hr").enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            token = response.body();
                            Log.i("token", response.body());
                            NotificationBodyModel notification = new NotificationBodyModel("Test notification", "test body notification");
                            MessageModel messageModel = new MessageModel(token, notification);
                            NotificationModel notificationModel = new NotificationModel(messageModel);
                            apiServiceToken.sendNotification(notificationModel).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    Log.i("uspješno", response.message());
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Log.i("neuspješno", t.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(getContext(), "Greška u API pozivu.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        inputDateActivity.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear);
                        inputDateActivity.setText(selectedDate);
                    },
                    year, month, day);

            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        });
    }


    public void refreshPopisAktivnosti() {
        apiService.getAktivnostiById(animalId).enqueue(new Callback<HttpRequestResponseList<Aktivnost>>() {
            @Override
            public void onResponse(Call<HttpRequestResponseList<Aktivnost>> call, Response<HttpRequestResponseList<Aktivnost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaAktivnosti = response.body().getResult();
                    if (listaAktivnosti.size() == 0) {
                        tvNemaAktivnosti.setVisibility(View.VISIBLE);
                    } else {
                        linearLayoutAktivnosti.removeAllViews();
                        for (Aktivnost a : listaAktivnosti) {
                            TextView tv = new TextView(getContext());
                            String text = a.getDatum() + " " + a.getAktivnost() + "\n" + a.getOpis();
                            tv.setText(text);
                            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
                            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            tv.setTextSize(17);
                            tv.setTextColor(Color.parseColor("#000000"));
                            linearLayoutAktivnosti.addView(tv);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<HttpRequestResponseList<Aktivnost>> call, Throwable t) {
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshPopisAktivnosti();
    }
}
