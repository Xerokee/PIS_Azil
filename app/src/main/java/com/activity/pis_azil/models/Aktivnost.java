package com.activity.pis_azil.models;

import java.util.List;

public class Aktivnost {

    private int id;
    private int id_ljubimca;
    private String datum;
    private String aktivnost;
    private String opis;

    public Aktivnost() {}

    public Aktivnost(int id, int id_ljubimca, String datum, String aktivnost, String opis) {
        this.id=id;
        this.id_ljubimca = id_ljubimca;
        this.datum = datum;
        this.aktivnost = aktivnost;
        this.opis= opis;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getIdLjubimca() {
        return id_ljubimca;
    }
    public void setIdLjubimca(int id_ljubimca) {
        this.id_ljubimca = id_ljubimca;
    }
    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getAktivnost() {
        return aktivnost;
    }

    public void setAktivnost(String aktivnost) {
        this.aktivnost = aktivnost;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }
}
