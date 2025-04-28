package com.activity.pis_azil.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.CalendarAdapter;
import com.activity.pis_azil.models.Meeting;
import com.activity.pis_azil.models.NewMeeting;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.network.HttpRequestResponseList;
import com.google.gson.Gson;
import com.orhanobut.dialogplus.DialogPlus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MeetingFragment extends Fragment {

    int idKorisnika;
    RecyclerView meetingsCalendar;
    List<Date> datesList = new ArrayList<>();
    List<Meeting> meetingsList = new ArrayList<>();
    ApiService apiService;
    CalendarAdapter calendarAdapter;

    public MeetingFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meeting, container, false);

        apiService = ApiClient.getClient().create(ApiService.class);

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userJson = prefs.getString("current_user", null);
        UserModel currentUser = new Gson().fromJson(userJson, UserModel.class);
        idKorisnika=currentUser.getIdKorisnika();

        meetingsCalendar = view.findViewById(R.id.meetingsCalendar);
        meetingsCalendar.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        CalendarView calendarView = view.findViewById(R.id.calendarView);

        calendarView.setWeekDayTextAppearance(R.style.CalendarTextAppearance);
        calendarView.setDateTextAppearance(R.style.CalendarTextAppearance);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                showMeetingsDialog(selectedDate.getTime());
            }
        });

        getMeetings();

        return view;
    }

    private void getMeetings(){
        apiService.getMeetings().enqueue(new Callback<HttpRequestResponseList<Meeting>>() {
            @Override
            public void onResponse(Call<HttpRequestResponseList<Meeting>> call, Response<HttpRequestResponseList<Meeting>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    meetingsList = response.body().getResult();
                    datesList = getDatesList();
                    calendarAdapter = new CalendarAdapter(getContext(), datesList, meetingsList, idKorisnika, MeetingFragment.this);
                    meetingsCalendar.setAdapter(calendarAdapter);
                }
                else {
                    datesList = getDatesList();
                    calendarAdapter = new CalendarAdapter(getContext(), datesList, meetingsList, idKorisnika, MeetingFragment.this);
                    meetingsCalendar.setAdapter(calendarAdapter);
                }
            }
            @Override
            public void onFailure(Call<HttpRequestResponseList<Meeting>> call, Throwable t) {
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addMeeting(NewMeeting newMeeting, DialogPlus dialogPlus) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.addMeeting(newMeeting).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Novi termin uspješno dodan.", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(() -> {
                        updateMeetings();
                        dialogPlus.dismiss();
                    }, 3000);
                } else {
                    Toast.makeText(getContext(), "Termin nije uspješno dodan.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Greška u API pozivu.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteMeeting(int idMeeting, DialogPlus dialogPlus) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.deleteMeeting(idMeeting).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Termin uspješno obrisan.", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(() -> {
                        updateMeetings();
                        dialogPlus.dismiss();
                    }, 3000);
                } else {
                    Toast.makeText(getContext(), "Termin nije uspješno obrisan.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Greška u API pozivu.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void editMeeting(int idMeeting, int idKorisnik, int rezervirano, DialogPlus dialogPlus) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.editMeeting(idMeeting, idKorisnik, rezervirano).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    String message = rezervirano == 1 ? "Termin uspješno rezerviran." : "Termin uspješno otkazan.";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(() -> {
                        updateMeetings();
                        dialogPlus.dismiss();
                    }, 3000);
                } else {
                    String message = rezervirano == 1 ? "Termin nije uspješno rezerviran." : "Termin nije uspješno otkazan.";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Greška u API pozivu.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isAdmin() {
        return idKorisnika == 1; // Ili tvoje pravilo za admina
    }

    private void showAdminOptions(Meeting meeting) {
    }

    private void showUserOptions(Meeting meeting) {
        if (meeting.getIdKorisnik() == 0) {
        } else if (meeting.getIdKorisnik() == idKorisnika) {
        } else {
            Toast.makeText(getContext(), "Termin nije slobodan!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean equalDate(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void showMeetingsDialog(Date date) {
        List<Meeting> meetingsForDay = new ArrayList<>();
        for (Meeting m : meetingsList) {
            if (equalDate(date, m.getDatum())) {
                meetingsForDay.add(m);
            }
        }

        DialogPlus dialog = DialogPlus.newDialog(requireContext())
                .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.dialog_day_meetings))
                .setExpanded(true, ViewGroup.LayoutParams.WRAP_CONTENT)
                .setGravity(Gravity.CENTER)
                .setCancelable(true)
                .create();

        View dialogView = dialog.getHolderView();
        LinearLayout listMeetings = dialogView.findViewById(R.id.listMeetings);

        listMeetings.removeAllViews();

        if (meetingsForDay.isEmpty()) {
            Toast.makeText(getContext(), "Nema navedenih termina.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        for (Meeting meeting : meetingsForDay) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_meeting, listMeetings, false);
            TextView tvTime = itemView.findViewById(R.id.tvTime);
            TextView tvName = itemView.findViewById(R.id.tvName);
            tvTime.setText(meeting.getVrijeme());
            tvName.setText(meeting.getImeKorisnik() != null ? meeting.getImeKorisnik() : "Slobodno");

            itemView.setOnClickListener(v -> {
                if (isAdmin()) {
                    showAdminOptions(meeting);
                } else {
                    showUserOptions(meeting);
                }
                dialog.dismiss();
            });

            listMeetings.addView(itemView);
        }

        dialog.show();
    }

    private List<Date> getDatesList(){
        List<Date> datesList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (int i=0; i<14; i++){
            datesList.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH,1);
        }
        return datesList;
    }

    public void updateMeetings(){
        getMeetings();
    }
}