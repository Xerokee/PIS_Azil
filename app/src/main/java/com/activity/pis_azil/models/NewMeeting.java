package com.activity.pis_azil.models;

import java.util.Date;

public class NewMeeting {

    public String datum;
    public String vrijeme;

    public NewMeeting(String datum, String vrijeme)
    {
        this.datum = datum;
        this.vrijeme = vrijeme;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getVrijeme() {
        return vrijeme;
    }

    public void setVrijeme(String vrijeme) {
        this.vrijeme = vrijeme;
    }
}
