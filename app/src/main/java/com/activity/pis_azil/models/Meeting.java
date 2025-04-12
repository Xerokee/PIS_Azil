package com.activity.pis_azil.models;

import java.util.Date;

public class Meeting {

    public int idMeeting;
    public Date datum;
    public String vrijeme;
    public int idKorisnik;
    public String imeKorisnik;

    public Meeting(int idMeeting, Date datum, String vrijeme, int idKorisnik, String imeKorisnik)
    {
        this.idMeeting = idMeeting;
        this.datum = datum;
        this.vrijeme = vrijeme;
        this.idKorisnik = idKorisnik;
        this.imeKorisnik = imeKorisnik;
    }

    public int getIdMeeting() {
        return idMeeting;
    }

    public void setIdMeeting(int idMeeting) {
        this.idMeeting = idMeeting;
    }

    public Date getDatum() {
        return datum;
    }

    public void setDatum(Date datum) {
        this.datum = datum;
    }

    public String getVrijeme() {
        return vrijeme;
    }

    public void setVrijeme(String vrijeme) {
        this.vrijeme = vrijeme;
    }

    public int getIdKorisnik() {
        return idKorisnik;
    }

    public void setIdKorisnik(int idKorisnik) {
        this.idKorisnik = idKorisnik;
    }

    public String getImeKorisnik() {
        return imeKorisnik;
    }

    public void setImeKorisnik(String imeKorisnik) {
        this.imeKorisnik = imeKorisnik;
    }
}
