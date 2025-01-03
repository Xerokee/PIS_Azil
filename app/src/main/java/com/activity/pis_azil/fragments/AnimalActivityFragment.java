package com.activity.pis_azil.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.activity.pis_azil.R;
import com.activity.pis_azil.models.Aktivnost;
import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.models.IsBlockedAnimalModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
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
    IsBlockedAnimalModel animalModel;
    int animalId;

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

        apiService = ApiClient.getClient().create(ApiService.class);

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
                            tv.setText(a.getDatum() + " - " + a.getOpis());
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
                .setExpanded(true, 1000)
                .setGravity(Gravity.CENTER)
                .setCancelable(true)
                .create();

        View dialogView = dialogPlus.getHolderView();
        TextView inputDateActivity = dialogView.findViewById(R.id.inputDateActivity);
        Button addActivity = dialogView.findViewById(R.id.addActivity);
        Button closeActivity = dialogView.findViewById(R.id.closeActivity);
        EditText inputDescriptionActivity = dialogView.findViewById(R.id.inputDescriptionActivity);

        dialogPlus.show();

        closeActivity.setOnClickListener(v -> {
            inputDescriptionActivity.setText("");
            dialogPlus.dismiss();
        });

        addActivity.setOnClickListener(v -> {
            if (Objects.equals(inputDateActivity.getText().toString(), "") || Objects.equals(inputDescriptionActivity.getText().toString(), "")) {
                Toast.makeText(getContext(), "Moraju biti popunjeni svi podaci.", Toast.LENGTH_SHORT).show();
            } else {
                Aktivnost novaAktivnost = new Aktivnost(0, animalId, inputDateActivity.getText().toString(), inputDescriptionActivity.getText().toString());
                apiService.addAktivnost(novaAktivnost).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
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
                            String text = a.getDatum() + " " + a.getOpis();
                            tv.setText(text);
                            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
                            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            tv.setTextSize(18);
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
