package com.activity.pis_azil.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.fragments.MeetingFragment;
import com.activity.pis_azil.models.Meeting;
import com.activity.pis_azil.models.NewMeeting;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;
import com.activity.pis_azil.network.DateDeserializer;
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

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    ApiService apiService;
    Context context;
    List<Date> datesList;
    List<Meeting> meetingsList;
    int idKorisnika;
    MeetingFragment fragment;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
    private final SimpleDateFormat newDateFormat = new SimpleDateFormat("dd-MM-YYYY", Locale.getDefault());

    String[] vrijemeLista = {"8:00","8:30","9:00","9:30","10:00","10:30","11:00","11:30","12:00","12:30","13:00","13:30","14:00", "14:30", "15:00", "15:30","16:00","16:30"};

    public CalendarAdapter(Context context, List<Date> datesList, List<Meeting> meetingsList, int idKorisnik, MeetingFragment fragment)
    {
        this.context= context;
        this.datesList = datesList;
        this.meetingsList = meetingsList;
        this.idKorisnika = idKorisnik;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public CalendarAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_day, parent, false);
        return new CalendarAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarAdapter.ViewHolder holder, int position) {
        apiService = ApiClient.getClient().create(ApiService.class);
        Date date = datesList.get(position);
        holder.tvDate.setText(dateFormat.format(date));

        String[] slobodnoVrijemeLista = {"8:00","8:30","9:00","9:30","10:00","10:30","11:00","11:30","12:00","12:30","13:00","13:30","14:00", "14:30", "15:00", "15:30","16:00","16:30"};
        List<String> slobodno = new ArrayList<>(Arrays.asList(slobodnoVrijemeLista));
        for (Meeting m: meetingsList){
            if (equalDate(date, m.getDatum())){
                slobodno.remove(m.getVrijeme());
            }
        }

        holder.meetingsDayList.removeAllViews();
        for (Meeting meeting: meetingsList){
            if (equalDate(date, meeting.getDatum())){
                View meetingView = LayoutInflater.from(context).inflate(R.layout.item_meeting, holder.meetingsDayList, false);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView tvTime = meetingView.findViewById(R.id.tvTime);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView tvIme = meetingView.findViewById(R.id.tvName);
                tvTime.setText(meeting.getVrijeme());
                if (Objects.equals(meeting.getImeKorisnik(), "") || Objects.equals(meeting.getImeKorisnik(), null)){
                    tvIme.setVisibility(View.GONE);
                }
                else{
                    tvIme.setText(meeting.getImeKorisnik());
                }
                holder.meetingsDayList.addView(meetingView);

                meetingView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final DialogPlus dialogMeeting = DialogPlus.newDialog(v.getContext())
                                .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.edit_meeting))
                                .setExpanded(true, 600)
                                .setGravity(Gravity.CENTER)
                                .setCancelable(true)
                                .create();

                        View view = dialogMeeting.getHolderView();
                        TextView tvDatum = view.findViewById(R.id.tvDatum);
                        TextView tvVrijeme = view.findViewById(R.id.tvVrijeme);
                        Button btnObrisi = view.findViewById(R.id.btnObrisi);
                        Button btnRezerviraj = view.findViewById(R.id.btnRezerviraj);
                        Button btnOtkazi = view.findViewById(R.id.btnOtkazi);

                        tvDatum.setText(dateFormat.format(meeting.getDatum()));
                        tvVrijeme.setText(meeting.getVrijeme());

                        if (!Objects.equals(idKorisnika,1)){  //nisi admin
                            btnObrisi.setVisibility(View.GONE);
                        }
                        else{
                            btnRezerviraj.setVisibility(View.GONE); //jesi admin
                            btnOtkazi.setVisibility(View.GONE);
                        }
                        btnObrisi.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //fragment.deleteMeeting(meeting.getIdMeeting(), dialogMeeting);
                            }
                        });
                        btnRezerviraj.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                fragment.editMeeting(meeting.getIdMeeting(), idKorisnika, 1, dialogMeeting);
                            }
                        });
                        btnOtkazi.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                fragment.editMeeting(meeting.getIdMeeting(), idKorisnika, 0, dialogMeeting);
                            }
                        });
                        if (Objects.equals(idKorisnika,1)){
                            dialogMeeting.show();
                        }
                        else {
                            if (!Objects.equals(idKorisnika,meeting.getIdKorisnik()) && !Objects.equals(0,meeting.getIdKorisnik())){

                            }
                            else{
                                if (!Objects.equals(idKorisnika,meeting.getIdKorisnik()) && Objects.equals(0,meeting.getIdKorisnik())){
                                    btnOtkazi.setVisibility(View.GONE);
                                }
                                else{
                                    btnRezerviraj.setVisibility(View.GONE);
                                }
                                dialogMeeting.show();
                            }
                        }
                    }
                });
            }
        }

        if (!Objects.equals(idKorisnika,1)){
            holder.btnAddMeeting.setVisibility(View.GONE);
        }

        holder.btnAddMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogPlus dialogAdd = DialogPlus.newDialog(v.getContext())
                        .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.new_meeting))
                        .setExpanded(true, 600)
                        .setGravity(Gravity.CENTER)
                        .setCancelable(true)
                        .create();

                View view = dialogAdd.getHolderView();
                TextView tvDatum = view.findViewById(R.id.tvDatum);
                Spinner spinnerVrijeme = view.findViewById(R.id.spinnerVrijeme);
                Button btnDodaj = view.findViewById(R.id.btnDodaj);

                tvDatum.setText(dateFormat.format(date));
                ArrayAdapter adapter = new ArrayAdapter(context.getApplicationContext(), android.R.layout.simple_spinner_item, slobodno);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerVrijeme.setAdapter(adapter);
                btnDodaj.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String vrijeme = spinnerVrijeme.getSelectedItem().toString();
                        NewMeeting noviSastanak = new NewMeeting(newDateFormat.format(date), vrijeme);
                        fragment.addMeeting(noviSastanak, dialogAdd);
                    }
                });
                dialogAdd.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return datesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        ImageButton btnAddMeeting;
        LinearLayout meetingsDayList;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnAddMeeting = itemView.findViewById(R.id.btnAddMeeting);
            meetingsDayList = itemView.findViewById(R.id.meetingsDayList);
        }
    }

    private boolean equalDate(Date date1, Date date2){
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(date1);
        calendar2.setTime(date2);
        boolean equal = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)  && calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR);
        return equal;
    }
}