package com.activity.pis_azil;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.activity.pis_azil.models.AnimalModel;
import com.activity.pis_azil.network.ApiClient;
import com.activity.pis_azil.network.ApiService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        checkAnimalStatus(context);
    }

    public static void setAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE);

        long interval = AlarmManager.INTERVAL_DAY * 7;
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
    }

    private void checkAnimalStatus(Context context) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getAdoptedAnimals().enqueue(new Callback<List<AnimalModel>>() {
            @Override
            public void onResponse(Call<List<AnimalModel>> call, Response<List<AnimalModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AnimalModel> animals = response.body();
                    for (AnimalModel animal : animals) {
                        if (animal.getDatum() != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                Date lastUpdateDate = sdf.parse(animal.getDatum());
                                Calendar currentDate = Calendar.getInstance();
                                Calendar lastUpdateCalendar = Calendar.getInstance();
                                lastUpdateCalendar.setTime(lastUpdateDate);

                                long diff = currentDate.getTimeInMillis() - lastUpdateCalendar.getTimeInMillis();
                                long daysBetween = diff / (24 * 60 * 60 * 1000);

                                if (daysBetween > 7) {
                                    sendNotification(context, animal.getImeLjubimca());
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AnimalModel>> call, Throwable t) {
                // Log error
            }
        });
    }

    private void sendNotification(Context context, String animalName) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "animal_status_channel";
        String channelName = "Animal Status Notifications";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.paw)
                .setContentTitle("Nema zapisa o stanju životinje")
                .setContentText("Nema zapisa o stanju životinje " + animalName + " u posljednjih tjedan dana.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(animalName.hashCode(), builder.build());
    }
}
