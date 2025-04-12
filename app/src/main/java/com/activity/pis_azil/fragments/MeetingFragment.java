package com.activity.pis_azil.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.activity.pis_azil.R;
import com.activity.pis_azil.adapters.CalendarAdapter;
import com.activity.pis_azil.models.Meeting;
import com.activity.pis_azil.models.UpdateDnevnikModel;
import com.activity.pis_azil.models.UserModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.network.HttpRequestResponseList;
import com.google.gson.Gson;

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