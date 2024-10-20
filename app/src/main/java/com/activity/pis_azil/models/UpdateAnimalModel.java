package com.activity.pis_azil.models;

import java.util.List;

public class UpdateAnimalModel {

    private String ime_ljubimca;
    private String opis_ljubimca;
    private int dob;

    // Constructors, getters, and setters
    public UpdateAnimalModel() {}

    public UpdateAnimalModel(String ime_ljubimca,  String opis_ljubimca, int dob) {
        this.ime_ljubimca = ime_ljubimca;
        this.opis_ljubimca = opis_ljubimca;
        this.dob=dob;

    }

    public String getImeLjubimca() {
        return ime_ljubimca;
    }

    public void setImeLjubimca(String ime_ljubimca) {
        this.ime_ljubimca = ime_ljubimca;
    }


    public String getOpisLjubimca() {
        return opis_ljubimca;
    }

    public void setOpisLjubimca(String opis_ljubimca) {
        this.opis_ljubimca = opis_ljubimca;
    }

    public int getDob(){return dob;}
    public void setDob(int dob) {this.dob=dob;}


}
