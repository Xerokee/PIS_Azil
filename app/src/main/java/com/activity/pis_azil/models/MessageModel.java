package com.activity.pis_azil.models;

public class MessageModel {
    public String token;
    public NotificationBodyModel notification;

    public MessageModel(String t, NotificationBodyModel n)
    {
        this.token = t;
        this.notification = n;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String Token) {
        this.token = Token;
    }

    public NotificationBodyModel getNotification() {
        return notification;
    }

    public void setNotification(NotificationBodyModel Notification) { this.notification = Notification;
    }
}
