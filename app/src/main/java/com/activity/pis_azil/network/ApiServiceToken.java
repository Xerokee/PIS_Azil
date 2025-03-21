package com.activity.pis_azil.network;

import com.activity.pis_azil.models.MessageModel;
import com.activity.pis_azil.models.NotificationModel;
import com.activity.pis_azil.models.UpdateDnevnikModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiServiceToken {
    @Headers({
            "Authorization: Bearer ya29.a0AeXRPp6Pb4lpoPCXC8k7HAAVX3bA4EnFyq0MiaY-klZOsulThnz8LbFyZ6LUbPFSaMqoB46tAEd6utgeYuMrdzbuoIizfRH_K6wH_ooZ3JocbF0vxLMcAAA1xRppUzxhHfQ_mTAI1HZq_5P8oZiu3WpwqZ2OSW6ed0VN--U4aCgYKASsSARISFQHGX2MilEtlQvcFgDkfOpqv8cha4Q0175",
            "Content-Type: application/json"
    })
    @POST("projects/1:11441286255:android:fc83408f0bf0d0ffc4719a/messages:send")
    Call<Void> sendNotification(@Body NotificationModel notificationModel);
}
