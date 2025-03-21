package com.activity.pis_azil.models;

public class NotificationModel {
    public MessageModel message;
    public NotificationBodyModel notification;

    public NotificationModel(MessageModel m)
    {
        this.message = m;
    }

    public MessageModel getMessage() {
        return message;
    }

    public void setMessage(MessageModel message) {
        this.message = message;
    }
}
