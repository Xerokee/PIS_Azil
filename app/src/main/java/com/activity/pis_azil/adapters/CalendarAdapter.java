package com.activity.pis_azil.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.pis_azil.R;
import com.activity.pis_azil.fragments.MeetingFragment;
import com.activity.pis_azil.models.Meeting;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    Context context;
    List<Date> datesList;
    List<Meeting> meetingsList;
    int idKorisnika;
    MeetingFragment fragment;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());

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
        Date date = datesList.get(position);
        holder.tvDate.setText(dateFormat.format(date));
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
            }
        }

        if (!Objects.equals(idKorisnika,0)){
            holder.btnAddMeeting.setVisibility(View.GONE);
        }

        holder.btnAddMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
