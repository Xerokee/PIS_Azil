package com.activity.pis_azil.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
    private final SimpleDateFormat newDateFormat = new SimpleDateFormat("dd-MM-YYYY", Locale.getDefault());

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

        //meetingsCalendar = view.findViewById(R.id.meetingsCalendar);
        //meetingsCalendar.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        CalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setMinDate(System.currentTimeMillis() - 1000);

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 13);
        calendarView.setMaxDate(maxDate.getTimeInMillis());

        calendarView.setWeekDayTextAppearance(R.style.CalendarTextAppearance);
        calendarView.setDateTextAppearance(R.style.CalendarTextAppearance);

        new Handler().postDelayed(() -> {
            try {
                ViewGroup vg = (ViewGroup) calendarView.getChildAt(0);
                if (vg != null) {
                    TextView monthTitle = (TextView) vg.getChildAt(0);
                    if (monthTitle instanceof TextView) {
                        monthTitle.setTextColor(Color.BLACK);
                    }
                    ImageButton leftArrow = (ImageButton) vg.getChildAt(1);
                    ImageButton rightArrow = (ImageButton) vg.getChildAt(2);
                    if (leftArrow != null) leftArrow.setColorFilter(Color.BLACK);
                    if (rightArrow != null) rightArrow.setColorFilter(Color.BLACK);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 100);

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
                    //meetingsCalendar.setAdapter(calendarAdapter);
                }
                else {
                    datesList = getDatesList();
                    calendarAdapter = new CalendarAdapter(getContext(), datesList, meetingsList, idKorisnika, MeetingFragment.this);
                    //meetingsCalendar.setAdapter(calendarAdapter);
                }
            }
            @Override
            public void onFailure(Call<HttpRequestResponseList<Meeting>> call, Throwable t) {
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
            }
        });
    }

    public void deleteMeeting(int idMeeting, DialogPlus dialogPlus, Date date) {
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
            }
        });
    }

    private boolean isAdmin() {
        return idKorisnika == 1;
    }

    private void showAdminOptions(Meeting meeting) {
        Log.i("pozvano", "pozvano");
    }

    private void showUserOptions(Meeting meeting) {
        if (meeting.getIdKorisnik() == 0) {
        } else if (meeting.getIdKorisnik() == idKorisnika) {
        } else {
            Toast.makeText(getContext(), "Termin nije slobodan!", Toast.LENGTH_SHORT).show();
        }
        Log.i("pozvano", "pozvano");
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
        Button btnDodaj = dialogView.findViewById(R.id.btnDodaj);

        if (!Objects.equals(idKorisnika, 1)){
            btnDodaj.setVisibility(View.GONE);
        }

        listMeetings.removeAllViews();

        btnDodaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialog.dismiss();
                View popUpView = LayoutInflater.from(v.getContext()).inflate(R.layout.new_meeting, null, false);
                PopupWindow popupWindow = new PopupWindow(popUpView,700,700,true);

                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.GRAY));
                TextView tvDatum = popUpView.findViewById(R.id.tvDatum);
                Spinner spinnerVrijeme = popUpView.findViewById(R.id.spinnerVrijeme);
                Button btnDodaj = popUpView.findViewById(R.id.btnDodaj);
                ImageButton btnClose = popUpView.findViewById(R.id.btnClose);
                tvDatum.setText(dateFormat.format(date));
                String[] slobodnoVrijemeLista = {"8:00","8:30","9:00","9:30","10:00","10:30","11:00","11:30","12:00","12:30","13:00","13:30","14:00", "14:30", "15:00", "15:30","16:00","16:30"};
                List<String> slobodno = new ArrayList<>(Arrays.asList(slobodnoVrijemeLista));
                for (Meeting m: meetingsList){
                    if (equalDate(date, m.getDatum())){
                        slobodno.remove(m.getVrijeme());
                    }
                }
                ArrayAdapter adapter = new ArrayAdapter(v.getContext(), android.R.layout.simple_spinner_item, slobodno);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerVrijeme.setAdapter(adapter);
                btnDodaj.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String vrijeme = spinnerVrijeme.getSelectedItem().toString();
                        NewMeeting noviSastanak = new NewMeeting(newDateFormat.format(date), vrijeme);
                        addMeeting(noviSastanak, dialog);
                        popupWindow.dismiss();
                    }
                });
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                popupWindow.showAtLocation(v,Gravity.CENTER,0,0);
            }
        });

        if (meetingsForDay.isEmpty() && !Objects.equals(idKorisnika, 1)) {
            Toast.makeText(getContext(), "Nema dodanih termina.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        for (Meeting meeting : meetingsForDay) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_meeting, listMeetings, false);
            TextView tvTime = itemView.findViewById(R.id.tvTime);
            TextView tvName = itemView.findViewById(R.id.tvName);
            Button btnObrisi = itemView.findViewById(R.id.btnObrisi);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnRezerviraj = itemView.findViewById(R.id.btnRezerviraj);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnOtkazi = itemView.findViewById(R.id.btnOtkazi);
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date startTime = inputFormat.parse(meeting.getVrijeme());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startTime);
                calendar.add(Calendar.MINUTE, 30);

                int startHour = Integer.parseInt(meeting.getVrijeme().split(":")[0]);
                String startMinutes = meeting.getVrijeme().split(":")[1];
                int endHour = calendar.get(Calendar.HOUR_OF_DAY);
                int endMinute = calendar.get(Calendar.MINUTE);

                String startTimeStr = startHour + ":" + startMinutes;
                String endTimeStr = endHour + ":" + (endMinute < 10 ? "0" + endMinute : endMinute);
                String timeRange = startTimeStr + " - " + endTimeStr;
                tvTime.setText(timeRange);
            } catch (Exception e) {
                tvTime.setText(meeting.getVrijeme());
            }
            if (meeting.getImeKorisnik() != null) {
                tvName.setText(meeting.getImeKorisnik());
                tvName.setTextColor(Color.parseColor("#FF0000"));
            } else {
                tvName.setText("Slobodno");
                tvName.setTextColor(Color.parseColor("#2fbd22"));
            }

            if (!Objects.equals(idKorisnika,1)){
                btnObrisi.setVisibility(View.GONE);
            }
            else{
                btnRezerviraj.setVisibility(View.GONE);
                btnOtkazi.setVisibility(View.GONE);
            }
            btnObrisi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteMeeting(meeting.getIdMeeting(), dialog, date);
                }
            });
            btnRezerviraj.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editMeeting(meeting.getIdMeeting(), idKorisnika, 1, dialog);
                }
            });
            btnOtkazi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editMeeting(meeting.getIdMeeting(), idKorisnika, 0, dialog);
                }
            });

            if (Objects.equals(idKorisnika,1)){
            }
            else {
                if (meeting.getIdKorisnik() == 0) {
                    btnOtkazi.setVisibility(View.GONE);
                } else if (meeting.getIdKorisnik() == idKorisnika) {
                    btnRezerviraj.setVisibility(View.GONE);
                } else {
                    btnRezerviraj.setVisibility(View.GONE);
                    btnOtkazi.setVisibility(View.GONE);
                }
            }

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