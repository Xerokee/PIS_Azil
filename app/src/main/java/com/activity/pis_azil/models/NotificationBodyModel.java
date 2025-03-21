package com.activity.pis_azil.models;

public class NotificationBodyModel {
    private String title;
    private String body;

    public NotificationBodyModel(String t, String b)
    {
        this.title = t;
        this.body = b;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String Title) {
        this.title = Title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String Body) {
        this.body = Body;
    }
}
