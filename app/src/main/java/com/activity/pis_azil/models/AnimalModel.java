package com.activity.pis_azil.models;

import java.io.Serializable;

public class AnimalModel implements Serializable {
    private int id_ljubimca;
    private int id_udomitelja;
    private String ime_ljubimca;
    private String tip_ljubimca;
    private String opis_ljubimca;
    private boolean udomljen;
    private String datum; // Added
    private String imgUrl;

    // Constructors, getters, and setters
    public AnimalModel() {}

    public AnimalModel(int id_ljubimca, int id_udomitelja, String ime_ljubimca, String tip_ljubimca, String opis_ljubimca, boolean udomljen, String datum, String imgUrl) {
        this.id_ljubimca = id_ljubimca;
        this.id_udomitelja = id_udomitelja;
        this.ime_ljubimca = ime_ljubimca;
        this.tip_ljubimca = tip_ljubimca;
        this.opis_ljubimca = opis_ljubimca;
        this.udomljen = udomljen;
        this.datum = datum;
        this.imgUrl = imgUrl;
    }

    public int getIdLjubimca() {
        return id_ljubimca;
    }

    public void setIdLjubimca(int id_ljubimca) {
        this.id_ljubimca = id_ljubimca;
    }

    public int getIdUdomitelja() {
        return id_udomitelja;
    }

    public void setIdUdomitelja(int id_udomitelja) {
        this.id_udomitelja = id_udomitelja;
    }

    public String getImeLjubimca() {
        return ime_ljubimca;
    }

    public void setImeLjubimca(String ime_ljubimca) {
        this.ime_ljubimca = ime_ljubimca;
    }

    public String getTipLjubimca() {
        return tip_ljubimca;
    }

    public void setTipLjubimca(String tip_ljubimca) {
        this.tip_ljubimca = tip_ljubimca;
    }

    public String getOpisLjubimca() {
        return opis_ljubimca;
    }

    public void setOpisLjubimca(String opis_ljubimca) {
        this.opis_ljubimca = opis_ljubimca;
    }

    public boolean isUdomljen() {
        return udomljen;
    }

    public void setUdomljen(boolean udomljen) {
        this.udomljen = udomljen;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
