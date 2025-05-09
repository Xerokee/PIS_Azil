package com.activity.pis_azil.models;

import com.google.gson.annotations.SerializedName;

public class SifrTipLjubimca {
    @SerializedName("id")
    private int id;

    @SerializedName("naziv")
    private String naziv;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }
}

